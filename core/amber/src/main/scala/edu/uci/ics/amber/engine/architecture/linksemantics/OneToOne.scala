package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  OneToOnePartitioning,
  Partitioning
}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLink}

class OneToOne(from: PhysicalOp, fromPort: Int, to: PhysicalOp, toPort: Int, batchSize: Int)
    extends LinkStrategy(from, fromPort, to, toPort, batchSize) {
  override def getPartitioning: Iterable[
    (ActorVirtualIdentity, PhysicalLink, Partitioning, Seq[ActorVirtualIdentity])
  ] = {
    assert(from.numWorkers == to.numWorkers)
    from.identifiers.indices.map(i =>
      (
        from.identifiers(i),
        id,
        OneToOnePartitioning(batchSize, Array(to.identifiers(i))),
        Seq(to.identifiers(i))
      )
    )
  }
}
