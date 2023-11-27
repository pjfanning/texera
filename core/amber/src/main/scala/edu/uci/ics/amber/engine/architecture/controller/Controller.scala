package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{AllForOneStrategy, Props, SupervisorStrategy}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.controller.Controller.ReplayStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowRecoveryStatus
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{StateRestoreConfig, StepLoggingConfig}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
import edu.uci.ics.amber.engine.faulttolerance.ReplayGatewayWrapper
import edu.uci.ics.texera.workflow.common.WorkflowContext

import scala.concurrent.duration.DurationInt

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      x => None,x => None
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    stateRestoreConfigs: ActorVirtualIdentity => Option[StateRestoreConfig],
    stepLoggingConfigs: ActorVirtualIdentity => Option[StepLoggingConfig]
)

object Controller {

  val recoveryDelay: Long = AmberUtils.amberConfig.getLong("fault-tolerance.delay-before-recovery")

  def props(
      workflow: Workflow,
      controllerConfig: ControllerConfig = ControllerConfig.default
  ): Props =
    Props(
      new Controller(
        workflow,
        controllerConfig
      )
    )

  final case class ReplayStatusUpdate(id: ActorVirtualIdentity, status: Boolean)
}

class Controller(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig
) extends WorkflowActor(
      controllerConfig.stepLoggingConfigs(CONTROLLER),
      CONTROLLER
    ) {
  actorRefMappingService.registerActorRef(CLIENT, context.parent)
  val controllerTimerService = new ControllerTimerService(controllerConfig, actorService)
  val cp = new ControllerProcessor(
    workflow,
    controllerConfig,
    actorId,
    logManager.sendCommitted
  )

  val replayManager = new GlobalReplayManager(
    () => {
      cp.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
    },
    () => {
      cp.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
    }
  )

  override def initState(): Unit = {
    cp.setupActorService(actorService)
    cp.setupTimerService(controllerTimerService)
    cp.setupActorRefService(actorRefMappingService)
    val stateRestoreConfig = controllerConfig.stateRestoreConfigs(CONTROLLER)
    if (stateRestoreConfig.isDefined) {
      val logs = DeterminantLogStorage.getLogStorage(Some(stateRestoreConfig.get.readFrom))
      replayManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
      val replayGateway = new ReplayGatewayWrapper(cp.inputGateway, logManager)
      cp.inputGateway = replayGateway
      replayGateway.setupReplay(
        logs,
        stateRestoreConfig.get.replayTo,
        () => {
          replayManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
          cp.inputGateway = cp.inputGateway.asInstanceOf[ReplayGatewayWrapper].originalGateway
        }
      )
      logger.info(
        s"setting up replay, " +
          s"current step = ${logManager.getStep} " +
          s"target step = ${controllerConfig.replayTo.get} " +
          s"# of log record to replay = ${replayGateway.orderEnforcer.channelStepOrder.size}"
      )
      processMessages()
    }
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    val channel = cp.inputGateway.getChannel(workflowMsg.channel)
    channel.acceptMessage(workflowMsg)
    sender ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channel))
    processMessages()
  }

  def processMessages(): Unit = {
    var waitingForInput = false
    while (!waitingForInput) {
      cp.inputGateway.tryPickChannel match {
        case Some(channel) =>
          val msg = channel.take
          logManager.doFaultTolerantProcessing(msg.channel, Some(msg)) {
            msg.payload match {
              case payload: ControlPayload => cp.processControlPayload(msg.channel, payload)
              case p                       => throw new RuntimeException(s"controller cannot handle $p")
            }
          }
        case None => waitingForInput = true
      }
    }
  }

  def handleDirectInvocation: Receive = {
    case c: ControlInvocation =>
      // only client and self can send direction invocations
      val source = if (sender == self) {
        SELF
      } else {
        CLIENT
      }
      val selfControlChannelId = ChannelID(source, SELF, isControl = true)
      val channel = cp.inputGateway.getChannel(selfControlChannelId)
      channel.acceptMessage(
        WorkflowFIFOMessage(selfControlChannelId, channel.getCurrentSeq, c)
      )
      processMessages()
  }

  def handleReplayMessages: Receive = {
    case ReplayStatusUpdate(id, status) =>
      replayManager.markRecoveryStatus(id, status)
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation orElse handleReplayMessages
  }

  /** flow-control */
  override def getQueuedCredit(channelID: ChannelID): Long = {
    0 // no queued credit for controller
  }
  override def handleBackpressure(isBackpressured: Boolean): Unit = {}
  // adopted solution from
  // https://stackoverflow.com/questions/54228901/right-way-of-exception-handling-when-using-akka-actors
  override val supervisorStrategy: SupervisorStrategy =
    AllForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 1.minute) {
      case e: Throwable =>
        val failedWorker = actorRefMappingService.findActorVirtualIdentity(sender)
        logger.error(s"Encountered fatal error from $failedWorker, amber is shutting done.", e)
        cp.asyncRPCServer.execute(FatalError(e, failedWorker), actorId)
        Stop
    }

}
