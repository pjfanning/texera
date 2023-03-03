package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.GetReplayAlignmentHandler.ReplayAlignmentInfo
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class Interaction extends Serializable{

  private val participants = mutable.HashMap[ActorVirtualIdentity, ReplayAlignmentInfo]()

  def addParticipant(
      id: ActorVirtualIdentity,
      info: ReplayAlignmentInfo
  ): Unit = {
    participants(id) = info
  }

  lazy val allSenderChannelStates:Map[ActorVirtualIdentity, Map[ActorVirtualIdentity,Long]] = {
    val result = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[ActorVirtualIdentity, Long]]()
    participants.foreach{
      case (src, info) =>
        info.outputWatermarks.foreach{
          case (dst, sent) =>
            result.getOrElseUpdate(src, new mutable.HashMap[ActorVirtualIdentity, Long])(dst) = sent
        }
    }
    result.mapValues(_.toMap).toMap
  }

  lazy val allReceiverChannelStates: Map[ActorVirtualIdentity, Map[ActorVirtualIdentity, Long]] ={
    val result = mutable.HashMap[ActorVirtualIdentity, mutable.HashMap[ActorVirtualIdentity, Long]]()
    participants.foreach{
      case (dst, info) =>
        info.inputWatermarks.foreach{
          case (src, received) =>
            result.getOrElseUpdate(src, new mutable.HashMap[ActorVirtualIdentity, Long])(dst) = received
        }
    }
    result.mapValues(_.toMap).toMap
  }

  def getBusyTime(id:ActorVirtualIdentity): Option[Long] = {
    participants.get(id).map(x => x.processedTime)
  }

  def getCheckpointCost(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => x.estimatedCheckpointTime)

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys

  def getAlignment(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => x.alignment)

  def getLoadCost(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => x.estimatedLoadTime)

  def getTotalLoadCost: Long =  participants.values.map(_.estimatedLoadTime).sum

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
      sb.append(info.estimatedCheckpointTime)
      sb.append(" load cost = ")
      sb.append(info.estimatedLoadTime)
      sb.append("\n")
    }
    sb.toString()
  }

}
