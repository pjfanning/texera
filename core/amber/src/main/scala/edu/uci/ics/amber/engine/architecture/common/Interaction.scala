package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class Interaction {

  private val participants = mutable.HashMap[ActorVirtualIdentity, Int]()
  private val alignments = mutable.ArrayBuffer[Long]()
  private val checkpointCosts = mutable.ArrayBuffer[Long]()
  private val loadCosts = mutable.ArrayBuffer[Long]()

  def addParticipant(
      id: ActorVirtualIdentity,
      alignment: Long,
      checkpointCost: Long,
      loadCost: Long
  ): Unit = {
    participants(id) = participants.size
    alignments.append(alignment)
    checkpointCosts.append(checkpointCost)
    loadCosts.append(loadCost)
  }

  def getCheckpointCost(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => checkpointCosts(x))

  def getParticipants: Iterable[ActorVirtualIdentity] = participants.keys

  def getAlignment(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => alignments(x))

  def getLoadCost(id: ActorVirtualIdentity): Option[Long] =
    participants.get(id).map(x => loadCosts(x))

  def getTotalLoadCost: Long = loadCosts.sum

  def getAlignmentMap: Map[ActorVirtualIdentity, Long] =
    participants.map(x => x._1 -> alignments(x._2)).toMap

  def containsAlignment(id: ActorVirtualIdentity, alignment: Long): Boolean = {
    participants.contains(id) && alignments(participants(id)) == alignment
  }

  override def toString: String = {
    val sb = new StringBuilder()
    for (p <- participants) {
      val idx = p._2
      sb.append(p._1 + ": alignment = ")
      sb.append(alignments(idx))
      sb.append(" save cost = ")
      sb.append(checkpointCosts(idx))
      sb.append(" load cost = ")
      sb.append(loadCosts(idx))
      sb.append("\n")
    }
    sb.toString()
  }

}
