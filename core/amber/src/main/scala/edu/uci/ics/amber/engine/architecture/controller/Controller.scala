package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{Address, Props}
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.processing.{ControlProcessor, ControllerInternalPayloadManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkSenderActorRef
import edu.uci.ics.amber.engine.architecture.recovery.{ControllerReplayQueue, InternalPayloadManager}
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.concurrent.duration.DurationInt
import akka.pattern.ask
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessagePayloadWithPiggyback}
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
    val workflow: Workflow,
    val controllerConfig: ControllerConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(
      CONTROLLER,
      parentNetworkCommunicationActorRef,
      controllerConfig.stateRestoreConfig.confs(CONTROLLER),
      controllerConfig.supportFaultTolerance
    ) {

  override def getLogName: String = "WF" + workflow.getWorkflowId().id + "-CONTROLLER"

  // variables to be initialized by physical state mgr.
  var controlProcessor: ControlProcessor = _
  var replayQueue:ControllerReplayQueue = _

  def getAvailableNodes():Array[Address] = {
    Await
      .result(
        context.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses,
        5.seconds
      )
      .asInstanceOf[Array[Address]]
  }

  /** flow-control */
  override def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity): Int = {
    Constants.unprocessedBatchesCreditLimitPerSender
  }

  override def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit = {
    payload match {
      case control:ControlPayload =>
        if(replayQueue != null){
          replayQueue.enqueuePayload(channelEndpointID, control)
        }else{
          controlProcessor.processControlPayload(channelEndpointID, control)
        }
      case other =>
        logger.info(s"Controller cannot handle payload: $payload")
    }
  }

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

  override def internalPayloadManager: InternalPayloadManager = new ControllerInternalPayloadManager(this)
}
