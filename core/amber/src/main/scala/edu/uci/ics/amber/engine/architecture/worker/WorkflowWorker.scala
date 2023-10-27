package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.{
  DPQueue,
  AkkaMessageTransferService,
  WorkflowActor
}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.messaginglayer.AdaptiveBatchingMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.TriggerSend
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  final case class TriggerSend()
}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig
) extends WorkflowActor(actorId) {
  val inputQueue: DPQueue[WorkflowFIFOMessage] = new DPQueue[WorkflowFIFOMessage]()
  val outputQueue: DPQueue[WorkflowFIFOMessage] = new DPQueue[WorkflowFIFOMessage]()
  val dp = new DataProcessor(
    actorId,
    x => {
      outputQueue.offer(x)
      self ! TriggerSend()
    }
  )
  val adaptiveBatchingMonitor = new AdaptiveBatchingMonitor(actorService)
  dp.initOperator(
    workerIndex,
    workerLayer,
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer)),
    Iterator.empty
  )
  dp.initAdaptiveBatching(adaptiveBatchingMonitor)
  val dpThread = new DPThread(dp, inputQueue)
  override val transferService: AkkaMessageTransferService = new AkkaMessageTransferService(
    actorService,
    actorRefMappingService,
    backPressure => {
      if (backPressure) {
        outputQueue.block()
      } else {
        outputQueue.unblock()
      }
    }
  )

  def handleSendFromDP: Receive = {
    case TriggerSend() =>
      if (!outputQueue.isBlocked) {
        val msg = outputQueue.take
        transferService.send(msg)
      }
  }

  override def receive: Receive = {
    super.receive orElse handleSendFromDP
  }

  override def handleInputMessage(workflowMsg: WorkflowFIFOMessage): Unit = {
    inputQueue.offer(workflowMsg)
  }

  /** flow-control */
  override def getSenderCredits(channelEndpointID: ChannelID): Int =
    dp.getSenderCredits(channelEndpointID)

  override def initState(): Unit = {}

  override def postStop(): Unit = {
    super.postStop()
    adaptiveBatchingMonitor.stopAdaptiveBatching()
    dpThread.stop()
  }
}
