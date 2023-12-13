package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.linksemantics._
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.{
  BroadcastPartitioning,
  HashBasedShufflePartitioning,
  OneToOnePartitioning,
  RangeBasedShufflePartitioning,
  RoundRobinPartitioning
}
import edu.uci.ics.amber.engine.common.AmberConfig.defaultBatchSize
import edu.uci.ics.amber.engine.common.virtualidentity.{PhysicalLinkIdentity, PhysicalOpIdentity}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class PartitionEnforcer(physicalPlan: PhysicalPlan) {

  // a map of an operator to its output partition info
  val outputPartitionInfos = new mutable.HashMap[PhysicalOpIdentity, PartitionInfo]()
  val linkMapping = new mutable.HashMap[PhysicalLinkIdentity, PhysicalLink]()

  def getOutputPartition(
      fromPhysicalOpId: PhysicalOpIdentity,
      fromPort: Int,
      toPhysicalOpId: PhysicalOpIdentity,
      inputPort: Int
  ): (PhysicalLink, PartitionInfo) = {
    val toPhysicalOp = physicalPlan.getOperator(toPhysicalOpId)
    val fromPhysicalOp = physicalPlan.getOperator(fromPhysicalOpId)

    // make sure this input is connected to this port
    assert(toPhysicalOp.getInputOperators(inputPort).contains(fromPhysicalOpId))

    // partition requirement of this PhysicalOp on this input port
    val part =
      toPhysicalOp.partitionRequirement.lift(inputPort).flatten.getOrElse(UnknownPartition())
    // output partition of the input
    val inputPart = outputPartitionInfos(fromPhysicalOpId)

    // input partition satisfies the requirement, and number of worker match
    if (inputPart.satisfies(part) && fromPhysicalOp.numWorkers == toPhysicalOp.numWorkers) {
      val linkStrategy = new PhysicalLink(
        fromPhysicalOp,
        fromPort,
        toPhysicalOp,
        inputPort,
        partitionings = fromPhysicalOp.identifiers.indices
          .map(i =>
            (
              OneToOnePartitioning(defaultBatchSize, Array(toPhysicalOp.identifiers(i))),
              Array(toPhysicalOp.identifiers(i))
            )
          )
          .toIterator
      )

      val outputPart = inputPart
      (linkStrategy, outputPart)
    } else {
      // we must re-distribute the input partitions
      val linkStrategy = part match {
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
              .toIterator
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
              .toIterator
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
              .toIterator
          )
        case BroadcastPartition() =>
          new PhysicalLink(
            fromPhysicalOp,
            fromPort,
            toPhysicalOp,
            inputPort,
            partitionings = fromPhysicalOp.identifiers.indices
              .map(i =>
                (
                  BroadcastPartitioning(defaultBatchSize, toPhysicalOp.identifiers),
                  toPhysicalOp.identifiers
                )
              )
              .toIterator
          )
        case UnknownPartition() =>
          new PhysicalLink(
            fromPhysicalOp,
            fromPort,
            toPhysicalOp,
            inputPort,
            partitionings = fromPhysicalOp.identifiers.indices
              .map(i =>
                (
                  RoundRobinPartitioning(defaultBatchSize, toPhysicalOp.identifiers),
                  toPhysicalOp.identifiers
                )
              )
              .toIterator
          )
      }
      val outputPart = part
      (linkStrategy, outputPart)
    }
  }

  def enforcePartition(): PartitioningPlan = {

    physicalPlan
      .topologicalIterator()
      .foreach(physicalOpId => {
        val physicalOp = physicalPlan.getOperator(physicalOpId)
        if (physicalPlan.getSourceOperatorIds.contains(physicalOpId)) {
          // get output partition info of the source operator
          val outPart =
            physicalOp.partitionRequirement.headOption.flatten.getOrElse(UnknownPartition())
          outputPartitionInfos.put(physicalOpId, outPart)
        } else {
          val inputPartitionsOnPort = new ArrayBuffer[PartitionInfo]()

          // for each input port, enforce partition requirement
          physicalOp.inputPorts.indices.foreach(port => {
            // all input PhysicalOpIds connected to this port
            val inputPhysicalOpIds = physicalOp.getInputOperators(port)

            val fromPort = physicalPlan.getUpstreamPhysicalLinks(physicalOpId).head.fromPort

            // the output partition info of each link connected from each input PhysicalOp
            // for each input PhysicalOp connected on this port
            // check partition requirement to enforce corresponding LinkStrategy
            val outputPartitions = inputPhysicalOpIds.map(inputPhysicalOpId => {
              val (linkStrategy, outputPart) =
                getOutputPartition(inputPhysicalOpId, fromPort, physicalOpId, port)
              linkMapping.put(linkStrategy.id, linkStrategy)
              outputPart
            })

            assert(outputPartitions.size == inputPhysicalOpIds.size)

            val inputPartitionOnPort = outputPartitions.reduce((a, b) => a.merge(b))
            inputPartitionsOnPort.append(inputPartitionOnPort)
          })

          assert(inputPartitionsOnPort.size == physicalOp.inputPorts.size)

          // derive the output partition info of this operator
          val outputPartitionInfo = physicalOp.derivePartition(inputPartitionsOnPort.toList)
          outputPartitionInfos.put(physicalOpId, outputPartitionInfo)
        }
      })

    // returns the complete physical plan with link strategies
    new PartitioningPlan(linkMapping.toMap)
  }

}
