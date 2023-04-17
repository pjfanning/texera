package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class FIFOMarkerCollectionState(expectedMarkerCount:Long){
  private var markerReceived = 0L

  def acceptMarker(channel:ChannelEndpointID):Unit = {
    markerReceived += 1
  }
  def isCompleted:Boolean = markerReceived == expectedMarkerCount

  def acceptInputPayload(channel:ChannelEndpointID, payload: WorkflowFIFOMessagePayload):Unit = {}
}


class PendingCheckpoint(val actorId:ActorVirtualIdentity,
                        startTime:Long,
                        chkpt:SavedCheckpoint,
                        stepCursorAtCheckpoint:Long,
                        expectedMarkerCount:Long) extends FIFOMarkerCollectionState(expectedMarkerCount) with AmberLogging{

  private val channelAligned = mutable.HashSet[ChannelEndpointID]()

  def acceptMarker(channelId: ChannelEndpointID):Unit = {
    channelAligned.add(channelId)
    logger.info(s"start to record input channel current = ${channelAligned.size}, target = $expectedMarkerCount")
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

  def recordInput(channelId:ChannelEndpointID, payload:WorkflowFIFOMessagePayload): Unit ={
    if(!channelAligned.contains(channelId)){
      chkpt.addInputData(channelId, payload)
    }
  }

}
