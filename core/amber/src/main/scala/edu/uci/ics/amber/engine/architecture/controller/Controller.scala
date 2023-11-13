package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.Props
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, DeterminantLoggerImpl, LogManager}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.getWorkerLogName
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      AmberUtils.amberConfig.getString("fault-tolerance.log-storage-type")
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    logStorageType:String
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
}

class Controller(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig
) extends WorkflowActor(
      CONTROLLER
    ) {

  actorRefMappingService.registerActorRef(CLIENT, context.parent)
  val controllerTimerService = new ControllerTimerService(controllerConfig, actorService)
  val cp = new ControllerProcessor(
    workflow,
    controllerConfig,
    actorId,
    sendMessageFromActorToLogWriter
  )

  val logManager: LogManager = LogManager.getLogManager(controllerConfig.logStorageType, actorId.name, sendMessageFromLogWriterToActor)
  val detLogger:DeterminantLogger = logManager.getDeterminantLogger

  def sendMessageFromActorToLogWriter(msg:WorkflowFIFOMessage, step:Long): Unit ={
    logManager.sendCommitted(msg, step)
  }

  def sendMessageFromLogWriterToActor(msg:WorkflowFIFOMessage): Unit ={
    transferService.send(msg)
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    val channel = cp.inputGateway.getChannel(workflowMsg.channel)
    channel.acceptMessage(workflowMsg)
    sender ! NetworkAck(id, getSenderCredits(workflowMsg.channel))
    var waitingForInput = false
    while (!waitingForInput) {
      cp.inputGateway.tryPickChannel match {
        case Some(channel) =>
          val msg = channel.take
          msg.payload match {
            case payload: ControlPayload => cp.processControlPayload(msg.channel, payload)
            case p                       => throw new RuntimeException(s"controller cannot handle $p")
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
  override def getSenderCredits(channelID: ChannelID): Int =
    Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair

  override def handleBackpressure(isBackpressured: Boolean): Unit = {}
}
