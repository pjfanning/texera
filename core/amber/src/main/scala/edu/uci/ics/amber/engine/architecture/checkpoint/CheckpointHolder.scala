package edu.uci.ics.amber.engine.architecture.checkpoint

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkMessage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import javax.swing.GroupLayout.Alignment
import scala.collection.mutable

object CheckpointHolder {
  private val checkpoints = new mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[Long, SavedCheckpoint]]()
  private val workflows = new mutable.HashMap[Long, Workflow]()

  def hasCheckpoint(id:ActorVirtualIdentity, alignment: Long): Boolean = {
    checkpoints.contains(id) && checkpoints(id).contains(alignment)
  }

  def addCheckpoint(id:ActorVirtualIdentity, alignment:Long, checkpoint:SavedCheckpoint): Unit ={
    checkpoints.getOrElseUpdate(id, new mutable.HashMap[Long, SavedCheckpoint]())(alignment) = checkpoint
    println(s"checkpoint stored for $id at alignment = $alignment")
  }

  def addWorkflow(alignment: Long, wf: Workflow): Unit ={
    workflows(alignment) = wf
    println(s"workflow stored for alignment = $alignment")
  }
}
