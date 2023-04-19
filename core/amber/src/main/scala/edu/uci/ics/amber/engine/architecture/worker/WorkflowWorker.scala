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
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, FuncDelegate, FuncDelegateNoReturn, InternalChannelEndpointID, WorkflowDPMessagePayload, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.CompletableFuture

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef
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

  case class ReplaceRecoveryQueue()

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef) {

  // variables unrelated to physical states
  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  lazy val operator: IOperatorExecutor = workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  val creditMonitor = new CreditMonitorImpl()


  var dataProcessor: DataProcessor = new DataProcessor(ordinalMapping, actorId, determinantLogger)
  var internalQueue: WorkerInternalQueue = new WorkerInternalQueueImpl(creditMonitor)
  var dpThread: DPThread = _

  override def initState(): Unit = {
    dataProcessor.initDP(
      operator,
      Iterator.empty,
      context,
      logManager,
      internalQueue
    )
    dpThread = new DPThread(actorId, dataProcessor, internalQueue)
    dpThread.start()
    logger.info(s"Worker:$actorId = ${context.self} started")
  }

  override def getLogName: String = getWorkerLogName(actorId)

  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity):Int = {
    creditMonitor.getSenderCredits(actorVirtualIdentity)
  }

   override def receive: Receive =
    super.receive orElse {
      case ReplaceRecoveryQueue() =>
        logger.info("replace recovery queue with normal queue")
        val newQueue = new WorkerInternalQueueImpl(creditMonitor)
        WorkerInternalQueue.transferContent(internalQueue, newQueue)
        internalQueue = newQueue
        this.dpThread.internalQueue = newQueue
        logger.info("replace queue done!")
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  override def handlePayload(channelId: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit = {
    internalQueue.enqueuePayload(DPMessage(channelId, payload))
  }

  def executeThroughDP[T](
                           func: () => T
                         ): CompletableFuture[T] = {
    val future = new CompletableFuture[T]()
    internalQueue.enqueuePayload(DPMessage(InternalChannelEndpointID, FuncDelegate(func, future)))
    dpThread.unblock() // in case it is blocked.
    future
  }

  def executeThroughDPNoReturn(
                           func: () => Unit
                         ): Unit = {
    internalQueue.enqueuePayload(DPMessage(InternalChannelEndpointID, FuncDelegateNoReturn(func)))
    dpThread.unblock() // in case it is blocked.
  }

  override def postStop(): Unit = {
    super.postStop()
    // shutdown dp thread by sending a command
    internalPayloadManager.handlePayload(InternalChannelEndpointID, ShutdownDP())
    logger.info("stopped!")
  }

  override val internalPayloadManager: InternalPayloadManager = new WorkerInternalPayloadManager(this)

}
