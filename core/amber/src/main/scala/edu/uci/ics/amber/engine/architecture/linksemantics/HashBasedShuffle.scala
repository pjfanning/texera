package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  HashBasedShufflePartitioning,
  Partitioning
}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLink}

class HashBasedShuffle(
    from: PhysicalOp,
    fromPort: Int,
    to: PhysicalOp,
    toPort: Int,
    batchSize: Int,
    hashColumnIndices: Seq[Int]
) extends LinkStrategy(from, fromPort, to, toPort, batchSize) {
  override def getPartitioning: Iterable[
    (ActorVirtualIdentity, PhysicalLink, Partitioning, Seq[ActorVirtualIdentity])
  ] = {
    from.identifiers.map(x =>
      (
        x,
        id,
        HashBasedShufflePartitioning(batchSize, to.identifiers, hashColumnIndices),
        to.identifiers.toSeq
      )
    )
  }

}
