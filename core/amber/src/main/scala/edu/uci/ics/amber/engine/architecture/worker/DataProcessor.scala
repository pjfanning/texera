package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.ActorRef
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LinkCompletedHandler.LinkCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LocalOperatorExceptionHandler.LocalOperatorException
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionStartedHandler.WorkerStateUpdated
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{
  LogManager,
  ProcessControlMessage,
  SenderActorChange
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.OutputManager
import edu.uci.ics.amber.engine.architecture.recovery.{LocalRecoveryManager, RecoveryQueue}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.architecture.logging.{LogManager, ProcessControlMessage, SenderActorChange}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkOutputPort, TupleToBatchConverter}
import edu.uci.ics.amber.engine.architecture.recovery.LocalRecoveryManager
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor._
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{COMPLETED, PAUSED, READY, RUNNING, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataPayload, WorkflowControlMessage, WorkflowDataMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
import edu.uci.ics.amber.engine.common.{AmberLogging, IOperatorExecutor, InputExhausted}
import edu.uci.ics.amber.error.ErrorUtils.safely

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors, Future}
import scala.collection.mutable
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest


object DataProcessor{
  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalCommand

  case class Shutdown(reason:Option[Throwable], completionFuture:CompletableFuture[Unit]) extends InternalCommand

  case class Checkpoint(networkSender:ActorRef, completionFuture:CompletableFuture[Long]) extends InternalCommand

  case object NoOperation extends InternalCommand

  case class ControlElement(payload: ControlPayload, from: ActorVirtualIdentity)

  sealed trait DataElement

  case class InputTuple(from: ActorVirtualIdentity, tuple: ITuple) extends DataElement

  case class EndMarker(from: ActorVirtualIdentity) extends DataElement

  case class FinalizeLink(link:LinkIdentity) extends DataElement

  case class FinalizeOperator() extends DataElement
}


class DataProcessor( // meta dependencies:
                     val operator: IOperatorExecutor, // core logic
                     val opExecConfig: OpExecConfig,
                     val actorId: ActorVirtualIdentity,
                     val allUpstreamLinkIds: Set[LinkIdentity],
                     val inputToOrdinalMapping: Map[LinkIdentity, Int],
                     //  use two different types for the wire library to do dependency injection
                     // temporary workaround, will be refactored soon
                     val outputToOrdinalMapping: mutable.Map[LinkIdentity, Int]
) extends DataProcessorRPCHandlerInitializer with AmberLogging with java.io.Serializable {

  // outer dependencies
  protected var inputHub:InputHub = _
  protected var logStorage: DeterminantLogStorage = _
  protected var logManager: LogManager = _
  protected var recoveryManager: LocalRecoveryManager = _

  def initialize(
  inputHub: InputHub,
  logStorage: DeterminantLogStorage,
  logManager: LogManager,
  recoveryManager: LocalRecoveryManager): Unit ={
    this.inputHub = inputHub
    this.logStorage = logStorage
    this.logManager = logManager
    this.recoveryManager = recoveryManager
  }

  def outputDataPayload(
                         to: ActorVirtualIdentity,
                         self: ActorVirtualIdentity,
                         seqNum: Long,
                         payload: DataPayload
                       ): Unit = {
    val msg = WorkflowDataMessage(self, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }

  def outputControlPayload(
                            to: ActorVirtualIdentity,
                            self: ActorVirtualIdentity,
                            seqNum: Long,
                            payload: ControlPayload
                          ): Unit = {
    val msg = WorkflowControlMessage(self, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }

  // inner dependencies
  // 1. Data Output
  lazy protected val dataOutputPort: NetworkOutputPort[DataPayload] =
    new NetworkOutputPort[DataPayload](this.actorId, this.outputDataPayload)
  // 2. Control Output
  lazy protected val controlOutputPort: NetworkOutputPort[ControlPayload] = {
    new NetworkOutputPort[ControlPayload](this.actorId, this.outputControlPayload)
  }
  // 3. RPC Layer
  lazy protected val asyncRPCClient: AsyncRPCClient = new AsyncRPCClient(controlOutputPort, actorId)
  lazy protected val asyncRPCServer: AsyncRPCServer = new AsyncRPCServer(controlOutputPort, actorId)
  // 4. pause manager
  lazy protected val pauseManager: PauseManager = wire[PauseManager]
  // 5. breakpoint manager
  lazy protected val breakpointManager: BreakpointManager = wire[BreakpointManager]
  // 6. upstream links
  lazy protected val upstreamLinkStatus: UpstreamLinkStatus = wire[UpstreamLinkStatus]
  // 7. state manager
  lazy protected val stateManager: WorkerStateManager = new WorkerStateManager()
  // 8. batch producer
  lazy protected val outputManager: OutputManager = new OutputManager(actorId, dataOutputPort)

  /**
    * Map from Identifier to input number. Used to convert the Identifier
    * to int when adding sender info to the queue.
    * We also keep track of the upstream actors so that we can emit
    * EndOfAllMarker when all upstream actors complete their job
    */
  protected val inputMap = new mutable.HashMap[ActorVirtualIdentity, LinkIdentity]
  protected val determinantLogger = logManager.getDeterminantLogger

  // dp thread stats:
  // TODO: add another variable for recovery index instead of using the counts below.
  protected var inputTupleCount = 0L
  protected var outputTupleCount = 0L
  protected var currentInputTuple: Either[ITuple, InputExhausted] = _
  protected var currentInputActor: ActorVirtualIdentity = _
  protected var currentOutputIterator: Iterator[(ITuple, Option[Int])] = _
  protected val dataQueue = inputHub.dataDeque
  protected val controlQueue = inputHub.controlDeque
  protected val internalQueue = inputHub.internalDeque
  protected var totalValidStep = 0L

  // initialize dp thread upon construction
  @transient
  private val dpThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor
  @transient
  private var dpThread: Future[_] = _
  def start(): Unit = {
    if(stateManager.getCurrentState == UNINITIALIZED){
      stateManager.transitTo(READY)
    }
    if (dpThread == null) {
      // TODO: setup context
      // operator.context = new OperatorContext(new TimeService(logManager))
      if (!inputHub.recoveryCompleted) {
        inputHub.registerOnEnd(() => recoveryManager.End())
        recoveryManager.Start()
        recoveryManager.registerOnEnd(() => {
          logger.info("recovery complete! restoring stashed inputs...")
          logManager.terminate()
          logStorage.cleanPartiallyWrittenLogFile()
          logManager.setupWriter(logStorage.getWriter)
          logger.info("stashed inputs restored!")
        })
      }
      dpThread = dpThreadExecutor.submit(new Runnable() {
        def run(): Unit = {
          try {
            runDPThreadMainLogicNew()
          } catch safely {
            case _: InterruptedException =>
              // dp thread will stop here
              logger.info("DP Thread exits")
            case err: Exception =>
              logger.error("DP Thread exists unexpectedly", err)
              asyncRPCClient.send(
                FatalError(new WorkflowRuntimeException("DP Thread exists unexpectedly", err)),
                CONTROLLER
              )
          }
        }
      })
    }
  }

  def registerInput(identifier: ActorVirtualIdentity, input: LinkIdentity): Unit = {
    inputMap(identifier) = input
  }

  def getInputLink(identifier: ActorVirtualIdentity): LinkIdentity = {
    if (identifier != null) {
      inputMap(identifier)
    } else {
      null // special case for source operator
    }
  }

  def getInputPort(identifier: ActorVirtualIdentity): Int = {
    val inputLink = getInputLink(identifier)
    if (inputLink == null) 0
    else if (!opExecConfig.inputToOrdinalMapping.contains(inputLink)) 0
    else opExecConfig.inputToOrdinalMapping(inputLink)
  }

  def getOutputLinkByPort(outputPort: Option[Int]): List[LinkIdentity] = {
    if (outputPort.isEmpty) {
      opExecConfig.outputToOrdinalMapping.keySet.toList
    } else {
      opExecConfig.outputToOrdinalMapping.filter(p => p._2 == outputPort.get).keys.toList
    }
  }

  def getOperatorExecutor(): IOperatorExecutor = operator

  /** provide API for actor to get stats of this operator
    *
    * @return (input tuple count, output tuple count)
    */
  def collectStatistics(): (Long, Long) = (inputTupleCount, outputTupleCount)

  /** provide API for actor to get current input tuple of this operator
    *
    * @return current input tuple if it exists
    */
  def getCurrentInputTuple: ITuple = {
    if (currentInputTuple != null && currentInputTuple.isLeft) {
      currentInputTuple.left.get
    } else {
      null
    }
  }

  def setCurrentTuple(tuple: Either[ITuple, InputExhausted]): Unit = {
    currentInputTuple = tuple
  }
  /** process currentInputTuple through operator logic.
    * this function is only called by the DP thread
    *
    * @return an iterator of output tuples
    */
  private[this] def processInputTuple(tuple:Either[ITuple,InputExhausted]):Unit = {
    currentInputTuple = tuple
    try {
      currentOutputIterator = operator.processTuple(
        currentInputTuple,
        getInputPort(currentInputActor),
        pauseManager,
        asyncRPCClient
      )
      if (currentInputTuple.isLeft) {
        inputTupleCount += 1
      }
      if (pauseManager.getPauseStatusByType(PauseType.OperatorLogicPause)) {
        // if the operatorLogic decides to pause, we need to disable the data queue for this worker.
      }
    } catch safely {
      case e =>
        // forward input tuple to the user and pause DP thread
        handleOperatorException(e)
    }
  }

  /** transfer one tuple from iterator to downstream.
    * this function is only called by the DP thread
    */
  private[this] def outputOneTuple(): Unit = {
    determinantLogger.stepIncrement()
    var out: (ITuple, Option[Int]) = null
    try {
      out = currentOutputIterator.next
    } catch safely {
      case e =>
        // invalidate current output tuple
        out = null
        // also invalidate outputIterator
        currentOutputIterator = null
        // forward input tuple to the user and pause DP thread
        handleOperatorException(e)
    }
    if (out == null) return

    val (outputTuple, outputPortOpt) = out
    if (breakpointManager.evaluateTuple(outputTuple)) {
      pauseManager.recordRequest(PauseType.UserPause, true)
      outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
      stateManager.transitTo(PAUSED)
    } else {
      outputTupleCount += 1
      val outLinks = getOutputLinkByPort(outputPortOpt)
      outLinks.foreach(link => outputManager.passTupleToDownstream(outputTuple, link))
    }
  }

  private[this] def handleDataElement(
                                       dataElement: DataElement
  ): Unit = {
    determinantLogger.stepIncrement()
    dataElement match {
      case InputTuple(from, tuple) =>
        if (stateManager.getCurrentState == READY) {
          stateManager.transitTo(RUNNING)
          outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(actorContext)
          asyncRPCClient.fireAndForget(
            WorkerStateUpdated(stateManager.getCurrentState),
            CONTROLLER
          )
        }
        if (currentInputActor != from) {
          determinantLogger.logDeterminant(SenderActorChange(from))
          currentInputActor = from
        }
        processInputTuple(Left(tuple))
      case EndMarker(from) =>
        if (currentInputActor != from) {
          determinantLogger.logDeterminant(SenderActorChange(from))
          currentInputActor = from
        }
        val currentLink = getInputLink(currentInputActor)
        upstreamLinkStatus.markWorkerEOF(from, currentLink)
        if (upstreamLinkStatus.isAllEOF) {
          dataQueue.addFirst(FinalizeOperator())
        }
        if (upstreamLinkStatus.isLinkEOF(currentLink)) {
          processInputTuple(Right(InputExhausted()))
          dataQueue.addFirst(FinalizeLink(currentLink))
        }
      case FinalizeLink(link) =>
        asyncRPCClient.fireAndForget(LinkCompleted(link), CONTROLLER)
      case FinalizeOperator() =>
        outputManager.emitEndOfUpstream()
        // Send Completed signal to worker actor.
        logger.info(s"$operator completed")
        operator.close() // close operator
        outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
        stateManager.transitTo(COMPLETED)
        asyncRPCClient.fireAndForget(WorkerExecutionCompleted(), CONTROLLER)
    }
  }

  @throws[Exception]
  private[this] def runDPThreadMainLogicNew(): Unit = {
    // main DP loop
    while (true) {
      val internalNotification = internalQueue.availableNotification()
      if(internalNotification.isDone){
        handleInternalMessage(internalQueue.dequeue())
      }
      val outputAvailable = currentOutputIterator != null && currentOutputIterator.hasNext
      val replayBlocking = inputHub.prepareInput(totalValidStep, outputAvailable)
      totalValidStep += 1
      val controlNotification = controlQueue.availableNotification()
      if (controlNotification.isDone) {
        // process control
        val control = controlQueue.dequeue()
        println(s"process control $control at step $totalValidStep")
        processControlCommand(control.payload, control.from)
      } else if (pauseManager.isPaused() || replayBlocking) {
        println(s"start waiting 2 futures at step $totalValidStep")
        CompletableFuture.anyOf(internalNotification, controlNotification).get()
        totalValidStep -= 1 // this round did nothing.
      } else if (outputAvailable) {
        // send output
        println(s"send output at step $totalValidStep")
        outputOneTuple()
      } else {
        val dataNotification = dataQueue.availableNotification()
        if (dataNotification.isDone) {
          // process input
          val data = dataQueue.dequeue()
          println(s"process input $data at step $totalValidStep")
          handleDataElement(data)
          println(s"finished process input at step $totalValidStep")
        } else {
          println(s"start waiting 3 futures at step $totalValidStep")
          CompletableFuture.anyOf(internalNotification, controlNotification, dataNotification).get()
          totalValidStep -= 1 // this round did nothing.
        }
      }
    }
  }

  private[this] def handleInternalMessage(internalCommand: InternalCommand): Unit ={
    internalCommand match {
      case DataProcessor.Shutdown(reason, completion) =>
        logManager.terminate()
        completion.complete(())
        dpThread.cancel(true) // interrupt
        dpThreadExecutor.shutdownNow() // destroy thread
        if(reason.isEmpty){
          throw new InterruptedException() // actively interrupt itself
        }else{
          throw reason.get
        }
      case DataProcessor.Checkpoint(networkSender, completion) =>
        // create checkpoint
        val chkpt = new SavedCheckpoint()
        chkpt.saveThread(this)
        chkpt.saveMessages(logManager.getUnackedMessages())
        // push to storage
        CheckpointHolder.addCheckpoint(actorId, totalValidStep, chkpt)
        // completion
        completion.complete(totalValidStep)
      case DataProcessor.NoOperation =>
        // do nothing
    }
  }

  private[this] def handleOperatorException(e: Throwable): Unit = {
    if (currentInputTuple.isLeft) {
      asyncRPCClient.send(
        LocalOperatorException(currentInputTuple.left.get, e),
        CONTROLLER
      )
    } else {
      asyncRPCClient.send(
        LocalOperatorException(ITuple("input exhausted"), e),
        CONTROLLER
      )
    }
    logger.warn(e.getLocalizedMessage + "\n" + e.getStackTrace.mkString("\n"))
    // invoke a pause in-place
    asyncRPCServer.execute(PauseWorker(), SELF)
  }

  /**
    * Called by skewed worker in Reshape when it has received the tuples from the helper
    * and is ready to output tuples.
    * The call comes from AcceptMutableStateHandler.
    *
    * @param iterator
    */
  def setCurrentOutputIterator(iterator: Iterator[ITuple]): Unit = {
    currentOutputIterator = iterator.map(t => (t, Option.empty))
  }

  private[this] def processControlCommand(
      payload: ControlPayload,
      from: ActorVirtualIdentity
  ): Unit = {
    determinantLogger.logDeterminant(ProcessControlMessage(payload, from))
    payload match {
      case invocation: ControlInvocation =>
        //logger.info("current total step = "+totalSteps+" recovery step = "+recoveryQueue.totalStep)
        asyncRPCServer.logControlInvocation(invocation, from)
        asyncRPCServer.receive(invocation, from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, from)
        asyncRPCClient.fulfillPromise(ret)
    }
  }

}
