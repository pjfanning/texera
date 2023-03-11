package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.{CheckpointStats, InitialCheckpointStats}
import edu.uci.ics.amber.engine.common.ambermessage.{CheckpointCompleted, SnapshotMarker, WorkflowFIFOMessagePayload, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PendingCheckpoint(inputPort:NetworkInputPort,
                        selfId:ActorVirtualIdentity,
                        val marker:SnapshotMarker,
                        startCallback: SnapshotMarker => (SavedCheckpoint, InitialCheckpointStats),
                        finalizeCallback: PendingCheckpoint => Unit) {

  private val channelAligned = new mutable.HashSet[(ActorVirtualIdentity, Boolean)]()
  val dataToTake = new mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]()
  var chkpt:SavedCheckpoint = _
  var initialCheckpointStats:InitialCheckpointStats = _
  var startAlignmentTime = 0L

  def acceptSnapshotMarker(channelId: (ActorVirtualIdentity, Boolean)):Boolean = {
    if(inputPort.getActiveChannels.map(_._1).toSet.forall(x => !marker.involved.contains(x)) && !marker.involved.contains(selfId)){
      // skip checkpoint myself
      println(s"Checkpoint for $selfId is skipped as $marker requested")
      return true
    }
    if(channelAligned.isEmpty){
      // received first checkpoint
      val result = startCallback(marker)
      chkpt = result._1
      initialCheckpointStats = result._2
      startAlignmentTime = System.currentTimeMillis()
    }
    channelAligned.add(channelId)
    if(inputPort.getActiveChannels.filter(x => marker.involved.contains(x._1)).forall(channelAligned.contains)){
      // if all channels are aligned
      finalizeCallback(this)
      return true
    }
    false
  }

  def recordInput(channelId:(ActorVirtualIdentity, Boolean), payload:WorkflowFIFOMessagePayload): Unit ={
    if(channelAligned.contains(channelId)){
      return
    }
    dataToTake.getOrElseUpdate(channelId, new mutable.ArrayBuffer[WorkflowFIFOMessagePayload]()).append(payload)
  }

}
