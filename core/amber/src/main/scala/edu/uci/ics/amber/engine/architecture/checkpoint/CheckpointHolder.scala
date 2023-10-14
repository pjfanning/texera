package edu.uci.ics.amber.engine.architecture.checkpoint

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessagePayload
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import javax.swing.GroupLayout.Alignment
import scala.collection.mutable
import scala.util.Try

object CheckpointHolder {

  private val checkpoints =
    new mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[Long, SavedCheckpoint]]()

  private val checkpointsId = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[String, Long]]()

  private val completedCheckpoint = mutable.HashMap[ActorVirtualIdentity, mutable.HashSet[String]]()

  private val checkpointSizes = mutable.HashMap[String, Long]()

  def hasCheckpoint(id:ActorVirtualIdentity, checkpointId:String): Boolean = {
    completedCheckpoint.contains(id) && completedCheckpoint(id).contains(checkpointId)
  }

  def clear(): Unit = {
    checkpoints.clear()
    checkpointsId.clear()
    completedCheckpoint.clear()
  }

  def hasCheckpoint(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    checkpoints.contains(id) && checkpoints(id).contains(alignment)
  }

  def getCheckpoint(id: ActorVirtualIdentity, alignment: Long): SavedCheckpoint = {
    assert(hasCheckpoint(id, alignment))
    checkpoints(id)(alignment)
  }

  def getCheckpointAlignment(id: ActorVirtualIdentity, name: String): Option[Long] = {
    checkpointsId.get(id).flatMap(_.get(name))
  }

  def addCheckpoint(
                     id: ActorVirtualIdentity,
                     alignment: Long,
                     checkpointId:String,
                     checkpoint: SavedCheckpoint,
                     size:Long
  ): Unit = {
    val oldSize = checkpointSizes.getOrElseUpdate(checkpointId, 0L)
    checkpointSizes(checkpointId) = oldSize + size
    completedCheckpoint.getOrElseUpdate(id, new mutable.HashSet[String]()).add(checkpointId)
    checkpointsId.getOrElseUpdate(id, new mutable.HashMap[String, Long]())(checkpointId) = alignment
//    checkpoints.getOrElseUpdate(id, new mutable.HashMap[Long, SavedCheckpoint]())(alignment) =
//      checkpoint
    if(checkpoint != null){
      println(
        s"checkpoint $checkpointId stored for $id at alignment = ${alignment} size = ${checkpoint.size()} bytes"
      )
    }
  }
}
