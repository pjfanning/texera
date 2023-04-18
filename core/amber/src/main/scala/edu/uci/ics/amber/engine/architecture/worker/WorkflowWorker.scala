package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, OrdinalMapping}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkSenderActorRef
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitorImpl
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.ShutdownDP
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{ReplaceRecoveryQueue, getWorkerLogName}
import edu.uci.ics.amber.engine.architecture.worker.processing.{DPThread, DataProcessor, WorkerInternalPayloadManager}
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, FuncDelegate, WorkflowDPMessagePayload, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.INTERNAL

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

  override def handlePayload(channelId: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit = {
    internalQueue.enqueuePayload(DPMessage(channelId, payload))
  }

  def executeThroughDP[T](
                           func: () => T
                         ): T = {
    val future = new CompletableFuture[T]()
    internalQueue.enqueuePayload(DPMessage(ChannelEndpointID(INTERNAL, isControlChannel = true), FuncDelegate(func, future)))
    future.get()
  }

  override def postStop(): Unit = {
    super.postStop()
    // shutdown dp thread by sending a command
    internalPayloadManager.handlePayload(ChannelEndpointID(INTERNAL, true), ShutdownDP())
    logger.info("stopped!")
  }

  override def internalPayloadManager: InternalPayloadManager = new WorkerInternalPayloadManager(this)
}
