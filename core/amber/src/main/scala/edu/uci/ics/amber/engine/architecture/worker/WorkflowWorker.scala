package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.{ActorRef, Props}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, OrdinalMapping}
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitorImpl
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.ThreadSyncChannelID
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  DPSynchronized,
  SyncAction,
  getWorkerLogName
}
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  DPThread,
  DataProcessor,
  WorkerInternalPayloadManager
}
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelEndpointID,
  DPMessage,
  StartSync,
  WorkflowExecutionPayload
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.LinkedBlockingQueue

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: ActorRef
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig,
        parentNetworkCommunicationActorRef
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  case class DPSynchronized()

  case class SyncAction(action: () => Unit)

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    val opExecConf: OpExecConfig,
    parentNetworkCommunicationActorRef: ActorRef
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef) {

  // variables unrelated to physical states
  val ordinalMapping: OrdinalMapping = opExecConf.ordinalMapping
  var operator: IOperatorExecutor = opExecConf.initIOperatorExecutor((workerIndex, opExecConf))
  val creditMonitor = new CreditMonitorImpl()

  var dataProcessor: DataProcessor = new DataProcessor(this)
  var internalQueue: WorkerInternalQueue = new WorkerInternalQueueImpl(creditMonitor)
  var dpThread: DPThread = _

  private val syncActions = new LinkedBlockingQueue[SyncAction]()

  override def initState(): Unit = {
    dataProcessor.initDP(
      this,
      Iterator.empty
    )
    dpThread = new DPThread(actorId, dataProcessor, internalQueue, this)
    dpThread.start()
    logger.info(s"Worker:$actorId = ${context.self} started")
  }

  override def getLogName: String = getWorkerLogName(actorId)

  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity): Int = {
    creditMonitor.getSenderCredits(actorVirtualIdentity)
  }

  def replaceRecoveryQueue(): Unit = {
    logger.info("replace recovery queue with normal queue")
    val newQueue = new WorkerInternalQueueImpl(creditMonitor)
    WorkerInternalQueue.transferContent(internalQueue, newQueue)
    internalQueue = newQueue
    this.dpThread.internalQueue = newQueue
    logger.info("replace queue done!")
  }

  def doSyncActionsAndUnBlockDP(): Unit = {
    while (!syncActions.isEmpty) {
      // DP paused, do action in a single-threaded manner
      syncActions.take().action()
    }
    dpThread.blockingFuture.complete(Unit)
  }

  def waitingForSync: Receive = {
    case DPSynchronized() =>
      doSyncActionsAndUnBlockDP()
      context.become(receive)
      unstashAll()
    case other =>
      stash()
  }

  override def receive: Receive =
    super.receive orElse {
      case DPSynchronized() =>
        doSyncActionsAndUnBlockDP()
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  override def handlePayload(
      channelId: ChannelEndpointID,
      payload: WorkflowExecutionPayload
  ): Unit = {
    internalQueue.enqueuePayload(DPMessage(channelId, payload))
  }

  def initiateSyncActionFromMain(func: () => Unit): Unit = {
    initiateSyncActionFromDP(func)
    context.become(waitingForSync)
  }

  def initiateSyncActionFromDP(func: () => Unit): Unit = {
    syncActions.add(SyncAction(func))
    internalQueue.enqueuePayload(DPMessage(ThreadSyncChannelID, StartSync()))
  }

  override def postStop(): Unit = {
    super.postStop()
    // shutdown dp thread by sending a command
    dpThread.stop()
    logger.info("stopped!")
  }

  override val internalPayloadManager: InternalPayloadManager = new WorkerInternalPayloadManager(
    this
  )

}
