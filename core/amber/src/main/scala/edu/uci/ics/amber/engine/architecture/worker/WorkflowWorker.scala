package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.Controller.{ReplayComplete, ReplayStart}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.logging.{MessageContent, ProcessingStep}
import edu.uci.ics.amber.engine.architecture.messaginglayer.WorkerTimerService
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  TriggerSend,
  WorkflowWorkerConfig
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.faulttolerance.{ReplayGatewayWrapper, ReplayOrderEnforcer}

import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      workerConf: WorkflowWorkerConfig
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig,
        workerConf
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  final case class TriggerSend(msg: WorkflowFIFOMessage)

  final case class WorkflowWorkerConfig(logStorageType: String, replayTo: Option[Long])

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    workerConf: WorkflowWorkerConfig
) extends WorkflowActor(workerConf.logStorageType, actorId) {
  val inputQueue: LinkedBlockingQueue[Either[WorkflowFIFOMessage, ControlInvocation]] =
    new LinkedBlockingQueue()
  var dp = new DataProcessor(
    actorId,
    workerIndex,
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer)),
    workerLayer,
    sendMessageToLogWriter
  )
  val timerService = new WorkerTimerService(actorService)

  val replayOrderEnforcer = new ReplayOrderEnforcer()
  if (workerConf.replayTo.isDefined) {
    context.parent ! ReplayStart(actorId)
    val logs = logStorage.getReader.mkLogRecordIterator().toArray
    val steps = mutable.Queue[ProcessingStep]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        dp.inputGateway.getChannel(message.channel).acceptMessage(message)
      case other =>
        throw new RuntimeException(s"cannot handle $other in the log")
    }
    replayOrderEnforcer.setReplayTo(
      steps,
      dp.cursor.getStep,
      workerConf.replayTo.get,
      () => {
        context.parent ! ReplayComplete(actorId)
        dp.inputGateway = dp.inputGateway.asInstanceOf[ReplayGatewayWrapper].networkInputGateway
      }
    )
    dp.inputGateway = new ReplayGatewayWrapper(replayOrderEnforcer, dp.inputGateway)
  }

  val dpThread =
    new DPThread(actorId, dp, logManager.getDeterminantLogger, replayOrderEnforcer, inputQueue)

  def sendMessageFromDPToMain(msg: WorkflowFIFOMessage): Unit = {
    // limitation: TriggerSend will be processed after input messages before it.
    self ! TriggerSend(msg)
  }

  def handleSendFromDP: Receive = {
    case TriggerSend(msg) =>
      transferService.send(msg)
  }

  def handleDirectInvocation: Receive = {
    case c: ControlInvocation =>
      inputQueue.put(Right(c))
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
    postStop()
    dp.asyncRPCClient.send(
      FatalError(reason, Some(actorId)),
      CONTROLLER
    )
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    inputQueue.put(Left(workflowMsg))
    sender ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channel))
  }

  /** flow-control */
  override def getQueuedCredit(channelID: ChannelID): Long =
    dp.getQueuedCredit(channelID)

  override def initState(): Unit = {
    dp.InitTimerService(timerService)
    dpThread.start()
  }

  override def postStop(): Unit = {
    super.postStop()
    timerService.stopAdaptiveBatching()
    dpThread.stop()
    logManager.terminate()
  }

  override def handleBackpressure(isBackpressured: Boolean): Unit = {
    val backpressureMessage = ControlInvocation(0, Backpressure(isBackpressured))
    inputQueue.put(Right(backpressureMessage))
  }
}
