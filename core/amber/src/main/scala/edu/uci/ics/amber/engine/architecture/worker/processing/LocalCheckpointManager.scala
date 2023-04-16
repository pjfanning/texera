package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.recovery.PendingCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, EstimationMarker, FIFOMarker, GlobalCheckpointMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import scala.collection.mutable

abstract class LocalCheckpointManager {

  private val receivedMarkers = mutable.HashSet[Long]()
  private val pendingCheckpoints = mutable.HashMap[Long, PendingCheckpoint]()

  def doCheckpointEstimation(marker: EstimationMarker):Unit

  def prepareGlobalCheckpoint(channel:ChannelEndpointID, marker:GlobalCheckpointMarker):PendingCheckpoint

  def inputMarker(channel: ChannelEndpointID, marker:FIFOMarker):Unit = {
    marker match {
      case est @ EstimationMarker(id) =>
        if(!receivedMarkers.contains(id)){
          receivedMarkers.add(id)
          doCheckpointEstimation(est)
        }
      case c @ GlobalCheckpointMarker(id, _) =>
        if(!pendingCheckpoints.contains(id)){
          pendingCheckpoints(id) = prepareGlobalCheckpoint(channel, c)
        }
        pendingCheckpoints(id).acceptSnapshotMarker(channel)
        if(pendingCheckpoints(id).isCompleted){
          pendingCheckpoints.remove(id)
        }
    }
  }

  def inputPayload(channel:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit ={
    pendingCheckpoints.foreach{
      case (id, chkpt) =>
        if(!payload.isInstanceOf[FIFOMarker]){
          chkpt.recordInput(channel, payload)
        }
    }
  }
}


class EmptyLocalCheckpointManager extends LocalCheckpointManager{
  override def doCheckpointEstimation(marker: EstimationMarker): Unit = {}

  override def prepareGlobalCheckpoint(channel: ChannelEndpointID, marker: GlobalCheckpointMarker): PendingCheckpoint = {
    new PendingCheckpoint(SELF,0, new SavedCheckpoint(), 0,0, () => {})
  }
}
