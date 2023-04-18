package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, MarkerAlignmentInternalPayload, MarkerCollectionSupport, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


class PendingCheckpoint(val actorId:ActorVirtualIdentity,
                        startTime:Long,
                        val stepCursorAtCheckpoint:Long,
                        val chkpt:SavedCheckpoint,
                        toAlign: Set[ChannelEndpointID]) extends MarkerCollectionSupport with AmberLogging{

  val aligned = new mutable.HashSet[ChannelEndpointID]()
  def isCompleted: Boolean = toAlign.subsetOf(aligned)

  def onReceiveMarker(channel: ChannelEndpointID): Unit = {
    aligned.add(channel)
    logger.info(s"start to record input channel current = ${aligned.size}, target = $toAlign")
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
      logger.info(
        s"local checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000f}s"
      )
    }
  }

  def onReceivePayload(channel: ChannelEndpointID, p: WorkflowFIFOMessagePayload): Unit = {
    if(!aligned.contains(channel)){
      chkpt.addInputData(channel, p)
    }
  }
}
