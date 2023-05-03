package edu.uci.ics.amber.engine.architecture.checkpoint

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessagePayload
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import javax.swing.GroupLayout.Alignment
import scala.collection.mutable
import scala.util.Try

object CheckpointHolder {

  private var uniqueId = 0L

  def generateCheckpointId: String = {
    uniqueId += 1
    s"global_checkpoint-$uniqueId"
  }

  def generateEstimationId(time:Long):String = {
    uniqueId += 1
    s"estimation-$time-$uniqueId"
  }

  private val checkpoints =
    new mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[Long, SavedCheckpoint]]()

  private val checkpointIdMap = mutable.HashMap[(ActorVirtualIdentity, Long), (String, String)]()

  private val completedCheckpoint = mutable.HashMap[ActorVirtualIdentity, mutable.HashSet[String]]()

  def hasCheckpoint(id:ActorVirtualIdentity, checkpointId:String): Boolean = {
    completedCheckpoint.contains(id) && completedCheckpoint(id).contains(checkpointId)
  }

  def clear(): Unit = {
    checkpoints.clear()
  }

  def hasCheckpoint(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    checkpoints.contains(id) && checkpoints(id).contains(alignment)
  }

  def getCheckpoint(id: ActorVirtualIdentity, alignment: Long): SavedCheckpoint = {
    assert(hasCheckpoint(id, alignment))
    checkpoints(id)(alignment)
  }

  def findLastCheckpointOf(id: ActorVirtualIdentity, alignment: Long): Option[(Long, String, String)] = {
    if (checkpoints.contains(id)) {
      val res = Try(
        checkpoints(id).map(x => (alignment - x._1, x._1)).filter(_._1 >= 0).minBy(_._1)._2
      ).toOption
      if(res.isDefined){
        val ids = checkpointIdMap((id, res.get))
        Some((res.get, ids._1, ids._2))
      }else{
        None
      }
    } else {
      None
    }
  }

  def addCheckpoint(
                     id: ActorVirtualIdentity,
                     alignment: Long,
                     snapshotId:String,
                     markerId:String,
                     checkpoint: SavedCheckpoint
  ): Unit = {
    completedCheckpoint.getOrElseUpdate(id, new mutable.HashSet[String]()).add(snapshotId)
    checkpointIdMap((id, alignment)) = (snapshotId, markerId)
    checkpoints.getOrElseUpdate(id, new mutable.HashMap[Long, SavedCheckpoint]())(alignment) =
      checkpoint
    if(checkpoint != null){
      println(
        s"checkpoint $markerId stored for $id at alignment = ${alignment} size = ${checkpoint.size()} bytes"
      )
    }
  }
}
