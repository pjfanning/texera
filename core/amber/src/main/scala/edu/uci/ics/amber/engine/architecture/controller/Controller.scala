package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.Props
import akka.serialization.SerializationExtension
import akka.util.Timeout
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkMessage,
  NetworkSenderActorRef,
  RegisterActorRef
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      AmberUtils.amberConfig.getBoolean("fault-tolerance.enable-determinant-logging"),
      WorkflowStateRestoreConfig.empty
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    var supportFaultTolerance: Boolean,
    var stateRestoreConfig: WorkflowStateRestoreConfig
)

object Controller {

  def props(
      workflow: Workflow,
      controllerConfig: ControllerConfig = ControllerConfig.default,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef = NetworkSenderActorRef()
  ): Props =
    Props(
      new Controller(
        workflow,
        controllerConfig,
        parentNetworkCommunicationActorRef
      )
    )
}

class Controller(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(
      CONTROLLER,
      parentNetworkCommunicationActorRef,
      controllerConfig.supportFaultTolerance
    ) {
  private var controlInputPort: NetworkInputPort[ControlPayload] = _

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
  private var controllerProcessor: ControllerProcessor = _

  override def getLogName: String = "WF" + workflow.getWorkflowId().id + "-CONTROLLER"

  val workflowScheduler =
    new WorkflowScheduler(
      networkCommunicationActor,
      context,
      logger,
      workflow,
      controllerConfig
    )

  // register controller itself and client
  networkCommunicationActor.waitUntil(RegisterActorRef(CONTROLLER, self))
  networkCommunicationActor.waitUntil(RegisterActorRef(CLIENT, context.parent))

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, controller is shutting done.", reason)
    // report error to frontend
    controllerProcessor.sendToClient(FatalError(reason))
  }

  def running: Receive = {
    forwardResendRequest orElse acceptRecoveryMessages orElse acceptDirectInvocations orElse {
      case NetworkMessage(id, WorkflowControlMessage(from, seqNum, payload)) =>
        controlInputPort.handleMessage(
          this.sender(),
          Constants.unprocessedBatchesCreditLimitPerSender, // Controller is assumed to have enough credits
          id,
          from,
          seqNum,
          payload
        )
      case other =>
        logger.info(s"unhandled message: $other")
    }
  }

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      controllerProcessor.handleControlPayload(CLIENT, invocation)
  }

  def acceptRecoveryMessages: Receive = {
    case recoveryMsg: WorkflowRecoveryMessage =>
      controllerProcessor.processRecoveryMessage(recoveryMsg)
  }

  def recovering: Receive = {
    case NetworkMessage(
          _,
          WorkflowControlMessage(from, seqNum, ControlInvocation(_, FatalError(err)))
        ) =>
      // fatal error during recovery, fail
      controllerProcessor.sendToClient(FatalError(err))
      // re-throw the error to fail the actor
      throw err
    case NetworkMessage(id, WorkflowControlMessage(from, seqNum, payload)) =>
      controlInputPort.handleMessage(
        this.sender(),
        Constants.unprocessedBatchesCreditLimitPerSender, // Controller is assumed to have enough credits
        id,
        from,
        seqNum,
        payload
      )
    case invocation: ControlInvocation =>
      logger.info("Reject during recovery: " + invocation)
    case other =>
      logger.info("Ignore during recovery: " + other)
  }

  override def receive: Receive = {
    // load from checkpoint if available
    controllerConfig.stateRestoreConfig.controllerConf.fromCheckpoint match {
      case Some(chkptAlignment) =>
        logger.info("checkpoint found, start loading")
        val chkpt = CheckpointHolder.getCheckpoint(CONTROLLER, chkptAlignment)
        val startLoadingTime = System.currentTimeMillis()
        val serialization = SerializationExtension(context.system)
        // reload states:
        controllerProcessor = chkpt.load("controlState").toObject(serialization)
        controlInputPort = new NetworkInputPort[ControlPayload](
          this.actorId,
          controllerProcessor.handleControlPayloadOuter
        )
        controlInputPort.setFIFOState(chkpt.load("fifoState").toObject(serialization))
        // re-send outputs:
        chkpt
          .load("outputMessages")
          .toObject(serialization)
          .asInstanceOf[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]]
          .foreach {
            case (id, iter) =>
              iter.foreach { msg =>
                networkCommunicationActor ! SendRequest(id, msg.internalMessage) //re-assign ack id.
              }
          }
        logger.info(
          s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime) / 1000f}s"
        )
      case None =>
        controllerProcessor = new ControllerProcessor()
        controlInputPort = new NetworkInputPort[ControlPayload](
          this.actorId,
          controllerProcessor.handleControlPayloadOuter
        )
    }

    // passing non-serialized controller state
    controllerProcessor.initialize(
      controlInputPort,
      workflow,
      workflowScheduler,
      logManager,
      logStorage,
      networkCommunicationActor,
      context,
      controllerConfig
    )

    // restore workers:
    logger.info("start to restore workers")
    controllerProcessor.restoreWorkers()

    // set replay alignment and start
    controllerConfig.stateRestoreConfig.controllerConf.replayTo match {
      case Some(replayAlignment) =>
        controllerProcessor.enterReplay(
          replayAlignment,
          () => {
            unstashAll()
            context.become(running)
          }
        )
        forwardResendRequest orElse acceptRecoveryMessages orElse recovering
      case None =>
        // interrupt replay if checkpoint was taken during replay
        controllerProcessor.interruptReplay()
        running
    }
  }

  override def postStop(): Unit = {
    logger.info("Controller start to shutdown")
    controllerProcessor.terminate()
    logManager.terminate()
//    if (workflow.isCompleted) {
//      workflow.getAllWorkers.foreach { workerId =>
//        DeterminantLogStorage
//          .getLogStorage(
//            controllerConfig.supportFaultTolerance,
//            WorkflowWorker.getWorkerLogName(workerId)
//          )
//          .deleteLog()
//      }
    //logStorage.deleteLog()
//    }
    logger.info("stopped successfully!")
  }
}
