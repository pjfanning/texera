package edu.uci.ics.amber.engine.architecture.worker.processing

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager._
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, PendingCheckpoint, RecoveryInternalQueueImpl, ReplayOrderEnforcer, WorkerCheckpointRestoreManager}
import edu.uci.ics.amber.engine.architecture.worker.{WorkerInternalQueue, WorkerInternalQueueImpl, WorkflowWorker}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted}
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointSupport, ambermessage}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, NeverCompleteMarkerCollection, OneTimeInternalPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

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
        worker.initiateSyncActionFromMain(() =>{
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
            estimatedCheckpointCost + worker.internalQueue.getDataQueueLength*400)
          worker.dataProcessor.outputPort.sendToClient(EstimationCompleted(actorId, id, stats))
        })
      case SetupReplay(id, fromCheckpoint, replayTo, confs) =>
        worker.isReplaying = true
        worker.dpThread.stop() // intentionally kill DP
        restoreManager.restoreFromCheckpointAndSetupReplay(id, fromCheckpoint, replayTo, confs, pending)
      case StartReplay(id) =>
        worker.dpThread.start()
        logger.info(s"$actorId starts replay")
      case _ => ???
    }
  }

  override def markerAlignmentStart(channel: ChannelEndpointID, payload: MarkerAlignmentInternalPayload, pendingCollections:mutable.HashMap[String, MarkerCollectionSupport]): Unit = {
    payload match {
      case TakeRuntimeGlobalCheckpoint(id, alignmentMap) =>
        if(worker.isReplaying){
          logger.info("doing replay, enforce replay checkpoints, ignore normal checkpoints.")
          return
        }
        if(alignmentMap.contains(worker.actorId)) {
          if (!CheckpointHolder.hasCheckpoint(actorId, id)) {
            worker.initiateSyncActionFromMain(() => {
              worker.dataProcessor.outputPort.broadcastMarker(payload)
              val chkpt = new SavedCheckpoint()
              val pending = new PendingCheckpoint(
                id,
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
              pending.onReceiveMarker(channel)
              if (!pending.isNoLongerPending) {
                pendingCollections(id) = pending
              }
            })
          } else {
            logger.info(s"already took checkpoint, ignore marker for $id")
            pendingCollections(id) = new NeverCompleteMarkerCollection()
          }
        }
      case _ => ???
    }
  }
}
