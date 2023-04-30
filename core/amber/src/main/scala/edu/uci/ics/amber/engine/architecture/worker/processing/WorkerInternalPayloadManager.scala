package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint, RecoveryInternalQueueImpl, ReplayOrderEnforcer, WorkerCheckpointRestoreManager}
import edu.uci.ics.amber.engine.architecture.worker.{WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted}
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, ambermessage}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class WorkerInternalPayloadManager(worker:WorkflowWorker) extends InternalPayloadManager with AmberLogging {

  val restoreManager = new WorkerCheckpointRestoreManager(worker)

  override def actorId: ActorVirtualIdentity = worker.actorId

  override def handlePayload(channel:ChannelEndpointID, payload: IdempotentInternalPayload): Unit = {
    payload match {
      case SetupLogging() =>
        InternalPayloadManager.setupLoggingForWorkflowActor(worker, true)
      case NoOp() =>
        // no op
      case _ => ???
    }
  }

  override def handlePayload(payload: OneTimeInternalPayload): Unit = {
    payload match {
      case EstimateCheckpointCost(id) =>
        worker.executeThroughDP(() =>{
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          var estimatedCheckpointCost = 0
          if(worker.dataProcessor.operatorOpened) {
            worker.dataProcessor.operator match {
              case support: CheckpointSupport =>
                estimatedCheckpointCost = support.getEstimatedCheckpointTime
              case _ =>
            }
          }
          val stats = CheckpointStats(
            worker.dataProcessor.cursor.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            0,
            estimatedCheckpointCost + worker.dataProcessor.internalQueue.getDataQueueLength)
          worker.dataProcessor.outputPort.sendToClient(EstimationCompleted(actorId, id, stats))
        })
      case LoadStateAndReplay(id, fromCheckpoint, seqMap, replayTo, confs) =>
        worker.isReplaying = true
        worker.dpThread.stop() // intentionally kill DP
        restoreManager.restoreFromCheckpointAndSetupReplay(id, fromCheckpoint, seqMap, replayTo, confs, pending)
      case _ => ???
    }
  }

  override def markerAlignmentStart(payload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(id, alignmentMap) =>
        worker.executeThroughDP(() =>{
          val chkpt = new SavedCheckpoint()
          chkpt.attachSerialization(SerializationExtension(worker.context.system))
          worker.dataProcessor.outputPort.broadcastMarker(payload)
          val pending = new PendingCheckpoint(
            id,
            worker.actorId,
            System.currentTimeMillis(),
            worker.dataProcessor.cursor.getStep,
            worker.inputPort.getFIFOState,
            worker.dataProcessor.outputPort.getFIFOState,
            0,
            chkpt,
            alignmentMap(worker.actorId))
          pending.setOnComplete(restoreManager.onCheckpointCompleted)
          pending.checkpointDone = true
          pending.initialCheckpointTime = restoreManager.fillCheckpoint(pending)
          pending
        })
      case _ => ???
    }
  }
}
