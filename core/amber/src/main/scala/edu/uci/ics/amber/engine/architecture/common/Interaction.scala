package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class Interaction(initialState:Boolean = false) extends Serializable{

  private val participants = mutable.HashMap[ActorVirtualIdentity, CheckpointStats]()

  def addParticipant(
      id: ActorVirtualIdentity,
      info: CheckpointStats
  ): Unit = {
    participants(id) = info
  }

  def getBusyTime(id:ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.initialCheckpointStats.processedTime)
  }

  def getCheckpointCost(id: ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.initialCheckpointStats.checkpointStateTime + x.inputAlignmentTime)
  }

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys

  def getAlignment(id: ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.initialCheckpointStats.alignment)
  }

  def getLoadCost(id: ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => 0)
  }

  def getTotalLoadCost: Long =  {
    if(initialState){
      return 0
    }
    participants.values.map(x => 0).sum
  }

  def getAlignmentMap: Map[ActorVirtualIdentity, Long] =
    participants.map(x => x._1 -> x._2.initialCheckpointStats.alignment).toMap

  def containsAlignment(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    participants.contains(id) && participants(id).initialCheckpointStats.alignment == alignment
  }

  override def toString: String = {
    val sb = new StringBuilder()
    for (p <- participants) {
      val info = p._2
      sb.append(p._1 + ": alignment = ")
      sb.append(info.initialCheckpointStats.alignment)
      sb.append(" save cost = ")
      sb.append(info.initialCheckpointStats.checkpointStateTime)
      sb.append(" load cost = ")
      sb.append(0)
      sb.append("\n")
    }
    sb.toString()
  }

}
