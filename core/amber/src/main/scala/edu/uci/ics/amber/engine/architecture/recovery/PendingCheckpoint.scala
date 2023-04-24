package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.NoOp
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, MarkerAlignmentInternalPayload, MarkerCollectionSupport, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


class PendingCheckpoint(val actorId:ActorVirtualIdentity,
                        var startTime:Long,
                        val stepCursorAtCheckpoint:Long,
                        var fifoInputState:Map[ChannelEndpointID, Long],
                        var fifoOutputState:Map[ChannelEndpointID, Long],
                        var initialCheckpointTime:Long,
                        val chkpt:SavedCheckpoint,
                        toAlign: Set[ChannelEndpointID]) extends MarkerCollectionSupport with AmberLogging{

  val aligned = new mutable.HashSet[ChannelEndpointID]()
  def isCompleted: Boolean = toAlign.subsetOf(aligned)

  def onReceiveMarker(channel: ChannelEndpointID): Unit = {
    aligned.add(channel)
    logger.info(s"finish recording input channel current = ${aligned.size}, target = $toAlign")
  }

  def onReceivePayload(channel: ChannelEndpointID, p: WorkflowFIFOMessagePayload): Unit = {
    if(!aligned.contains(channel) && toAlign.contains(channel)){
      if(p.isInstanceOf[AmberInternalPayload]){
        chkpt.addInputData(channel, NoOp())
      }else{
        chkpt.addInputData(channel, p)
      }
    }
  }
}
