package edu.uci.ics.amber.engine.architecture.deploysemantics

import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  BroadcastPartitioning,
  HashBasedShufflePartitioning,
  OneToOnePartitioning,
  Partitioning,
  RangeBasedShufflePartitioning,
  RoundRobinPartitioning
}
import edu.uci.ics.amber.engine.common.AmberConfig.defaultBatchSize
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalLinkIdentity}
import edu.uci.ics.texera.workflow.common.workflow.{
  BroadcastPartition,
  HashPartition,
  PartitionInfo,
  RangePartition,
  SinglePartition,
  UnknownPartition
}

object PhysicalLink {
  def apply(
      fromPhysicalOp: PhysicalOp,
      fromPort: Int,
      toPhysicalOp: PhysicalOp,
      inputPort: Int,
      part: PartitionInfo
  ): PhysicalLink = {
    part match {
      case HashPartition(hashColumnIndices) =>
        new PhysicalLink(
          fromPhysicalOp,
          fromPort,
          toPhysicalOp,
          inputPort,
          partitionings = fromPhysicalOp.identifiers.indices
            .map(_ =>
              (
                HashBasedShufflePartitioning(
                  defaultBatchSize,
                  toPhysicalOp.identifiers,
                  hashColumnIndices
                ),
                toPhysicalOp.identifiers
              )
            )
            .toArray
        )
      case RangePartition(rangeColumnIndices, rangeMin, rangeMax) =>
        new PhysicalLink(
          fromPhysicalOp,
          fromPort,
          toPhysicalOp,
          inputPort,
          partitionings = fromPhysicalOp.identifiers.indices
            .map(i =>
              (
                RangeBasedShufflePartitioning(
                  defaultBatchSize,
                  toPhysicalOp.identifiers,
                  rangeColumnIndices,
                  rangeMin,
                  rangeMax
                ),
                toPhysicalOp.identifiers
              )
            )
            .toArray
        )
      case SinglePartition() =>
        assert(toPhysicalOp.numWorkers == 1)
        new PhysicalLink(
          fromPhysicalOp,
          fromPort,
          toPhysicalOp,
          inputPort,
          partitionings = fromPhysicalOp.identifiers.indices
            .map(i =>
              (
                OneToOnePartitioning(defaultBatchSize, Array(toPhysicalOp.identifiers.head)),
                toPhysicalOp.identifiers
              )
            )
            .toArray
        )
      case BroadcastPartition() =>
        new PhysicalLink(
          fromPhysicalOp,
          fromPort,
          toPhysicalOp,
          inputPort,
          partitionings = fromPhysicalOp.identifiers.indices
            .map(_ =>
              (
                BroadcastPartitioning(defaultBatchSize, toPhysicalOp.identifiers),
                toPhysicalOp.identifiers
              )
            )
            .toArray
        )
      case UnknownPartition() =>
        new PhysicalLink(
          fromPhysicalOp,
          fromPort,
          toPhysicalOp,
          inputPort,
          partitionings = fromPhysicalOp.identifiers.indices
            .map(_ =>
              (
                RoundRobinPartitioning(defaultBatchSize, toPhysicalOp.identifiers),
                toPhysicalOp.identifiers
              )
            )
            .toArray
        )
    }
  }
}
class PhysicalLink(
    val fromOp: PhysicalOp,
    val fromPort: Int,
    val toOp: PhysicalOp,
    val toPort: Int,
    val partitionings: Array[(Partitioning, Array[ActorVirtualIdentity])]
) extends Serializable {

  val id: PhysicalLinkIdentity = PhysicalLinkIdentity(fromOp.id, fromPort, toOp.id, toPort)
  def totalReceiversCount: Long = toOp.numWorkers

}
