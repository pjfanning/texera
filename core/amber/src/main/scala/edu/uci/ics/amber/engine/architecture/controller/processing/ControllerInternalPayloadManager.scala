package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ControllerInternalPayloadManager(controller:Controller) extends InternalPayloadManager with AmberLogging{

  override def actorId: ActorVirtualIdentity = controller.actorId

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        val stats = CheckpointStats(
          id,
          controller.inputPort.getFIFOState,
          controller.controlProcessor.outputPort.getFIFOState,
          controller.controlProcessor.determinantLogger.getStep,
          0)
        controller.controlProcessor.outputPort.broadcastMarker(payload)
      case _ => ???
    }
  }

  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit ={
    idempotentInternalPayload match {
      case CheckpointCompleted(id, step) => ???
      case RestoreFromCheckpoint(fromCheckpoint, replayTo) => ???
      case _ => ???
    }

  }

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    markerAlignmentInternalPayload match {
      case TakeCheckpoint(_, _) =>
        logger.info("start to take global checkpoint")
        val startTime = System.currentTimeMillis()
        val chkpt = new SavedCheckpoint()
        chkpt.attachSerialization(SerializationExtension(controller.context.system))
        chkpt.save("fifoState", controller.inputPort.getFIFOState)
        chkpt.save("controlState", controller.controlProcessor)
        val toAlign = new mutable.HashSet[ChannelEndpointID]
        val markerCollectionCountMap = controller.controlProcessor.execution.getAllWorkers.map{
          worker =>
            toAlign.add(ChannelEndpointID(worker, true))
            val mutableSet = controller.controlProcessor.execution.getOperatorExecution(worker).getWorkerInfo(worker).upstreamChannels
            worker -> mutableSet.toSet
        }.toMap
        val checkpointId = CheckpointHolder.generateCheckpointId
        controller.controlProcessor.outputPort.broadcastMarker(TakeCheckpoint(checkpointId, markerCollectionCountMap))
        val numControlSteps = controller.controlProcessor.determinantLogger.getStep
        new PendingCheckpoint(actorId, startTime, numControlSteps, chkpt, toAlign.toSet)
      case _ => ???
    }
  }

  override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {
    markerAlignmentInternalPayload match {
      case TakeCheckpoint(id, alignmentMap) =>

      case _ => ???
    }
  }
}
