package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{Address, Cancellable, PoisonPill, Props}
import akka.pattern.ask
import akka.serialization.SerializationExtension
import akka.util.Timeout
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowRecoveryStatus, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.collection.mutable
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
      Map.empty
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    var supportFaultTolerance: Boolean,
    var replayRequest: Map[ActorVirtualIdentity, Long]
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
    controllerProcessor = new ControllerProcessor()
    controlInputPort = new NetworkInputPort[ControlPayload](
      this.actorId,
      controllerProcessor.handleControlPayloadOuter
    )
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
    controllerProcessor.execution.initExecutionState(workflow)
    // load state if available
    if (controllerConfig.replayRequest.nonEmpty) {
      val alignment = controllerConfig.replayRequest(CONTROLLER)
      val checkpointOpt = CheckpointHolder.findLastCheckpointOf(CONTROLLER, alignment)
      if (checkpointOpt.isDefined) {
        logger.info("checkpoint found, start loading")
        val startLoadingTime = System.currentTimeMillis()
        val serialization = SerializationExtension(context.system)
        // reload states:
        controllerProcessor = checkpointOpt.get.load("controlState").toObject(serialization)
        controlInputPort = new NetworkInputPort[ControlPayload](
          this.actorId,
          controllerProcessor.handleControlPayloadOuter
        )
        controlInputPort.setFIFOState(checkpointOpt.get.load("fifoState").toObject(serialization))
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
        // re-send outputs:
        checkpointOpt.get
          .load("outputMessages")
          .toObject(serialization)
          .asInstanceOf[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]]
          .foreach {
            case (id, iter) =>
              iter.foreach { msg =>
                networkCommunicationActor ! SendRequest(id, msg.internalMessage) //re-assign ack id.
              }
          }
        logger.info(s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime)/1000f}s")
      }
      controllerProcessor.enterReplay(
        alignment,
        () => {
          unstashAll()
          context.become(running)
        }
      )
      forwardResendRequest orElse acceptRecoveryMessages orElse recovering
    } else {
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
