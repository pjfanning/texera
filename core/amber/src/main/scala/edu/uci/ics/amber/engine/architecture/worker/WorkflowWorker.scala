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
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CreditMonitor, CreditMonitorImpl, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.PendingCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.DPMessage
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{CheckInitialized, ReplaceRecoveryQueue, getWorkerLogName}
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.NoOpHandler.NoOp
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ShutdownDPHandler.ShutdownDP
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, IOperatorExecutor}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.{TakeCheckpoint, TakeCursor}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalMessage, ChannelEndpointID, CheckpointCompleted, ContinueReplayTo, ControlPayload, CreditRequest, DataFrame, DataPayload, EndOfUpstream, EpochMarker, EstimationMarker, FIFOMarker, GetOperatorInternalState, GlobalCheckpointMarker, UpdateRecoveryStatus, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
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
      stateRestoreConfig: ReplayConfig
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
    restoreConfig: ReplayConfig
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, supportFaultTolerance) {
  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  var dataProcessor: DataProcessor = wire[DataProcessor]
  lazy val operator: IOperatorExecutor =
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  logger.info(s"Worker:$actorId = ${context.self}")
  lazy val inputPort: NetworkInputPort = new NetworkInputPort(this.actorId, this.handlePayload)
  val creditMonitor = new CreditMonitorImpl()
  var inputQueue: WorkerInternalQueue = _
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
  private val receivedMarkers = mutable.HashSet[Long]()
  private val pendingCheckpoints = mutable.HashMap[Long, PendingCheckpoint]()

  if (parentNetworkCommunicationActorRef != null) {
    parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
  }

  override def getLogName: String = getWorkerLogName(actorId)

  def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity) = {
    creditMonitor.getSenderCredits(actorVirtualIdentity)
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
          outputIter = support.deserializeState(chkpt)
      case _ =>
    }
    logger.info("operator restored")
    dataProcessor = chkpt.load("controlState")
    logger.info(s"DP restored ${dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
    outputIter
  }

  def insertPayloads(channelData: mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]): Unit ={
    channelData.foreach{
      case (channelId, payloads) =>
        payloads.foreach(x => handlePayload(channelId, x))
    }
  }

  override def receive: Receive = {
    // load from checkpoint if available
    var outputIter: Iterator[(ITuple, Option[Int])] = Iterator.empty
    try {
      restoreConfig.fromCheckpoint match {
        case None | Some(0) =>
          inputQueue = new WorkerInternalQueueImpl(creditMonitor)
        case Some(alignment) =>
          val chkpt = CheckpointHolder.getCheckpoint(actorId, alignment)
          logger.info("checkpoint found, start loading")
          val startLoadingTime = System.currentTimeMillis()
          // restore state from checkpoint: can be in either replaying or normal processing
          chkpt.attachSerialization(SerializationExtension(context.system))
          val fifoState:Map[ChannelEndpointID, Long] = chkpt.load("fifoState")
          val fifoStateWithInputData = (chkpt.getInputData.mapValues(_.size.toLong).toSeq ++ fifoState.toSeq)
            .groupBy(_._1).mapValues(_.map(_._2).sum)
          fifoStateWithInputData.foreach{
            case (tuple, l) =>
              logger.info(s"restored fifo status for upstream $tuple is $l")
          }
          inputPort.setFIFOState(fifoStateWithInputData)
          logger.info("fifo state restored")
          inputQueue = chkpt.load("inputHubState")
          logger.info("input queue restored")
          operator match {
            case support: CheckpointSupport =>
              outputIter = support.deserializeState(chkpt)
            case _ =>
          }
          logger.info("operator restored")
          dataProcessor = chkpt.load("controlState")
          logger.info(s"DP restored ${dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
          chkpt.getInputData.foreach{
            case (channel, payloads) =>
              payloads.foreach(payload => handlePayload(channel, payload))
          }
          logger.info("inflight data restored")
          logger.info(
            s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime) / 1000f}s"
          )
      }
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
              while (impl.getQueueHeadStatus(0).isDefined) {
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
            context.parent ! AmberInternalMessage(actorId, UpdateRecoveryStatus(false))
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
    dataProcessor.initDP(
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
      case AmberInternalMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case AmberInternalMessage(from, ContinueReplayTo(index)) =>
        assert(inputQueue.isInstanceOf[RecoveryInternalQueueImpl])
        logger.info("set replay to " + index)
        inputQueue.asInstanceOf[RecoveryInternalQueueImpl].setReplayTo(index, () => {
          logger.info("replay completed!")
          context.parent ! AmberInternalMessage(actorId, UpdateRecoveryStatus(false))
        })
        inputQueue.enqueueSystemCommand(NoOp()) //kick start replay process
      case NetworkMessage(id, workflowMsg @ WorkflowFIFOMessage(channel, _, payload)) =>
        sender ! NetworkAck(id, Some(getSenderCredits(channel.endpointWorker)))
        if(payload.isInstanceOf[GlobalCheckpointMarker]){
          logger.info(s"receive ${payload.asInstanceOf[GlobalCheckpointMarker].id} from $channel")
        }
        inputPort.handleMessage(workflowMsg)
      case NetworkMessage(id, CreditRequest(actor)) =>
        sender ! NetworkAck(id, Some(getSenderCredits(actor)))
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handlePayload(ChannelEndpointID(SELF, false), invocation)
  }

  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      sender ! Ack
  }

  def handlePayload(channelId:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    pendingCheckpoints.foreach{
      case (id, chkpt) =>
        if(!payload.isInstanceOf[FIFOMarker]){
          chkpt.recordInput(channelId, payload)
        }
    }
    payload match {
      case marker: FIFOMarker =>
        logger.info(s"process marker $marker")
        marker match {
          case EstimationMarker(id) =>
            if(!receivedMarkers.contains(id)){
              receivedMarkers.add(id)
              inputQueue.enqueueSystemCommand(TakeCursor(marker, inputPort.getFIFOState))
            }
          case GlobalCheckpointMarker(id, markerCollectionCount) =>
            if(!pendingCheckpoints.contains(id)){
              val chkpt = new SavedCheckpoint()
              chkpt.attachSerialization(SerializationExtension(context.system))
              logger.info("start to take local checkpoint")
              // fill in checkpoint
              chkpt.save("fifoState", inputPort.getFIFOState)
              val syncFuture = new CompletableFuture[Long]()
              inputQueue.enqueueSystemCommand(TakeCheckpoint(marker, chkpt, syncFuture))
              val totalStep = syncFuture.get()
              val onComplete = () => {context.parent ! AmberInternalMessage(actorId, CheckpointCompleted(id, totalStep))}
              pendingCheckpoints(id) = new PendingCheckpoint(actorId, System.currentTimeMillis(), chkpt, totalStep, markerCollectionCount.getOrElse(actorId, 1), onComplete)
            }
            pendingCheckpoints(id).acceptSnapshotMarker(channelId)
            if(pendingCheckpoints(id).isCompleted){
              pendingCheckpoints.remove(id)
            }
        }
      case other =>
        inputQueue.enqueuePayload(DPMessage(channelId, payload))
    }
  }

  override def postStop(): Unit = {
    // shutdown dp thread by sending a command
    val syncFuture = new CompletableFuture[Unit]()
    inputQueue.enqueueSystemCommand(ShutdownDP(None, syncFuture))
    syncFuture.get()
    logger.info("stopped!")
  }

}
