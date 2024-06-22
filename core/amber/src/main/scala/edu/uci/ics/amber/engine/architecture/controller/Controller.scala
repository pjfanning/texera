package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.SupervisorStrategy.Stop
import akka.pattern.ask
import akka.actor.{AllForOneStrategy, Props, SupervisorStrategy}
import akka.util.Timeout
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.NetworkAck
import edu.uci.ics.amber.engine.architecture.controller.Controller.{ReplayStatusUpdate, RetrieveOperatorState, WorkflowRecoveryStatus}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowPaused, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{FaultToleranceConfig, StateRestoreConfig}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelMarkerPayload, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.ExecutionStatsUpdate
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelIdentity}
import edu.uci.ics.amber.engine.common.{AmberConfig, CheckpointState, SerializedState, VirtualIdentityUtils}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

import scala.concurrent.duration.DurationInt

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      statusUpdateIntervalMs = Option(AmberConfig.getStatusUpdateIntervalInMs),
      stateRestoreConfOpt = None,
      faultToleranceConfOpt = None
    )
}

final case class ControllerConfig(
    statusUpdateIntervalMs: Option[Long],
    stateRestoreConfOpt: Option[StateRestoreConfig],
    faultToleranceConfOpt: Option[FaultToleranceConfig]
)

object Controller {

  def props(
      workflowContext: WorkflowContext,
      physicalPlan: PhysicalPlan,
      opResultStorage: OpResultStorage,
      controllerConfig: ControllerConfig = ControllerConfig.default
  ): Props =
    Props(
      new Controller(
        workflowContext,
        physicalPlan,
        opResultStorage,
        controllerConfig
      )
    )

  final case class ReplayStatusUpdate(id: ActorVirtualIdentity, status: Boolean)
  final case class WorkflowRecoveryStatus(isRecovering: Boolean)
  final case class RetrieveOperatorState(opId:String)
}

class Controller(
    workflowContext: WorkflowContext,
    physicalPlan: PhysicalPlan,
    opResultStorage: OpResultStorage,
    controllerConfig: ControllerConfig
) extends WorkflowActor(
      controllerConfig.faultToleranceConfOpt,
      CONTROLLER
    ) {
  implicit val timeout: Timeout = 5.minutes
  actorRefMappingService.registerActorRef(CLIENT, context.parent)
  val controllerTimerService = new ControllerTimerService(controllerConfig, actorService)
  var cp = new ControllerProcessor(
    workflowContext,
    opResultStorage,
    controllerConfig,
    actorId,
    logManager.sendCommitted
  )

  // manages the lifecycle of entire replay process
  // triggers onStart callback when the first worker/controller marks itself as recovering.
  // triggers onComplete callback when all worker/controller finishes recovering.
  private val globalReplayManager = new GlobalReplayManager(
    () => {
      //onStart
      context.parent ! WorkflowRecoveryStatus(true)
    },
    () => {
      //onComplete
      context.parent ! WorkflowRecoveryStatus(false)
    }
  )

  override def initState(): Unit = {
    initControllerProcessor()
    val controllerRestoreConf = controllerConfig.stateRestoreConfOpt
    if (controllerRestoreConf.isDefined) {
      globalReplayManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
      setupReplay(
        cp,
        controllerRestoreConf.get,
        () => {
          logger.info("replay completed!")
          globalReplayManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
        }
      )
      context.parent ! WorkflowRecoveryStatus(true)
      processMessages()
    }
  }

  override def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit = {
    val channel = cp.inputGateway.getChannel(workflowMsg.channelId)
    channel.acceptMessage(workflowMsg)
    sender() ! NetworkAck(id, getInMemSize(workflowMsg), getQueuedCredit(workflowMsg.channelId))
    processMessages()
  }

  def processMessages(): Unit = {
    if(isReadOnlyState){
      logger.info("controller is in read only state")
      return
    }
    var waitingForInput = false
    while (!waitingForInput) {
      cp.inputGateway.tryPickChannel match {
        case Some(channel) =>
          val msg = channel.take
          val msgToLog = Some(msg).filter(_.payload.isInstanceOf[ControlPayload])
          logManager.withFaultTolerant(msg.channelId, msgToLog) {
            msg.payload match {
              case payload: ControlPayload      => cp.processControlPayload(msg.channelId, payload)
              case marker: ChannelMarkerPayload => // skip marker
              case p                            => throw new RuntimeException(s"controller cannot handle $p")
            }
          }
        case None =>
          waitingForInput = true
      }
    }
  }

  def handleDirectInvocation: Receive = {
    case req: RetrieveOperatorState =>
      val localSender = sender()
      val worker = cp.executionState.getAllBuiltWorkers.find( w =>
        w.name.contains(req.opId) && !w.name.contains("globalAgg")).get
      val ref = actorRefMappingService.actorRefMapping(worker)
      (ref ? req).map {
        ret =>
          localSender ! ret
      }(context.dispatcher)
    case c: ControlInvocation =>
      // only client and self can send direction invocations
      val source = if (sender() == self) {
        SELF
      } else {
        CLIENT
      }
      val controlChannelId = ChannelIdentity(source, SELF, isControl = true)
      val channel = cp.inputGateway.getChannel(controlChannelId)
      channel.acceptMessage(
        WorkflowFIFOMessage(controlChannelId, channel.getCurrentSeq, c)
      )
      processMessages()
  }

  def handleReplayMessages: Receive = {
    case ReplayStatusUpdate(id, status) =>
      globalReplayManager.markRecoveryStatus(id, status)
  }

  override def receive: Receive = {
    super.receive orElse handleDirectInvocation orElse handleReplayMessages
  }

  /** flow-control */
  override def getQueuedCredit(channelId: ChannelIdentity): Long = {
    0 // no queued credit for controller
  }
  override def handleBackpressure(isBackpressured: Boolean): Unit = {}
  // adopted solution from
  // https://stackoverflow.com/questions/54228901/right-way-of-exception-handling-when-using-akka-actors
  override val supervisorStrategy: SupervisorStrategy =
    AllForOneStrategy(maxNrOfRetries = 0, withinTimeRange = 1.minute) {
      case e: Throwable =>
        val failedWorker = actorRefMappingService.findActorVirtualIdentity(sender())
        logger.error(s"Encountered fatal error from $failedWorker, amber is shutting done.", e)
        cp.asyncRPCClient.sendToClient(
          FatalError(e, failedWorker)
        ) // only place to actively report fatal error
        Stop
    }

  private def initControllerProcessor(): Unit = {
    cp.setupActorService(actorService)
    cp.setupTimerService(controllerTimerService)
    cp.setupActorRefService(actorRefMappingService)
    cp.setupLogManager(logManager)
    cp.setupReplayManager(globalReplayManager)
    cp.setupTransferService(transferService)
    cp.workflowScheduler.updateSchedule(physicalPlan)
  }

  override def initFromCheckpoint(chkpt: CheckpointState): Unit = {
    val cpState: ControllerProcessor = chkpt.load(SerializedState.CP_STATE_KEY)
    val outputMessages: Array[WorkflowFIFOMessage] = chkpt.load(SerializedState.OUTPUT_MSG_KEY)
    cp = cpState
    cp.outputHandler = logManager.sendCommitted
    initControllerProcessor()
    // revive all workers.
    val builtPhysicalOps = cp.executionState.getAllBuiltWorkers
      .map(workerId => {
        VirtualIdentityUtils.getPhysicalOpId(workerId)
      })
      .toSet
    builtPhysicalOps.foreach { opId =>
      val op = workflow.physicalPlan.getOperator(opId)
      val region =
        workflow.regionPlan.regions.find(r => r.resourceConfig.get.operatorConfigs.contains(opId))
      val dummyExecution =
        new OperatorExecution(workflow.context.workflowId, workflow.context.executionId, opId, 1)
      op.build(
        actorService,
        dummyExecution,
        region.get.resourceConfig.get.operatorConfigs(opId),
        controllerConfig.workerRestoreConf,
        controllerConfig.workerLoggingConf,
        globalReplayManager
      )

    }
    outputMessages.foreach(transferService.send)
    logger.info(s"restored workflow state = ${cp.executionState.getState}")
    cp.asyncRPCClient.sendToClient(WorkflowStatusUpdate(cp.executionState.getWorkflowStatus))
    cp.asyncRPCClient.sendToClient(WorkflowPaused())
    globalReplayManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
  }
}
