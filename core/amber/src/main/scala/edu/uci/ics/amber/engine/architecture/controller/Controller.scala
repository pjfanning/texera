package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{Address, Props}
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowRecoveryStatus
import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.concurrent.duration.DurationInt
import akka.pattern.ask
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.{EmptyLocalCheckpointManager, LocalCheckpointManager}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.concurrent.Await

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      AmberUtils.amberConfig.getBoolean("fault-tolerance.enable-determinant-logging"),
      WorkflowReplayConfig.empty
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    var supportFaultTolerance: Boolean,
    var stateRestoreConfig: WorkflowReplayConfig
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
    workflow: Workflow,
    controllerConfig: ControllerConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(
      CONTROLLER,
      parentNetworkCommunicationActorRef,
      controllerConfig.stateRestoreConfig.confs(CONTROLLER),
      controllerConfig.supportFaultTolerance
    ) {

  override def getLogName: String = "WF" + workflow.getWorkflowId().id + "-CONTROLLER"

  lazy val controlProcessor: ControlProcessor = new ControlProcessor(actorId, determinantLogger)

  override def postStop(): Unit = {
    logger.info("Controller start to shutdown")
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

  override val localCheckpointManager: LocalCheckpointManager = new EmptyLocalCheckpointManager()

  /** flow-control */
  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity): Int = {
    Constants.unprocessedBatchesCreditLimitPerSender
  }

  override def setupState(fromChkpt: Option[SavedCheckpoint], replayTo: Option[Long]): Unit = {
    // register controller itself and client
    networkCommunicationActor.waitUntil(RegisterActorRef(CONTROLLER, self))
    networkCommunicationActor.waitUntil(RegisterActorRef(CLIENT, context.parent))
    controlProcessor.initCP(workflow, controllerConfig,
      new WorkflowScheduler(
        networkCommunicationActor,
        context,
        logger,
        workflow,
        controllerConfig
      ), new GlobalRecoveryManager(
        () => {
          logger.info("Start global recovery")
          controlProcessor.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
        },
        () => {
          logger.info("global recovery complete!")
          controlProcessor.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
        }
      ), () =>{
        Await
          .result(
            context.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses,
            5.seconds
          )
          .asInstanceOf[Array[Address]]
      }, inputPort, context, logManager)
  }

  override def inputPayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    payload match {
      case control:ControlPayload =>
        controlProcessor.processControlPayload(channelEndpointID, control)
      case other =>
        logger.info(s"Controller cannot handle payload: $payload")
    }
  }
}
