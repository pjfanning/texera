package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.actor.ActorContext
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.LinkCompletedHandler.LinkCompleted
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.LocalOperatorExceptionHandler.LocalOperatorException
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.WorkerExecutionStartedHandler.WorkerStateUpdated
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OrdinalMapping
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort, OutputManager}
import edu.uci.ics.amber.engine.architecture.recovery.LocalRecoveryManager
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{ControlElement, DataElement, EndMarker, InputEpochMarker, InputTuple}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{COMPLETED, PAUSED, READY, RUNNING, UNINITIALIZED}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor.{DPOutputIterator, FinalizeLink, FinalizeOperator}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipFaultTolerance
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF, SOURCE_STARTER_ACTOR, SOURCE_STARTER_OP}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity, LinkIdentity}
import edu.uci.ics.amber.engine.common.{AmberLogging, IOperatorExecutor, ISourceOperatorExecutor, InputExhausted}
import edu.uci.ics.amber.error.ErrorUtils.safely
import edu.uci.ics.texera.workflow.operators.nn.DNNOpExec

import java.util.concurrent.{ExecutorService, Executors, Future}
import scala.collection.mutable

object DataProcessor {

  class SpecialDataTuple extends ITuple {
    override def length: Int = 0

    override def get(i: Int): Any = null

    override def toArray(): Array[Any] = Array.empty
  }
  case class FinalizeLink(link: LinkIdentity) extends SpecialDataTuple
  case class FinalizeOperator() extends SpecialDataTuple

  class DPOutputIterator extends Iterator[(ITuple, Option[Int])] {
    val queue = new mutable.Queue[(ITuple, Option[Int])]
    @transient var outputIter: Iterator[(ITuple, Option[Int])] = Iterator.empty

    def setTupleOutput(outputIter: Iterator[(ITuple, Option[Int])]): Unit = {
      this.outputIter = outputIter
    }

    override def hasNext: Boolean = outputIter.hasNext || queue.nonEmpty

    override def next(): (ITuple, Option[Int]) = {
      if (outputIter.hasNext) {
        outputIter.next()
      } else {
        queue.dequeue()
      }
    }

    def appendSpecialTupleToEnd(tuple: ITuple): Unit = {
      queue.enqueue((tuple, None))
    }
  }

}

class DataProcessor( // meta dependencies:
    val ordinalMapping: OrdinalMapping,
    val actorId: ActorVirtualIdentity
) extends AmberLogging
    with Serializable {

  // outer dependencies
  @transient
  private[processing] var internalQueue: WorkerInternalQueue = _
  @transient
  private[processing] var logStorage: DeterminantLogStorage = _
  @transient
  private[processing] var logManager: LogManager = _
  @transient
  private[processing] var recoveryManager: LocalRecoveryManager = _
  @transient
  private[processing] var actorContext: ActorContext = _
  @transient
  private[processing] var operator: IOperatorExecutor = _


  def initialize(
                  operator: IOperatorExecutor, // core logic
                  currentOutputIterator: Iterator[(ITuple, Option[Int])],
                  internalQueue: WorkerInternalQueue,
                  logStorage: DeterminantLogStorage,
                  logManager: LogManager,
                  recoveryManager: LocalRecoveryManager,
                  actorContext: ActorContext
  ): Unit = {
    this.operator = operator
    this.outputIterator.setTupleOutput(currentOutputIterator)
    this.internalQueue = internalQueue
    this.logStorage = logStorage
    this.logManager = logManager
    this.recoveryManager = recoveryManager
    this.actorContext = actorContext
    this.pauseManager.initialize(this)
    this.epochManager.initialize(this)
    this.rpcInitializer = new DataProcessorRPCHandlerInitializer(this)
  }

  def getOperatorId: LayerIdentity = VirtualIdentityUtils.getOperator(actorId)
  def getWorkerIndex: Int = VirtualIdentityUtils.getWorkerIndex(actorId)

  def outputPayload(
                         to: ActorVirtualIdentity,
                         msg:WorkflowFIFOMessage
                       ): Unit = {
    logManager.sendCommitted(SendRequest(to, msg))
  }

  // inner dependencies
  // 1. Unified data/control Output
  lazy private[processing] val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  // 3. RPC Layer
  lazy private[processing] val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  lazy private[processing] val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)
  // 4. pause manager
  lazy private[processing] val pauseManager: PauseManager = wire[PauseManager]
  // 5. breakpoint manager
  lazy private[processing] val breakpointManager: BreakpointManager = wire[BreakpointManager]
  // 6. upstream links
  lazy val upstreamLinkStatus: UpstreamLinkStatus = wire[UpstreamLinkStatus]
  // 7. state manager
  lazy private[processing] val stateManager: WorkerStateManager = new WorkerStateManager()
  // 8. batch producer
  lazy private[processing] val outputManager: OutputManager =
    new OutputManager(actorId, outputPort)
  // 9. epoch manager
  lazy private[processing] val epochManager: EpochManager = new EpochManager()
  // rpc handlers
  @transient
  private[this] var rpcInitializer: DataProcessorRPCHandlerInitializer = _

  lazy private[processing] val determinantLogger: DeterminantLogger =
    logManager.getDeterminantLogger

  private[processing] var outputIterator: DPOutputIterator = new DPOutputIterator()

  // dp thread stats:
  // TODO: add another variable for recovery index instead of using the counts below.
  protected var inputTupleCount = 0L
  protected var outputTupleCount = 0L
  protected var currentInputTuple: Either[ITuple, InputExhausted] = _
  protected var currentInputActor: ActorVirtualIdentity = _
  var totalValidStep = 0L
  var totalTimeSpent = 0L

  // initialize dp thread upon construction
  @transient
  private[processing] var dpThreadExecutor: ExecutorService = _
  @transient
  private[processing] var dpThread: Future[_] = _
  def start(): Unit = {
    if (dpThreadExecutor != null) {
      return
    }
    dpThreadExecutor = Executors.newSingleThreadExecutor
    if (stateManager.getCurrentState == UNINITIALIZED) {
      stateManager.transitTo(READY)
    }
    if (dpThread == null) {
      // TODO: setup context
      // operator.context = new OperatorContext(new TimeService(logManager))
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
    upstreamLinkStatus.registerInput(identifier, input)
    internalQueue.registerInput(identifier.name)
  }

  def getInputPort(identifier: ActorVirtualIdentity): Int = {
    val inputLink = upstreamLinkStatus.getInputLink(identifier)
    if (inputLink.from == SOURCE_STARTER_OP) 0 // special case for source operator
    else if (!ordinalMapping.input.contains(inputLink)) 0
    else ordinalMapping.input(inputLink)
  }

  def getOutputLinkByPort(outputPort: Option[Int]): List[LinkIdentity] = {
    if (outputPort.isEmpty) {
      ordinalMapping.output.keySet.toList
    } else {
      ordinalMapping.output.filter(p => p._2 == outputPort.get).keys.toList
    }
  }

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
  private[this] def processInputTuple(tuple: Either[ITuple, InputExhausted]): Unit = {
    determinantLogger.stepIncrement()
    currentInputTuple = tuple
//    if (operator.isInstanceOf[DNNOpExec]) {
//      logger.info(s"input $tuple at step = $totalValidStep")
//    }
    try {
      outputIterator.setTupleOutput(
        operator.processTuple(
          currentInputTuple,
          getInputPort(currentInputActor),
          pauseManager,
          asyncRPCClient
        )
      )
      if (currentInputTuple.isLeft) {
        inputTupleCount += 1
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
    outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(actorContext)
    determinantLogger.stepIncrement()
    var out: (ITuple, Option[Int]) = null
    try {
      out = outputIterator.next
    } catch safely {
      case e =>
        // invalidate current output tuple
        out = null
        // also invalidate outputIterator
        outputIterator.setTupleOutput(Iterator.empty)
        // forward input tuple to the user and pause DP thread
        handleOperatorException(e)
    }
    if (out == null) return

    val (outputTuple, outputPortOpt) = out
//    if (operator.isInstanceOf[DNNOpExec]) {
//      logger.info(s"output null = ${outputTuple == null} at step = $totalValidStep")
//    }

    if (outputTuple == null) return

    outputTuple match {
      case FinalizeOperator() =>
        outputManager.emitEndOfUpstream()
        // Send Completed signal to worker actor.
        logger.info(s"$operator completed at step = $totalValidStep outputted = $outputTupleCount")
        operator.close() // close operator
        outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
        stateManager.transitTo(COMPLETED)
        asyncRPCClient.send(WorkerExecutionCompleted(totalValidStep), CONTROLLER)
      case FinalizeLink(link) =>
        logger.info(s"process FinalizeLink message at step = $totalValidStep")
        if(link != null && link.from != SOURCE_STARTER_OP){
          asyncRPCClient.send(LinkCompleted(link), CONTROLLER)
        }
      case _ =>
        if (breakpointManager.evaluateTuple(outputTuple)) {
          pauseManager.pause(UserPause)
          outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
          stateManager.transitTo(PAUSED)
        } else {
          outputTupleCount += 1
          // println(s"send output $outputTuple at step $totalValidStep")
          val outLinks = getOutputLinkByPort(outputPortOpt)
          outLinks.foreach(link => outputManager.passTupleToDownstream(outputTuple, link))
        }
    }
  }

  private[this] def handleDataElement(
      dataElement: DataElement
  ): Unit = {
    // println(s"process input $dataElement at step $totalValidStep")
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
          determinantLogger.setCurrentSender(from)
          currentInputActor = from
        }
        processInputTuple(Left(tuple))
      case EndMarker(from) =>
        if (currentInputActor != from) {
          determinantLogger.setCurrentSender(from)
          currentInputActor = from
        }
        val currentLink = upstreamLinkStatus.getInputLink(currentInputActor)
        upstreamLinkStatus.markWorkerEOF(from, currentLink)
        if (upstreamLinkStatus.isLinkEOF(currentLink)) {
          processInputTuple(Right(InputExhausted()))
          logger.info(
            s"$currentLink completed, append FinalizeLink message at step = $totalValidStep"
          )
          outputIterator.appendSpecialTupleToEnd(FinalizeLink(currentLink))
        }
        if (upstreamLinkStatus.isAllEOF) {
          logger.info(
            s"operator completed, append FinalizeOperator message at step = $totalValidStep"
          )
          outputIterator.appendSpecialTupleToEnd(FinalizeOperator())
        }
      case InputEpochMarker(from, epochMarker) =>
        epochManager.processEpochMarker(from, epochMarker)
    }
  }

  @throws[Exception]
  private[this] def runDPThreadMainLogicNew(): Unit = {
    // main DP loop
    while (true) {
      if (outputIterator.hasNext && !pauseManager.isPaused()) {
        val input = internalQueue.peek(totalValidStep)
        val startTime = System.nanoTime()
        input match {
          case Some(value) =>
            value match {
              case _: DataElement =>
                outputOneTuple()
              case _: ControlElement =>
                val control = internalQueue.take(totalValidStep).asInstanceOf[ControlElement]
                processControlCommand(control.payload, control.from)
            }
          case None =>
            outputOneTuple()
        }
        totalTimeSpent += System.nanoTime() - startTime
      } else {
        // TODO: find and fix null bug here.
        val input = internalQueue.take(totalValidStep)
        val startTime = System.nanoTime()
        input match {
          case element: DataElement =>
            handleDataElement(element)
          case ControlElement(payload, from) =>
            processControlCommand(payload, from)
        }
        totalTimeSpent += System.nanoTime() - startTime
      }
      totalValidStep += 1
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
    outputIterator.setTupleOutput(iterator.map(t => (t, Option.empty)))
  }

  private[this] def processControlCommand(
      payload: ControlPayload,
      from: ActorVirtualIdentity
  ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
    payload match {
      case invocation: ControlInvocation =>
        if (!invocation.command.isInstanceOf[SkipFaultTolerance]) {
          determinantLogger.logDeterminant(ProcessControlMessage(invocation, from))
        }
        asyncRPCServer.logControlInvocation(invocation, from, totalValidStep)
        asyncRPCServer.receive(invocation, from)
        if (invocation.command.isInstanceOf[SkipFaultTolerance]) {
          totalValidStep -= 1 // negate the effect, must do it after processing control message
        }
      case ret: ReturnInvocation =>
        determinantLogger.logDeterminant(ProcessControlMessage(ret, from))
        asyncRPCClient.logControlReply(ret, from, totalValidStep)
        asyncRPCClient.fulfillPromise(ret)
    }
  }

}
