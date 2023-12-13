package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.deploysemantics.{PhysicalLink, PhysicalOp}
import edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings._
import edu.uci.ics.amber.engine.common.AmberConfig.defaultBatchSize
import edu.uci.ics.amber.engine.common.virtualidentity.{PhysicalLinkIdentity, PhysicalOpIdentity}

import scala.collection.mutable

class PartitionEnforcer(physicalPlan: PhysicalPlan) {


  val linkMapping = new mutable.HashMap[PhysicalLinkIdentity, PhysicalLink]()

  def enforcePartition(): PartitioningPlan = {
    // a map of an operator to its output partition info
    val outputPartitionInfos = new mutable.HashMap[PhysicalOpIdentity, PartitionInfo]()
    physicalPlan
      .topologicalIterator()
      .foreach(physicalOpId => {
        val physicalOp = physicalPlan.getOperator(physicalOpId)
        val outputPartitionInfo = if (physicalPlan.getSourceOperatorIds.contains(physicalOpId)) {
          // get output partition info of the source operator
          physicalOp.partitionRequirement.headOption.flatten.getOrElse(UnknownPartition())
        } else {
          val inputPartitionings = enforcePartitionRequirement(physicalOp, outputPartitionInfos.toMap)
          assert(inputPartitionings.length == physicalOp.inputPorts.size)
          // derive the output partition info of this operator
          physicalOp.derivePartition(inputPartitionings.toList)
        }
        outputPartitionInfos.put(physicalOpId, outputPartitionInfo)
      })

    // returns the complete physical plan with link strategies
    new PartitioningPlan(linkMapping.toMap)
  }


  private def enforcePartitionRequirement(physicalOp: PhysicalOp, partitionInfos: Map[PhysicalOpIdentity, PartitionInfo]): Array[PartitionInfo] = {
    // for each input port, enforce partition requirement
    physicalOp.inputPorts.indices.map(port => {
      // all input PhysicalOpIds connected to this port
      val inputPhysicalOpIds = physicalOp.getInputOperators(port)

      val fromPort = physicalPlan.getUpstreamPhysicalLinks(physicalOp.id).head.fromPort

      // the output partition info of each link connected from each input PhysicalOp
      // for each input PhysicalOp connected on this port
      // check partition requirement to enforce corresponding LinkStrategy
      val outputPartitions = inputPhysicalOpIds.map(inputPhysicalOpId => {
        val inputPartitionInfo = partitionInfos(inputPhysicalOpId)
        val (physicalLink, outputPart) =
          getOutputPartitionInfo(inputPhysicalOpId, fromPort, physicalOp.id, port, inputPartitionInfo)
        linkMapping.put(physicalLink.id, physicalLink)
        outputPart
      })

      assert(outputPartitions.size == inputPhysicalOpIds.size)

      outputPartitions.reduce((a, b) => a.merge(b))
    }).toArray
  }

  private def getOutputPartitionInfo(
      fromPhysicalOpId: PhysicalOpIdentity,
      fromPort: Int,
      toPhysicalOpId: PhysicalOpIdentity,
      inputPort: Int,
      inputPartitionInfo: PartitionInfo
  ): (PhysicalLink, PartitionInfo) = {
    val toPhysicalOp = physicalPlan.getOperator(toPhysicalOpId)
    val fromPhysicalOp = physicalPlan.getOperator(fromPhysicalOpId)

    // make sure this input is connected to this port
    assert(toPhysicalOp.getInputOperators(inputPort).contains(fromPhysicalOpId))

    // partition requirement of this PhysicalOp on this input port
    val part =
      toPhysicalOp.partitionRequirement.lift(inputPort).flatten.getOrElse(UnknownPartition())

    // input partition satisfies the requirement, and number of worker match
    if (inputPartitionInfo.satisfies(part) && fromPhysicalOp.numWorkers == toPhysicalOp.numWorkers) {
      val physicalLink = new PhysicalLink(
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
          .toArray
      )
      val outputPart = inputPartitionInfo
      (physicalLink, outputPart)
    } else {
      // we must re-distribute the input partitions
      val physicalLink = part match {
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
      val outputPart = part
      (physicalLink, outputPart)
    }
  }




}
