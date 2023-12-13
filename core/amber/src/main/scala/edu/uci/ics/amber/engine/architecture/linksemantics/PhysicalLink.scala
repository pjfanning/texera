package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.Partitioning
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLinkIdentity}

class PhysicalLink(
    val fromOp: PhysicalOp,
    val fromPort: Int,
    val toOp: PhysicalOp,
    val toPort: Int,
    val partitionings: Iterator[Tuple2[Partitioning, Array[ActorVirtualIdentity]]]
) extends Serializable {

  val id: PhysicalLinkIdentity = PhysicalLinkIdentity(fromOp.id, fromPort, toOp.id, toPort)
  def totalReceiversCount: Long = toOp.numWorkers

}
