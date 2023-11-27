package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.Controller.ReplayStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.messaginglayer.WorkerTimerService
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{TriggerSend, WorkflowWorkerConfig}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.faulttolerance.ReplayGatewayWrapper
import edu.uci.ics.texera.workflow.common.WorkflowContext

import java.util.concurrent.LinkedBlockingQueue

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

  final case class StateRestoreConfig(readFrom: StepLoggingConfig, replayTo:Long)

  final case class StepLoggingConfig(logStorageType: String, storageKey: String)

  final case class WorkflowWorkerConfig(stateRestoreConfig: Option[StateRestoreConfig], stepLoggingConfig: Option[StepLoggingConfig])

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    workerConf: WorkflowWorkerConfig
) extends WorkflowActor(workerConf.stepLoggingConfig, actorId) {
  val inputQueue: LinkedBlockingQueue[Either[WorkflowFIFOMessage, ControlInvocation]] =
    new LinkedBlockingQueue()
  var dp = new DataProcessor(
    actorId,
    workerIndex,
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer)),
    workerLayer,
    logManager.sendCommitted
  )
  val timerService = new WorkerTimerService(actorService)

  val dpThread =
    new DPThread(actorId, dp, logManager, inputQueue)

  override def initState(): Unit = {
    dp.InitTimerService(timerService)
    if (workerConf.stateRestoreConfig.isDefined) {
      context.parent ! ReplayStatusUpdate(actorId, status = true)
      val logs = DeterminantLogStorage.getLogStorage(Some(workerConf.stateRestoreConfig.get.readFrom))
      val replayGateway = new ReplayGatewayWrapper(dp.inputGateway, logManager)
      dp.inputGateway = replayGateway
      replayGateway.setupReplay(
        logs,
        workerConf.stateRestoreConfig.get.replayTo,
        () => {
          logger.info("replay completed!")
          context.parent ! ReplayStatusUpdate(actorId, status = false)
          dp.inputGateway = dp.inputGateway.asInstanceOf[ReplayGatewayWrapper].originalGateway
        }
      )
      logger.info(s"setting up replay, current step = ${logManager.getStep} target step = ${workerConf.stateRestoreConfig.get.replayTo} # of log record to replay = ${replayGateway.orderEnforcer.channelStepOrder.size}")
    }
    dpThread.start()
  }

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
