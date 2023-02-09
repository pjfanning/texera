package edu.uci.ics.amber.engine.architecture.checkpoint

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkMessage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import javax.swing.GroupLayout.Alignment
import scala.collection.mutable
import scala.util.Try

object CheckpointHolder {
  private val checkpoints =
    new mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[Long, SavedCheckpoint]]()

  def clear(): Unit = {
    checkpoints.clear()
  }

  def hasCheckpoint(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    checkpoints.contains(id) && checkpoints(id).contains(alignment)
  }

  def findLastCheckpointOf(id: ActorVirtualIdentity, alignment: Long): Option[SavedCheckpoint] = {
    if (checkpoints.contains(id)) {
      Try(
        checkpoints(id).map(x => (alignment - x._1, x._2)).filter(_._1 >= 0).minBy(_._1)._2
      ).toOption
    } else {
      None
    }
  }

  def addCheckpoint(
      id: ActorVirtualIdentity,
      alignment: Long,
      checkpoint: SavedCheckpoint
  ): Unit = {
    checkpoints.getOrElseUpdate(id, new mutable.HashMap[Long, SavedCheckpoint]())(alignment) =
      checkpoint
  }
}
