package edu.uci.ics.amber.engine.architecture.linksemantics

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  Partitioning,
  RangeBasedShufflePartitioning
}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLink}

class RangeBasedShuffle(
    from: PhysicalOp,
    fromPort: Int,
    to: PhysicalOp,
    toPort: Int,
    batchSize: Int,
    rangeColumnIndices: Seq[Int],
    rangeMin: Long,
    rangeMax: Long
) extends LinkStrategy(from, fromPort, to, toPort, batchSize) {
  override def getPartitioning: Iterable[
    (ActorVirtualIdentity, PhysicalLink, Partitioning, Seq[ActorVirtualIdentity])
  ] = {
    from.identifiers.map(x =>
      (
        x,
        id,
        RangeBasedShufflePartitioning(
          batchSize,
          to.identifiers,
          rangeColumnIndices,
          rangeMin,
          rangeMax
        ),
        to.identifiers.toSeq
      )
    )
  }
}
