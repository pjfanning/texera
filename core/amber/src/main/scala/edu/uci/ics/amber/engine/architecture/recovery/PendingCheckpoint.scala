package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{GlobalCheckpointMarker, WorkflowFIFOMessagePayload, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PendingCheckpoint(val actorId:ActorVirtualIdentity,
                        startTime:Long,
                        chkpt:SavedCheckpoint,
                        stepCursorAtCheckpoint:Long,
                        targetAlignmentCount:Long,
                        onComplete: () => Unit) extends AmberLogging{

  private val channelAligned = mutable.HashSet[(ActorVirtualIdentity, Boolean)]()

  def acceptSnapshotMarker(channelId: (ActorVirtualIdentity, Boolean)):Unit = {
    channelAligned.add(channelId)
    logger.info(s"start to record input channel current = ${channelAligned.size}, target = $targetAlignmentCount")
    if(isCompleted){
      // if all channels are aligned
      CheckpointHolder.addCheckpoint(
        actorId,
        stepCursorAtCheckpoint,
        chkpt
      )
      logger.info(
        s"checkpoint stored for $actorId at alignment = ${stepCursorAtCheckpoint} size = ${chkpt.size()} bytes"
      )
      onComplete()
      logger.info(
        s"local checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000f}s"
      )
    }
  }

  def isCompleted:Boolean = channelAligned.size == targetAlignmentCount

  def recordInput(channelId:(ActorVirtualIdentity, Boolean), payload:WorkflowFIFOMessagePayload): Unit ={
    if(!channelAligned.contains(channelId)){
      chkpt.addInputData(channelId, payload)
    }
  }

}
