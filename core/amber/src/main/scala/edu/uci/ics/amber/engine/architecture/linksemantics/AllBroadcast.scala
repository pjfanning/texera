package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  BroadcastPartitioning,
  Partitioning
}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLinkIdentity}

class AllBroadcast(
    from: PhysicalOp,
    fromPort: Int,
    to: PhysicalOp,
    toPort: Int,
    batchSize: Int
) extends LinkStrategy(from, fromPort, to, toPort, batchSize) {
  override def getPartitioning: Iterable[
    (ActorVirtualIdentity, PhysicalLinkIdentity, Partitioning, Seq[ActorVirtualIdentity])
  ] = {
    from.identifiers.map(x =>
      (x, id, BroadcastPartitioning(batchSize, to.identifiers), to.identifiers.toSeq)
    )
  }

}
