package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LinkCompletedHandler.LinkCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LocalOperatorExceptionHandler.LocalOperatorException
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionStartedHandler.WorkerStateUpdated
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{LogManager, ProcessControlMessage, SenderActorChange}
import edu.uci.ics.amber.engine.architecture.messaginglayer.TupleToBatchConverter
import edu.uci.ics.amber.engine.architecture.recovery.{LocalRecoveryManager, ReplayGate}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor._
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.CheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{COMPLETED, PAUSED, READY, RUNNING, UNINITIALIZED}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayload
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


object DataProcessor{
  // 4 kinds of elements can be accepted by internal queue
  sealed trait DataElement

  case class InputTuple(from: ActorVirtualIdentity, tuple: ITuple) extends DataElement

  case class ControlElement(payload: ControlPayload, from: ActorVirtualIdentity)

  case class EndMarker(from: ActorVirtualIdentity) extends DataElement

  case class FinalizeLink(link:LinkIdentity) extends DataElement

  case class FinalizeOperator() extends DataElement
}


class DataProcessor( // dependencies:
                     operator: IOperatorExecutor, // core logic
                     asyncRPCClient: AsyncRPCClient, // to send controls
                     batchProducer: TupleToBatchConverter, // to send output tuples
                     val pauseManager: PauseManager, // to pause/resume
                     breakpointManager: BreakpointManager, // to evaluate breakpoints
                     stateManager: WorkerStateManager,
                     upstreamLinkStatus: UpstreamLinkStatus,
                     asyncRPCServer: AsyncRPCServer,
                     val logStorage: DeterminantLogStorage,
                     val logManager: LogManager,
                     val recoveryManager: LocalRecoveryManager,
                     val recoveryQueue: ReplayGate,
                     val actorId: ActorVirtualIdentity,
                     val inputToOrdinalMapping: Map[LinkIdentity, Int],
                     //  use two different types for the wire library to do dependency injection
                     // temporary workaround, will be refactored soon
                     val outputToOrdinalMapping: mutable.Map[LinkIdentity, Int],
                     dataQueue: ProactiveDeque[DataElement],
                     controlQueue:ProactiveDeque[ControlElement]
) extends AmberLogging {

  // initialize dp thread upon construction
  private val dpThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor
  private var dpThread: Future[_] = _
  def start(): Unit = {
    if (dpThread == null) {
      // TODO: setup context
      stateManager.assertState(UNINITIALIZED)
      // operator.context = new OperatorContext(new TimeService(logManager))
      stateManager.transitTo(READY)
      if (!recoveryQueue.recoveryCompleted) {
        recoveryQueue.registerOnEnd(() => recoveryManager.End())
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

  /**
    * Map from Identifier to input number. Used to convert the Identifier
    * to int when adding sender info to the queue.
    * We also keep track of the upstream actors so that we can emit
    * EndOfAllMarker when all upstream actors complete their job
    */
  private val inputMap = new mutable.HashMap[ActorVirtualIdentity, LinkIdentity]
  private val determinantLogger = logManager.getDeterminantLogger

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
    else if (!inputToOrdinalMapping.contains(inputLink)) 0
    else inputToOrdinalMapping(inputLink)
  }

  def getOutputLinkByPort(outputPort: Option[Int]): Option[List[LinkIdentity]] = {
    if (outputPort.isEmpty)
      return Option.empty
    val outLinks = outputToOrdinalMapping.filter(p => p._2 == outputPort.get).keys.toList
    Option.apply(outLinks)
  }

  // dp thread stats:
  // TODO: add another variable for recovery index instead of using the counts below.
  private var inputTupleCount = 0L
  private var outputTupleCount = 0L
  private var currentInputTuple: Either[ITuple, InputExhausted] = _
  private var currentInputActor: ActorVirtualIdentity = _
  private var currentOutputIterator: Iterator[(ITuple, Option[Int])] = _
  var totalValidStep = 0L

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

  def shutdown(): Unit = {
    dpThread.cancel(true) // interrupt
    dpThreadExecutor.shutdownNow() // destroy thread
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
      stateManager.transitTo(PAUSED)
    } else {
      outputTupleCount += 1
      val outLinks = getOutputLinkByPort(outputPortOpt)
      if (outLinks.isEmpty) {
        batchProducer.passTupleToDownstream(outputTuple, Option.empty)
      } else {
        outLinks.get.foreach(outLink => {
          batchProducer.passTupleToDownstream(outputTuple, Option.apply(outLink))
        })
      }
    }
  }

  private[this] def handleDataElement(
                                       dataElement: DataElement
  ): Unit = {
    dataElement match {
      case InputTuple(from, tuple) =>
        if (stateManager.getCurrentState == READY) {
          stateManager.transitTo(RUNNING)
          asyncRPCClient.send(
            WorkerStateUpdated(stateManager.getCurrentState),
            CONTROLLER
          )
        }
        if (currentInputActor != from) {
          determinantLogger.logDeterminant(SenderActorChange(from), totalValidStep)
          currentInputActor = from
        }
        processInputTuple(Left(tuple))
      case EndMarker(from) =>
        if (currentInputActor != from) {
          determinantLogger.logDeterminant(SenderActorChange(from), totalValidStep)
          currentInputActor = from
        }
        upstreamLinkStatus.markWorkerEOF(from)
        val currentLink = getInputLink(currentInputActor)
        if (upstreamLinkStatus.isAllEOF) {
          dataQueue.addFirst(FinalizeOperator())
        }
        if (upstreamLinkStatus.isLinkEOF(currentLink)) {
          processInputTuple(Right(InputExhausted()))
          dataQueue.addFirst(FinalizeLink(currentLink))
        }
      case FinalizeLink(link) =>
        asyncRPCClient.send(LinkCompleted(link), CONTROLLER)
      case FinalizeOperator() =>
        batchProducer.emitEndOfUpstream()
        // Send Completed signal to worker actor.
        logger.info(s"$operator completed")
        operator.close() // close operator
        asyncRPCClient.send(WorkerExecutionCompleted(), CONTROLLER)
        stateManager.transitTo(COMPLETED)
    }
  }

  @throws[Exception]
  private[this] def runDPThreadMainLogicNew(): Unit = {
    // main DP loop
    while (true) {

      val internalNotification = ???
      if(internalNotification.isDone){
        processInternalMessage()
      }


      recoveryQueue.prepareInput(totalValidStep)
      totalValidStep += 1
      val controlNotification = controlQueue.availableNotification()
      if (controlNotification.isDone) {
        // process control
        val control = controlQueue.dequeue()
        processControlCommand(control.payload, control.from)
      } else if (pauseManager.isPaused()) {
        controlNotification.get()
        totalValidStep -= 1 // this round did nothing.
      } else if (currentOutputIterator.hasNext) {
        // send output
        outputOneTuple()
      } else {
        val dataNotification = dataQueue.availableNotification()
        if (dataNotification.isDone) {
          // process input
          handleDataElement(dataQueue.dequeue())
        } else {
          CompletableFuture.anyOf(controlNotification, dataNotification).get()
          totalValidStep -= 1 // this round did nothing.
        }
      }
      determinantLogger.updateStep(totalValidStep)
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
    payload match {
      case invocation: ControlInvocation =>
        if(!invocation.command.isInstanceOf[TakeCheckpoint]){
          determinantLogger.logDeterminant(ProcessControlMessage(payload, from), totalValidStep)
        }
        //logger.info("current total step = "+totalSteps+" recovery step = "+recoveryQueue.totalStep)
        asyncRPCServer.logControlInvocation(invocation, from)
        asyncRPCServer.receive(invocation, from)
      case ret: ReturnInvocation =>
        determinantLogger.logDeterminant(ProcessControlMessage(payload, from), totalValidStep)
        asyncRPCClient.logControlReply(ret, from)
        asyncRPCClient.fulfillPromise(ret)
    }
  }

}
