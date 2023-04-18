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
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort, OutputManager}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{COMPLETED, PAUSED, READY, RUNNING, UNINITIALIZED}
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor.{DPOutputIterator, FinalizeLink, FinalizeOperator}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DataFrame, DataPayload, EndOfUpstream, EpochMarker, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF, SOURCE_STARTER_ACTOR, SOURCE_STARTER_OP}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity, LinkIdentity}
import edu.uci.ics.amber.engine.common.{AmberLogging, IOperatorExecutor, ISourceOperatorExecutor, InputExhausted}
import edu.uci.ics.amber.error.ErrorUtils.safely

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
    actorId: ActorVirtualIdentity, determinantLogger: DeterminantLogger
) extends AmberProcessor(actorId, determinantLogger)
    with Serializable {

  // outer dependencies
  @transient
  private[processing] var operator: IOperatorExecutor = _
  @transient
  private[processing] var actorContext: ActorContext = _
  @transient
  private[processing] var internalQueue:WorkerInternalQueue = _
  @transient
  private[processing] var dpThread:DPThread = _

  def initDP(
                  operator: IOperatorExecutor, // core logic
                  currentOutputIterator: Iterator[(ITuple, Option[Int])],
                  actorContext: ActorContext,
                  logManager: LogManager,
                  internalQueue: WorkerInternalQueue
  ): Unit = {
    init(logManager)
    this.operator = operator
    this.outputIterator.setTupleOutput(currentOutputIterator)
    this.actorContext = actorContext
    this.internalQueue = internalQueue
    this.pauseManager.initialize(internalQueue)
    this.epochManager.initialize(this)
    new DataProcessorRPCHandlerInitializer(this)
  }

  def attachDPThread(dPThread: DPThread): Unit ={
    this.dpThread = dPThread
  }

  def getOperatorId: LayerIdentity = VirtualIdentityUtils.getOperator(actorId)
  def getWorkerIndex: Int = VirtualIdentityUtils.getWorkerIndex(actorId)

  // inner dependencies
  // 4. pause manager
  lazy private[processing] val pauseManager: PauseManager = wire[PauseManager]
  // 5. breakpoint manager
  lazy private[processing] val breakpointManager: BreakpointManager = new BreakpointManager(asyncRPCClient)
  // 6. upstream links
  lazy val upstreamLinkStatus: UpstreamLinkStatus = wire[UpstreamLinkStatus]
  // 7. state manager
  lazy private[processing] val stateManager: WorkerStateManager = new WorkerStateManager()
  // 8. batch producer
  lazy private[processing] val outputManager: OutputManager =
    new OutputManager(actorId, outputPort)
  // 9. epoch manager
  lazy private[processing] val epochManager: EpochManager = new EpochManager()

  private[processing] var outputIterator: DPOutputIterator = new DPOutputIterator()
  private[processing] var inputBatch: Array[ITuple] = _
  private[processing] var currentInputIdx = -1
  private[processing] var currentBatchChannel: ChannelEndpointID = _

  // dp thread stats:
  protected var inputTupleCount = 0L
  protected var outputTupleCount = 0L

  def registerInput(identifier: ActorVirtualIdentity, input: LinkIdentity): Unit = {
    upstreamLinkStatus.registerInput(identifier, input)
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

  /** process currentInputTuple through operator logic.
    * this function is only called by the DP thread
    *
    * @return an iterator of output tuples
    */
  def processInputTuple(tuple: Either[ITuple, InputExhausted]): Unit = {
    try {
      outputIterator.setTupleOutput(
        operator.processTuple(
          tuple,
          getInputPort(currentInputChannel.endpointWorker),
          pauseManager,
          asyncRPCClient
        )
      )
      if (tuple.isLeft) {
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
  def outputOneTuple(): Unit = {
    outputManager.adaptiveBatchingMonitor.enableAdaptiveBatching(actorContext)
    var out: (ITuple, Option[Int]) = null
    try {
      out = outputIterator.next()
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

    if (outputTuple == null) return

    outputTuple match {
      case FinalizeOperator() =>
        outputManager.emitEndOfUpstream()
        // Send Completed signal to worker actor.
        logger.info(s"$operator completed at step = ${determinantLogger.getStep} outputted = $outputTupleCount")
        operator.close() // close operator
        outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
        stateManager.transitTo(COMPLETED)
        asyncRPCClient.send(WorkerExecutionCompleted(determinantLogger.getStep), CONTROLLER)
      case FinalizeLink(link) =>
        logger.info(s"process FinalizeLink message at step = ${determinantLogger.getStep}")
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

  def hasUnfinishedInput: Boolean = inputBatch != null && currentInputIdx+1 < inputBatch.length

  def hasUnfinishedOutput: Boolean = outputIterator.hasNext

  def continueDataProcessing(): Unit ={
    updateInputChannelThenDoLogging(currentBatchChannel)
    if (hasUnfinishedOutput) {
      outputOneTuple()
    } else {
      currentInputIdx += 1
      processInputTuple(Left(inputBatch(currentInputIdx)))
    }
  }

  private[this] def initBatch(channel:ChannelEndpointID, batch:Array[ITuple]): Unit ={
    currentBatchChannel = channel
    inputBatch = batch
    currentInputIdx = 0
  }

  private[processing] def getCurrentInputTuple: ITuple = {
    if(inputBatch == null){
      null
    }else if(inputBatch.isEmpty) {
      ITuple("Input Exhausted")
    }else {
      inputBatch(currentInputIdx)
    }
  }

  def processDataPayload(
      channel:ChannelEndpointID,
      dataPayload: DataPayload
  ): Unit = {
    updateInputChannelThenDoLogging(channel)
    dataPayload match {
      case DataFrame(tuples) =>
        stateManager.conditionalTransitTo(READY, RUNNING, ()=> {
          asyncRPCClient.send(
            WorkerStateUpdated(stateManager.getCurrentState),
            CONTROLLER
          )
        })
        initBatch(channel, tuples)
        processInputTuple(Left(inputBatch(currentInputIdx)))
      case EndOfUpstream() =>
        val currentLink = upstreamLinkStatus.getInputLink(channel.endpointWorker)
        upstreamLinkStatus.markWorkerEOF(channel.endpointWorker, currentLink)
        if (upstreamLinkStatus.isLinkEOF(currentLink)) {
          initBatch(channel, Array.empty)
          processInputTuple(Right(InputExhausted()))
          logger.info(
            s"$currentLink completed, append FinalizeLink message at step = ${determinantLogger.getStep}"
          )
          outputIterator.appendSpecialTupleToEnd(FinalizeLink(currentLink))
        }
        if (upstreamLinkStatus.isAllEOF) {
          logger.info(
            s"operator completed, append FinalizeOperator message at step = ${determinantLogger.getStep}"
          )
          outputIterator.appendSpecialTupleToEnd(FinalizeOperator())
        }
      case marker: EpochMarker =>
        epochManager.processEpochMarker(channel.endpointWorker, marker)
    }
  }

  private[this] def handleOperatorException(e: Throwable): Unit = {
    asyncRPCClient.send(
      LocalOperatorException(getCurrentInputTuple, e),
      CONTROLLER
    )
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

}
