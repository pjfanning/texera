package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.serialization.SerializationExtension
import akka.util.Timeout
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.EstimationCompleted
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.recovery.{
  ControllerCheckpointRestoreManager,
  InternalPayloadManager,
  PendingCheckpoint
}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelEndpointID,
  IdempotentInternalPayload,
  MarkerAlignmentInternalPayload,
  MarkerCollectionSupport,
  NeverCompleteMarkerCollection,
  OneTimeInternalPayload
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

class ControllerInternalPayloadManager(controller: Controller)
    extends InternalPayloadManager
    with AmberLogging {

  override def actorId: ActorVirtualIdentity = controller.actorId

  implicit val initializeTimeout: Timeout = 10.seconds

  val restoreManager = new ControllerCheckpointRestoreManager(controller)

  override def handlePayload(
      channel: ChannelEndpointID,
      idempotentInternalPayload: IdempotentInternalPayload
  ): Unit = {
    idempotentInternalPayload match {
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(controller, true)
      case NoOp() =>
      // no op
      case _ => ???
    }

  }

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        controller.controlProcessor.outputPort.broadcastMarker(payload)
        val stats = CheckpointStats(
          controller.controlProcessor.cursor.getStep,
          controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          0,
          0
        )
        controller.controlProcessor.outputPort.sendToClient(EstimationCompleted(actorId, id, stats))
      case ReplayWorkflow(id, replayConfig) =>
        controller.isReplaying = true
        restoreManager.replayConfig = replayConfig
        val controllerReplayConf = replayConfig.confs(actorId)
        controller.controlProcessor.replayPlan = replayConfig
        restoreManager.restoreFromCheckpointAndSetupReplay(
          id,
          controllerReplayConf.fromCheckpoint,
          controllerReplayConf.replayTo,
          controllerReplayConf.checkpointConfig,
          pending
        )
      case _ => ???
    }
  }

  override def markerAlignmentStart(
      channel: ChannelEndpointID,
      markerAlignmentInternalPayload: MarkerAlignmentInternalPayload,
      pendingCollections: mutable.HashMap[String, MarkerCollectionSupport]
  ): Unit = {
    markerAlignmentInternalPayload match {
      case TakeRuntimeGlobalCheckpoint(id, _) =>
        logger.info("start to take global checkpoint")
        if (!CheckpointHolder.hasCheckpoint(actorId, id)) {
          val toAlign = mutable.HashSet[ChannelEndpointID](ChannelEndpointID(CONTROLLER, true))
          val markerCollectionCountMap = controller.controlProcessor.execution.getAllWorkers.map {
            worker =>
              val channelToAlign = ChannelEndpointID(worker, true)
              controller.controlProcessor.outputPort.addOutputChannel(
                channelToAlign
              ) // in case the channel is not established
              toAlign.add(channelToAlign)
              val mutableSet = controller.controlProcessor.execution
                .getOperatorExecution(worker)
                .getWorkerInfo(worker)
                .upstreamChannels
              worker -> mutableSet.toSet
          }.toMap
          controller.controlProcessor.outputPort.broadcastMarker(
            TakeRuntimeGlobalCheckpoint(id, markerCollectionCountMap)
          )
          val chkpt = new SavedCheckpoint()
          chkpt.attachSerialization(SerializationExtension(controller.context.system))
          val numControlSteps = controller.controlProcessor.cursor.getStep
          val pending = new PendingCheckpoint(
            id,
            id,
            actorId,
            System.currentTimeMillis(),
            numControlSteps,
            controller.inputPort.getFIFOState,
            controller.controlProcessor.outputPort.getFIFOState,
            0,
            chkpt,
            toAlign.toSet
          )
          pending.setOnComplete(restoreManager.onCheckpointCompleted)
          pending.checkpointDone = true
          pending.initialCheckpointTime = restoreManager.fillCheckpoint(pending)
          pending.onReceiveMarker(channel)
          if (!pending.isNoLongerPending) {
            pendingCollections(id) = pending
          }
        } else {
          logger.info(s"already took checkpoint, ignore marker for $id")
          pendingCollections(id) = new NeverCompleteMarkerCollection()
        }
      case _ => ???
    }
  }
}
