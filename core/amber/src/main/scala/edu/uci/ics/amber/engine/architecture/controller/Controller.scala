package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{AllForOneStrategy, Props, SupervisorStrategy}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowRecoveryStatus
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.{MessageContent, ProcessingStep}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
import edu.uci.ics.amber.engine.faulttolerance.{ReplayGatewayWrapper, ReplayOrderEnforcer}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      AmberUtils.amberConfig.getString("fault-tolerance.log-storage-type"),
      None
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    logStorageType: String,
    replayTo: Option[Long]
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

  final case class ReplayStart(id: ActorVirtualIdentity)

  final case class ReplayComplete(id: ActorVirtualIdentity)
}

class Controller(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig
) extends WorkflowActor(
      controllerConfig.logStorageType,
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

  val replayOrderEnforcer = new ReplayOrderEnforcer()
  if (controllerConfig.replayTo.isDefined) {
    replayManager.markRecoveryStatus(CONTROLLER, true)
    val logs = logStorage.getReader.mkLogRecordIterator().toArray
    val steps = mutable.Queue[ProcessingStep]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        cp.inputGateway.getChannel(message.channel).acceptMessage(message)
      case other =>
        throw new RuntimeException(s"cannot handle $other in the log")
    }
    replayOrderEnforcer.setReplayTo(
      steps,
      cp.cursor.getStep,
      controllerConfig.replayTo.get,
      () => {
        replayManager.markRecoveryStatus(CONTROLLER, false)
        cp.inputGateway = cp.inputGateway.asInstanceOf[ReplayGatewayWrapper].networkInputGateway
      }
    )
    cp.inputGateway = new ReplayGatewayWrapper(replayOrderEnforcer, cp.inputGateway)
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    val channel = cp.inputGateway.getChannel(workflowMsg.channel)
    channel.acceptMessage(workflowMsg)
    sender ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channel))
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
      cp.processControlPayload(ChannelID(SELF, SELF, isControl = true), c)
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation
  }

  override def initState(): Unit = {
    cp.setupActorService(actorService)
    cp.setupTimerService(controllerTimerService)
    cp.setupActorRefService(actorRefMappingService)
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
