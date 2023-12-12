package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.Partitioning
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLinkIdentity}

abstract class LinkStrategy(
    val from: PhysicalOp,
    val fromPort: Int,
    val to: PhysicalOp,
    val toPort: Int,
    val batchSize: Int
) extends Serializable {

  val id: PhysicalLinkIdentity = PhysicalLinkIdentity(from.id, fromPort, to.id, toPort)
  private var currentCompletedCount = 0

  def incrementCompletedReceiversCount(): Unit = currentCompletedCount += 1

  def isCompleted: Boolean = currentCompletedCount == totalReceiversCount

  def totalReceiversCount: Long = to.numWorkers

  // returns Iterable of (sender, link id, sender's partitioning, set of receivers)
  def getPartitioning: Iterable[
    (ActorVirtualIdentity, PhysicalLinkIdentity, Partitioning, Seq[ActorVirtualIdentity])
  ]
}
