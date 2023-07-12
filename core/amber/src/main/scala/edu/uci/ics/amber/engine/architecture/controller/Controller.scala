package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{ActorRef, Address, Props}
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.Controller.recoveryDelay
import edu.uci.ics.amber.engine.architecture.controller.processing.{ControlProcessor, ControllerInternalPayloadManager}
import edu.uci.ics.amber.engine.architecture.recovery.{ControllerReplayQueue, InternalPayloadManager}
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.concurrent.duration.DurationInt
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.SetupLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, OutsideWorldChannelEndpointID, WorkflowExecutionPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.concurrent.Await

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval"))
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long]
)

object Controller {

  val recoveryDelay: Long = AmberUtils.amberConfig.getLong("fault-tolerance.delay-before-recovery")

  def props(
      workflow: Workflow,
      pipelinedRegionPlan: PipelinedRegionPlan,
      controllerConfig: ControllerConfig = ControllerConfig.default,
      parentNetworkCommunicationActorRef: ActorRef
  ): Props =
    Props(
      new Controller(
        workflow,
        pipelinedRegionPlan,
        controllerConfig,
        parentNetworkCommunicationActorRef
      )
    )
}

class Controller(
    val workflow: Workflow,
    val pipelinedRegionPlan: PipelinedRegionPlan,
    val controllerConfig: ControllerConfig,
    parentNetworkCommunicationActorRef: ActorRef
) extends WorkflowActor(
      CONTROLLER,
      parentNetworkCommunicationActorRef
    ) {

  override def getLogName: String = "WF" + workflow.getWorkflowId().id + "-CONTROLLER"

  // separate pipelined region dag (non-serializable) from workflow

  // variables to be initialized
  var controlProcessor: ControlProcessor = new ControlProcessor(this)
  var replayQueue:ControllerReplayQueue = _

  override def initState(): Unit = {
    // register controller itself and client
    controlProcessor.initCP(this)
    actorService.registerActorForNetworkCommunication(CONTROLLER, self)
    actorService.registerActorForNetworkCommunication(CLIENT, context.parent)
    handlePayloadAndMarker(OutsideWorldChannelEndpointID, SetupLogging())
  }

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

  override def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowExecutionPayload): Unit = {
    if(channelEndpointID.endpointWorker == CONTROLLER){
      logger.info(s"receive $payload from myself!!!")
    }
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

  override val internalPayloadManager: InternalPayloadManager = new ControllerInternalPayloadManager(this)
}
