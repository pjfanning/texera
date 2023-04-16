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
import edu.uci.ics.amber.engine.architecture.logging.StepsOnChannel
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CreditMonitor, CreditMonitorImpl, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.PendingCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.DPMessage
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{CheckInitialized, ReplaceRecoveryQueue, getWorkerLogName}
import edu.uci.ics.amber.engine.architecture.worker.processing.{DPThread, DataProcessor, EmptyLocalCheckpointManager, LocalCheckpointManager}
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
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, restoreConfig, supportFaultTolerance) {

  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  var dataProcessor: DataProcessor = wire[DataProcessor]
  lazy val operator: IOperatorExecutor =
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  val creditMonitor = new CreditMonitorImpl()
  var internalQueue: WorkerInternalQueue = _

  override def getLogName: String = getWorkerLogName(actorId)

  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity) = {
    creditMonitor.getSenderCredits(actorVirtualIdentity)
  }

  override def setupState(fromChkpt: Option[SavedCheckpoint], replayConf: Option[Long]): Unit = {
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
    fromChkpt match {
      case Some(chkpt) =>
        outputIter = reloadState(chkpt)
      case None =>
        internalQueue = new WorkerInternalQueueImpl(creditMonitor)
    }
    initReplay(replayConf)
    dataProcessor.initDP(
      operator,
      outputIter,
      context,
      logManager,
      internalQueue
    )
    new DPThread(actorId, dataProcessor, internalQueue).start()
    logger.info(s"Worker:$actorId = ${context.self} started")
  }

  def reloadState(chkpt:SavedCheckpoint): Iterator[(ITuple, Option[Int])] ={
    internalQueue = chkpt.load("internalQueueState")
    logger.info("input queue restored")
    dataProcessor = chkpt.load("controlState")
    logger.info(s"DP restored ${dataProcessor.upstreamLinkStatus.upstreamMapReverse}")
    var outputIter:Iterator[(ITuple, Option[Int])] = Iterator.empty
    operator match {
      case support: CheckpointSupport =>
        outputIter = support.deserializeState(chkpt)
      case _ =>
    }
    logger.info("operator restored")
    outputIter
  }

  def initReplay(replayConf:Option[Long]): Unit ={
    // set replay
    replayConf match {
      case Some(replayTo) =>
        val queue = internalQueue match {
          case impl: RecoveryInternalQueueImpl => impl
          case impl: WorkerInternalQueueImpl   =>
            // TODO: convert to replay queue if we have normal queue
            new RecoveryInternalQueueImpl(creditMonitor)
        }
        queue.initialize(
          logStorage.getReader.mkLogRecordIterator().collect { case a: StepsOnChannel => a },
          dataProcessor.determinantLogger.getStep,
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
        internalQueue match {
          case impl: RecoveryInternalQueueImpl =>
            replaceRecoveryQueue()
          case impl: WorkerInternalQueueImpl =>
          // do nothing
        }
    }
  }

//  def insertPayloads(channelData: mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]): Unit ={
//    channelData.foreach{
//      case (channelId, payloads) =>
//        payloads.foreach(x => handlePayload(channelId, x))
//    }
//  }

  def replaceRecoveryQueue(): Unit = {
    val oldInputQueue = internalQueue.asInstanceOf[RecoveryInternalQueueImpl]
    internalQueue = new WorkerInternalQueueImpl(creditMonitor)
    // TODO: add unprocessed inputs into new queue
  }

   override def receive: Receive =
    super.receive orElse {
      case ReplaceRecoveryQueue(sync) =>
        logger.info("replace recovery queue with normal queue")
        replaceRecoveryQueue()
        // unblock sync future on DP
        sync.complete(())
        logger.info("replace queue done!")
      case AmberInternalMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case AmberInternalMessage(from, ContinueReplayTo(index)) =>
        assert(internalQueue.isInstanceOf[RecoveryInternalQueueImpl])
        logger.info("set replay to " + index)
        internalQueue.asInstanceOf[RecoveryInternalQueueImpl].setReplayTo(index, () => {
          logger.info("replay completed!")
          context.parent ! AmberInternalMessage(actorId, UpdateRecoveryStatus(false))
        })
        internalQueue.enqueueSystemCommand(NoOp()) //kick start replay process
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  override def inputPayload(channelId: ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    internalQueue.enqueuePayload(DPMessage(channelId, payload))
  }

  override def postStop(): Unit = {
    super.postStop()
    // shutdown dp thread by sending a command
    val syncFuture = new CompletableFuture[Unit]()
    internalQueue.enqueueSystemCommand(ShutdownDP(None, syncFuture))
    syncFuture.get()
    logger.info("stopped!")
  }

  override val localCheckpointManager: LocalCheckpointManager = new EmptyLocalCheckpointManager()
}
