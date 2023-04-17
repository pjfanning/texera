package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, OrdinalMapping}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{CreditMonitor, CreditMonitorImpl, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadHandler, WorkerInternalPayloadHandler}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.DPMessage
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{ReplaceRecoveryQueue, getWorkerLogName}
import edu.uci.ics.amber.engine.architecture.worker.processing.{DPThread, DataProcessor}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ShutdownDPHandler.ShutdownDP
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, IOperatorExecutor}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity}

import java.util.concurrent.CompletableFuture

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

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    supportFaultTolerance: Boolean,
    restoreConfig: ReplayConfig
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, restoreConfig, supportFaultTolerance) {

  // variables unrelated to physical states
  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  lazy val operator: IOperatorExecutor = workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  val creditMonitor = new CreditMonitorImpl()

  // variables to be initialized by physical state mgr.
  var dataProcessor: DataProcessor = _
  var internalQueue: WorkerInternalQueue = _
  var dpThread: DPThread = _

  override val internalMessageHandler: InternalPayloadHandler = new WorkerInternalPayloadHandler(this)

  override def getLogName: String = getWorkerLogName(actorId)

  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity):Int = {
    creditMonitor.getSenderCredits(actorVirtualIdentity)
  }

   override def receive: Receive =
    super.receive orElse {
      case ReplaceRecoveryQueue(sync) =>
        logger.info("replace recovery queue with normal queue")
        val newQueue = new WorkerInternalQueueImpl(creditMonitor)
        WorkerInternalQueue.transferContent(internalQueue, newQueue)
        internalQueue = newQueue
        this.dpThread.internalQueue = newQueue
        // unblock sync future on DP
        sync.complete(())
        logger.info("replace queue done!")
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
}
