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

  lazy val allSenderChannelStates:Map[ActorVirtualIdentity, Map[(ActorVirtualIdentity, Boolean),Long]] = {
    val result = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[(ActorVirtualIdentity, Boolean), Long]]()
    participants.foreach{
      case (src, info) =>
        info.outputWatermarks.foreach{
          case (dst, sent) =>
            result.getOrElseUpdate(src, new mutable.HashMap[(ActorVirtualIdentity, Boolean), Long])(dst) = sent
        }
    }
    result.mapValues(_.toMap).toMap
  }

  lazy val allReceiverChannelStates: Map[ActorVirtualIdentity, Map[(ActorVirtualIdentity, Boolean),Long]] ={
    val result = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[(ActorVirtualIdentity, Boolean), Long]]()
    participants.foreach{
      case (dst, info) =>
        info.inputWatermarks.foreach{
          case (src, received) =>
            result.getOrElseUpdate(src._1, new mutable.HashMap[(ActorVirtualIdentity, Boolean), Long])((dst, src._2)) = received
        }
    }
    result.mapValues(_.toMap).toMap
  }

  def getDownstreams(id:ActorVirtualIdentity): Iterable[(ActorVirtualIdentity, Boolean)] ={
    participants.get(id) match {
      case Some(value) => value.outputWatermarks.keys
      case None => Iterable.empty
    }
  }

  def getBusyTime(id:ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.processedTime)
  }

  def getCheckpointCost(id: ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.saveStateTime)
  }

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys

  def getAlignment(id: ActorVirtualIdentity): Option[Long] = {
    if(initialState){
      return Some(0)
    }
    participants.get(id).map(x => x.alignment)
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
    participants.map(x => x._1 -> x._2.alignment).toMap

  def containsAlignment(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    participants.contains(id) && participants(id).alignment == alignment
  }

  override def toString: String = {
    val sb = new StringBuilder()
    for (p <- participants) {
      val info = p._2
      sb.append(p._1 + ": alignment = ")
      sb.append(info.alignment)
      sb.append(" save cost = ")
      sb.append(info.saveStateTime)
      sb.append(" load cost = ")
      sb.append(0)
      sb.append("\n")
    }
    sb.toString()
  }

}
