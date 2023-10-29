package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.{
  AkkaMessageTransferService,
  DPOutputQueue,
  WorkflowActor
}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.messaginglayer.AdaptiveBatchingMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  MessageWithCallback,
  TriggerSend
}
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.LinkedBlockingQueue

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

  final case class MessageWithCallback(
      msg: Either[WorkflowFIFOMessage, ControlInvocation],
      callback: () => Unit
  )
}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig
) extends WorkflowActor(actorId) {
  val inputQueue: LinkedBlockingQueue[MessageWithCallback] =
    new LinkedBlockingQueue()
  val outputQueue: DPOutputQueue[WorkflowFIFOMessage] = new DPOutputQueue[WorkflowFIFOMessage]()
  var dp = new DataProcessor(
    actorId,
    workerIndex,
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer)),
    workerLayer,
    x => {
      outputQueue.offer(x)
      self ! TriggerSend()
    }
  )
  val adaptiveBatchingMonitor = new AdaptiveBatchingMonitor(actorService)
  val dpThread = new DPThread(actorId, dp, inputQueue)
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
        logger.info(s"send $msg")
        transferService.send(msg)
      }
  }

  def handleDirectInvocation: Receive = {
    case c: ControlInvocation =>
      inputQueue.put(MessageWithCallback(Right(c), null))
  }

  override def receive: Receive = {
    super.receive orElse handleSendFromDP orElse handleDirectInvocation
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    logger.info(s"received $workflowMsg")
    val senderRef = sender()
    inputQueue.put(
      MessageWithCallback(
        Left(workflowMsg),
        () => {
          senderRef ! NetworkAck(id, dp.getSenderCredits(workflowMsg.channel))
        }
      )
    )
  }

  /** flow-control */
  override def getSenderCredits(channelEndpointID: ChannelID): Int =
    dp.getSenderCredits(channelEndpointID)

  override def initState(): Unit = {
    dp.initAdaptiveBatching(adaptiveBatchingMonitor)
    dpThread.start()
  }

  override def postStop(): Unit = {
    super.postStop()
    adaptiveBatchingMonitor.stopAdaptiveBatching()
    dpThread.stop()
  }
}
