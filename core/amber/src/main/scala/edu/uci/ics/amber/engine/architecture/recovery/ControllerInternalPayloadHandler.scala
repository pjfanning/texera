package edu.uci.ics.amber.engine.architecture.recovery

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowRecoveryStatus
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.RegisterActorRef
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

class ControllerInternalPayloadHandler(controller:Controller) extends InternalPayloadHandler(controller.actorId) {

  override def handleCommand(channel:ChannelEndpointID, controlCommand: ControlInvocation): Unit = {
    controller.controlProcessor.processControlPayload(channel, controlCommand)
  }

//  override def doCheckpointEstimation(marker: EstimationMarker): Unit = {
//    controller.controlProcessor.outputPort.broadcastMarker(marker)
//  }
//
//  override def prepareGlobalCheckpoint(channel: ChannelEndpointID, marker: GlobalCheckpointMarker): PendingCheckpoint = {
//    logger.info("start to take global checkpoint")
//    val startTime = System.currentTimeMillis()
//    val chkpt = new SavedCheckpoint()
//    chkpt.attachSerialization(SerializationExtension(controller.controlProcessor.actorContext.system))
//    chkpt.save("fifoState", controller.inputPort.getFIFOState)
//    chkpt.save("controlState", controller.controlProcessor)
//    val markerCollectionCountMap = controller.controlProcessor.execution.getAllWorkers.map{
//      worker =>
//        worker -> controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).upstreamChannelCount
//    }.toMap
//    val checkpointId = CheckpointHolder.generateCheckpointId
//    controller.controlProcessor.outputPort.broadcastMarker(GlobalCheckpointMarker(checkpointId, markerCollectionCountMap))
//    val onComplete = () =>{controller.controlProcessor.asyncRPCClient.send(ReportCheckpointCompleted(), CONTROLLER)}
//    new PendingCheckpoint(actorId, startTime, chkpt, controller.controlProcessor.determinantLogger.getStep, controller.inputPort.getActiveChannels.size, onComplete)
//  }
//
//  override def restoreStateFrom(savedCheckpoint: Option[SavedCheckpoint], replayTo: Option[Long]): Unit = {
//    // register controller itself and client
//    controller.networkCommunicationActor.waitUntil(RegisterActorRef(CONTROLLER, controller.self))
//    controller.networkCommunicationActor.waitUntil(RegisterActorRef(CLIENT, controller.context.parent))
//    controller.controlProcessor.initCP(controller.workflow, controller.controllerConfig,
//      new WorkflowScheduler(
//        controller.networkCommunicationActor,
//        controller.context,
//        logger,
//        controller.workflow,
//        controller.controllerConfig
//      ), new GlobalRecoveryManager(
//        () => {
//          logger.info("Start global recovery")
//          controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
//        },
//        () => {
//          logger.info("global recovery complete!")
//          controller.controlProcessor.asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
//        }
//      ), controller.getAvailableNodes, controller.inputPort, controller.context, controller.logManager)
//  }
}
