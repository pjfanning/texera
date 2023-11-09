package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.messaginglayer.AdaptiveBatchingMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  MessageWithCallback,
  TriggerSend
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

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

  final case class TriggerSend(msg: WorkflowFIFOMessage)

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
  var dp = new DataProcessor(
    actorId,
    workerIndex,
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer)),
    workerLayer,
    x => {
      self ! TriggerSend(x)
    }
  )
  val adaptiveBatchingMonitor = new AdaptiveBatchingMonitor(actorService)
  val dpThread = new DPThread(actorId, dp, inputQueue)

  def handleSendFromDP: Receive = {
    case TriggerSend(msg) =>
      transferService.send(msg)
  }

  def handleDirectInvocation: Receive = {
    case c: ControlInvocation =>
      inputQueue.put(MessageWithCallback(Right(c), null))
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
    postStop()
    dp.asyncRPCClient.send(
      FatalError(reason),
      CONTROLLER
    )
  }

  override def receive: Receive = {
    super.receive orElse handleSendFromDP orElse handleDirectInvocation
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
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

  override def handleBackpressure(isBackpressured: Boolean): Unit = {
    val backpressureMessage = ControlInvocation(0, Backpressure(isBackpressured))
    inputQueue.put(MessageWithCallback(Right(backpressureMessage), null))
  }
}
