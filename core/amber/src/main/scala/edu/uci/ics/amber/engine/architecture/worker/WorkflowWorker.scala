package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import akka.pattern.StatusReply.Ack
import akka.serialization.SerializationExtension
import akka.util.Timeout
import com.softwaremill.macwire.wire
import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, OrdinalMapping}
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CreditMonitor, CreditMonitorImpl, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.{PendingCheckpoint, ReplayInputRecorder}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.ControlElement
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{CheckInitialized, ReplaceRecoveryQueue, getWorkerLogName}
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.NoOpHandler.NoOp
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ShutdownDPHandler.ShutdownDP
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, IOperatorExecutor}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{EndMarker, InputEpochMarker, InputTuple}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.{CheckpointStats, InitialCheckpointStats, StartCheckpoint}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{CheckpointCompleted, ContinueReplay, ContinueReplayTo, ControlPayload, CreditRequest, DataFrame, DataPayload, EndOfUpstream, EpochMarker, GetOperatorInternalState, ResendOutputTo, SnapshotMarker, TakeLocalCheckpoint, UpdateRecoveryStatus, WorkflowFIFOMessage, WorkflowFIFOMessagePayload, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}

import java.util.concurrent.CompletableFuture
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef,
      supportFaultTolerance: Boolean,
      stateRestoreConfig: StateRestoreConfig
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig,
        parentNetworkCommunicationActorRef,
        supportFaultTolerance,
        stateRestoreConfig
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  case class ReplaceRecoveryQueue(syncFuture: CompletableFuture[Unit])

  case class CheckInitialized()

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    supportFaultTolerance: Boolean,
    restoreConfig: StateRestoreConfig
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, supportFaultTolerance) {
  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  var dataProcessor: DataProcessor = wire[DataProcessor]
  lazy val operator: IOperatorExecutor =
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  logger.info(s"Worker:$actorId = ${context.self}")
  lazy val inputPort: NetworkInputPort = new NetworkInputPort(this.actorId, this.handlePayload)
  val creditMonitor = new CreditMonitorImpl()
  var inputQueue: WorkerInternalQueue = _
  val pendingCheckpoints = new mutable.HashMap[Long, PendingCheckpoint]()
  val inputPayloadRecorder = new ReplayInputRecorder()
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  if (parentNetworkCommunicationActorRef != null) {
    parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
  }

  override def getLogName: String = getWorkerLogName(actorId)

  def getSenderCredits(sender: ActorVirtualIdentity) = {
    creditMonitor.getSenderCredits(sender)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
  }

  def reloadState(chkpt:SavedCheckpoint): Iterator[(ITuple, Option[Int])] ={
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
    inputQueue = chkpt.load("internalQueueState")
    logger.info("input queue restored")
    operator match {
      case support: CheckpointSupport =>
        chkpt.pointerToCompletion match {
          case Some(value) =>
            outputIter =
              support.deserializeState(CheckpointHolder.getCheckpoint(actorId, value))
          case None => outputIter = support.deserializeState(chkpt)
        }
      case _ =>
    }
    logger.info("operator restored")
    dataProcessor = chkpt.load("controlState")
    logger.info(s"DP restored ${dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
    outputIter
  }

  def insertPayloads(channelData: mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]): Unit ={
    channelData.foreach{
      case (channelId, payloads) =>
        payloads.foreach(x => handlePayload(channelId, x))
    }
  }

  override def receive: Receive = {
    // load from checkpoint if available
    var outputIter: Iterator[(ITuple, Option[Int])] = Iterator.empty
    var messageToReprocess:mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.ArrayBuffer[WorkflowFIFOMessagePayload]] = mutable.HashMap()
    var unprocessedMessages:mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.ArrayBuffer[WorkflowFIFOMessagePayload]] = mutable.HashMap()
    try {
      restoreConfig.fromCheckpoint match {
        case None | Some(0) =>
          inputQueue = new WorkerInternalQueueImpl(creditMonitor)
        case Some(alignment) =>
          val chkpt = CheckpointHolder.getCheckpoint(actorId, alignment)
          logger.info("checkpoint found, start loading")
          val startLoadingTime = System.currentTimeMillis()
          //restore state from checkpoint: can be in either replaying or normal processing
          chkpt.attachSerialization(SerializationExtension(context.system))
          val fifoStateAtCheckpointTime:Map[(ActorVirtualIdentity, Boolean), Long] = chkpt.load("fifoState")
          var currentCheckpoint = chkpt
          while(currentCheckpoint.prevCheckpoint.isDefined){
            messageToReprocess = currentCheckpoint.load("processedMessages")
            currentCheckpoint = CheckpointHolder.getCheckpoint(actorId, currentCheckpoint.prevCheckpoint.get)
          }
          // reload state from the last concrete chkpt
          if(currentCheckpoint.has("internalQueueState")){
            outputIter = reloadState(currentCheckpoint)
          }
          unprocessedMessages = chkpt.load("unprocessedData")
          val additionalFifoState = unprocessedMessages.mapValues(_.size.toLong)
          val fifoState = fifoStateAtCheckpointTime.map{
            case (k, v) =>
              (k, v + additionalFifoState.getOrElse(k, 0L))
          }
          inputPort.setFIFOState(fifoState)
          logger.info("fifo state restored")
          logger.info(
            s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime) / 1000f}s"
          )
      }
      insertPayloads(messageToReprocess)
      insertPayloads(unprocessedMessages)
      // set replay
      restoreConfig.replayTo match {
        case Some(replayTo) =>
          val queue = inputQueue match {
            case impl: RecoveryInternalQueueImpl => impl
            case impl: WorkerInternalQueueImpl   =>
              // convert to replay queue if we have normal queue
              val newQueue = new RecoveryInternalQueueImpl(creditMonitor)
              impl.dataQueues.foreach(x => {
                x._2.enable(true)
                newQueue.registerInput(x._1)
              })
              var numDataTupleRestored = 0
              var numControlRestored = 0
              while (impl.peek(0).isDefined) {
                impl.take(0) match {
                  case element: WorkerInternalQueue.DataElement =>
                    newQueue.enqueueData(element)
                    numDataTupleRestored += 1
                  case ctrl: ControlElement =>
                    newQueue.enqueueCommand(ctrl)
                    numControlRestored += 1
                }
              }
              logger.info(
                s"Worker Queue convert to Recovery Queue: ${numControlRestored} control restored, ${numDataTupleRestored} data restored"
              )
              inputQueue = newQueue
              newQueue
          }
          queue.initialize(
            logStorage.getReader.mkLogRecordIterator(),
            dataProcessor.totalValidStep,
            () => {
              val syncFuture = new CompletableFuture[Unit]()
              context.self ! ReplaceRecoveryQueue(syncFuture)
              syncFuture.get()
            }
          )
          logger.info("set replay to " + replayTo)
          queue.setReplayTo(replayTo, () => {
            logger.info("replay completed!")
            context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
          })
          recoveryManager.registerOnStart(() => {}
            // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(true))
          )
          recoveryManager.setNotifyReplayCallback(() => {}
            // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
          )
          recoveryManager.Start()
          recoveryManager.registerOnEnd(() => {
            logger.info("recovery complete! restoring stashed inputs...")
            logManager.terminate()
            logStorage.cleanPartiallyWrittenLogFile()
            logManager.setupWriter(logStorage.getWriter)
            logger.info("stashed inputs restored!")
            // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
          })
          val fifoState = recoveryManager.getFIFOState(logStorage.getReader.mkLogRecordIterator())
          inputPort.overwriteControlFIFOSeqNum(fifoState)
        case None =>
          inputQueue match {
            case impl: RecoveryInternalQueueImpl =>
              replaceRecoveryQueue()
            case impl: WorkerInternalQueueImpl =>
            // do nothing
          }
      }
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    dataProcessor.initialize(
      operator,
      outputIter,
      inputQueue,
      logStorage,
      logManager,
      recoveryManager,
      context
    )
    dataProcessor.start()
    receiveAndProcessMessages
  }

  def replaceRecoveryQueue(): Unit = {
    val oldInputQueue = inputQueue.asInstanceOf[RecoveryInternalQueueImpl]
    inputQueue = new WorkerInternalQueueImpl(creditMonitor)
    // add unprocessed inputs into new queue
    oldInputQueue.registeredInputs.foreach(inputQueue.registerInput)
    oldInputQueue.getAllStashedInputs.foreach(inputQueue.enqueueData)
    oldInputQueue.getAllStashedControls.foreach(inputQueue.enqueueCommand)
  }

  def receiveAndProcessMessages: Receive =
    acceptInitializationMessage orElse acceptDirectInvocations orElse forwardResendRequest orElse disallowActorRefRelatedMessages orElse {
      case ReplaceRecoveryQueue(sync) =>
        logger.info("replace recovery queue with normal queue")
        replaceRecoveryQueue()
        // unblock sync future on DP
        sync.complete(())
        logger.info("replace queue done!")
      case WorkflowRecoveryMessage(from, TakeLocalCheckpoint(cutoffs)) =>
        val startTime = System.currentTimeMillis()
        val syncFuture = new CompletableFuture[Long]()
        val chkpt = new SavedCheckpoint()
        chkpt.attachSerialization(SerializationExtension(context.system))
        logger.info("start to take local checkpoint")
        chkpt.save(
          "dataFifoState",
          dataInputPort.getFIFOState
        )
        chkpt.save(
          "controlFifoState",
          controlInputPort.getFIFOState
        )
        inputQueue.enqueueSystemCommand(TakeCheckpoint(cutoffs, chkpt, syncFuture))
        sender ! syncFuture.get()
        logger.info(
          s"local checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000f}s"
        )
      case WorkflowRecoveryMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case WorkflowRecoveryMessage(from, ContinueReplayTo(index)) =>
        assert(inputQueue.isInstanceOf[RecoveryInternalQueueImpl])
        logger.info("set replay to " + index)
        inputQueue.asInstanceOf[RecoveryInternalQueueImpl].setReplayTo(index, () => {
          logger.info("replay completed!")
          context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
        })
        inputQueue.enqueueSystemCommand(NoOp()) //kick start replay process
      case NetworkMessage(id, workflowMsg @ WorkflowFIFOMessage(from, _, _, payload)) =>
        sender ! NetworkAck(id, Some(getSenderCredits(from)))
        if(payload.isInstanceOf[SnapshotMarker]){
          logger.info(s"receive marker $payload from $from")
        }
        inputPort.handleMessage(workflowMsg)
      case NetworkMessage(id, CreditRequest(from)) =>
        sender ! NetworkAck(id, Some(getSenderCredits(from)))
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handlePayload((SELF, false), invocation)
  }

  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      sender ! Ack
  }

  def handlePayload(channelId:(ActorVirtualIdentity, Boolean), payload: WorkflowFIFOMessagePayload): Unit = {
    val (from, _) = channelId
    inputPayloadRecorder.recordPayload(channelId, payload)
    pendingCheckpoints.values.foreach(_.recordInput(channelId, payload))
    payload match {
      case marker: SnapshotMarker =>
        logger.info(s"process marker $marker")
        val completed = pendingCheckpoints
          .getOrElseUpdate(marker.id, new PendingCheckpoint(inputPort, actorId, marker, startCheckpoint, finalizeCheckpoint))
          .acceptSnapshotMarker(channelId)
        if(completed) {
          pendingCheckpoints.remove(marker.id)
        }
      case DataFrame(payload) =>
        payload.foreach { i =>
          inputQueue.enqueueData(InputTuple(from, i))
        }
      case EndOfUpstream() =>
        inputQueue.enqueueData(EndMarker(from))
      case marker @ EpochMarker(_, _, _) =>
        inputQueue.enqueueData(InputEpochMarker(from, marker))
      case controlCommand: ControlPayload =>
        inputQueue.enqueueCommand(ControlElement(controlCommand, from))
      case _ =>
        throw new WorkflowRuntimeException(s"cannot accept payload: $payload")
    }
  }

  def startCheckpoint(marker:SnapshotMarker): (SavedCheckpoint, InitialCheckpointStats) ={
    logger.info("calling startCheckpoint")
    val ser = SerializationExtension(context.system)
    val chkpt = new SavedCheckpoint()
    chkpt.attachSerialization(ser)
    var inputSavingTime = 0L
    if(!marker.involved.contains(actorId)){
      //save channel data
      val input = inputPayloadRecorder.getRecordedInputForReplay
      inputSavingTime = input.values.map(_.size).sum
      if(!marker.estimation){
        chkpt.save("processedData",input)
      }
    }
    if(!marker.estimation){
      // save state
      chkpt.save("fifoState", inputPort.getFIFOState)
    }
    val sync = new CompletableFuture[InitialCheckpointStats]()
    inputQueue.enqueueSystemCommand(StartCheckpoint(marker,chkpt, sync))
    val stats = sync.get()
    inputPayloadRecorder.clearAll()
    (chkpt, stats.copy(saveProcessedInputTime = inputSavingTime))
  }

  def finalizeCheckpoint(pendingCheckpoint: PendingCheckpoint): Unit ={
    val startCheckpointInputTime = System.currentTimeMillis()
    val chkpt = pendingCheckpoint.chkpt
    val dataToTake = pendingCheckpoint.dataToTake
    val initialCheckpointStats = pendingCheckpoint.initialCheckpointStats
    val checkpointId = pendingCheckpoint.marker.id
    val checkpointStats = if(!pendingCheckpoint.marker.estimation){
      chkpt.save("unprocessedData", dataToTake)
      chkpt.prevCheckpoint = CheckpointHolder.findLastCheckpointOf(actorId, initialCheckpointStats.alignment)
      CheckpointHolder.addCheckpoint(actorId, initialCheckpointStats.alignment, chkpt)
      CheckpointStats(checkpointId, initialCheckpointStats, startCheckpointInputTime - pendingCheckpoint.startAlignmentTime, System.currentTimeMillis() - startCheckpointInputTime, chkpt.size(), false)
    }else{
      CheckpointStats(checkpointId, initialCheckpointStats, startCheckpointInputTime - pendingCheckpoint.startAlignmentTime, dataToTake.values.map(_.size).sum, 0, true)
    }
    networkCommunicationActor ! SendRequest(CONTROLLER, WorkflowRecoveryMessage(actorId, CheckpointCompleted(checkpointStats)))
  }

  override def postStop(): Unit = {
    // shutdown dp thread by sending a command
    val syncFuture = new CompletableFuture[Unit]()
    inputQueue.enqueueSystemCommand(ShutdownDP(None, syncFuture))
    syncFuture.get()
    logger.info("stopped!")
  }

}
