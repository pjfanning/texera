package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.linksemantics._
import edu.uci.ics.amber.engine.common.AmberConfig.defaultBatchSize
import edu.uci.ics.amber.engine.common.virtualidentity.{PhysicalLinkIdentity, PhysicalOpIdentity}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class PartitionEnforcer(physicalPlan: PhysicalPlan) {

  // a map of an operator to its output partition info
  val outputPartitionInfos = new mutable.HashMap[PhysicalOpIdentity, PartitionInfo]()
  val linkMapping = new mutable.HashMap[PhysicalLinkIdentity, LinkStrategy]()

  def getOutputPartition(
      currentPhysicalOpId: PhysicalOpIdentity,
      fromPort: Int,
      inputPhysicalOpId: PhysicalOpIdentity,
      inputPort: Int
  ): (LinkStrategy, PartitionInfo) = {
    val currentPhysicalOp = physicalPlan.getOperator(currentPhysicalOpId)
    val inputPhysicalOp = physicalPlan.getOperator(inputPhysicalOpId)

    // make sure this input is connected to this port
    assert(currentPhysicalOp.getInputOperators(inputPort).contains(inputPhysicalOpId))

    // partition requirement of this PhysicalOp on this input port
    val part =
      currentPhysicalOp.partitionRequirement.lift(inputPort).flatten.getOrElse(UnknownPartition())
    // output partition of the input
    val inputPart = outputPartitionInfos(inputPhysicalOpId)

    // input partition satisfies the requirement, and number of worker match
    if (inputPart.satisfies(part) && inputPhysicalOp.numWorkers == currentPhysicalOp.numWorkers) {
      val linkStrategy =
        new OneToOne(inputPhysicalOp, fromPort, currentPhysicalOp, inputPort, defaultBatchSize)
      val outputPart = inputPart
      (linkStrategy, outputPart)
    } else {
      // we must re-distribute the input partitions
      val linkStrategy = part match {
        case HashPartition(hashColumnIndices) =>
          new HashBasedShuffle(
            inputPhysicalOp,
            fromPort,
            currentPhysicalOp,
            inputPort,
            defaultBatchSize,
            hashColumnIndices
          )
        case RangePartition(rangeColumnIndices, rangeMin, rangeMax) =>
          new RangeBasedShuffle(
            inputPhysicalOp,
            fromPort,
            currentPhysicalOp,
            inputPort,
            defaultBatchSize,
            rangeColumnIndices,
            rangeMin,
            rangeMax
          )
        case SinglePartition() =>
          new AllToOne(inputPhysicalOp, fromPort, currentPhysicalOp, inputPort, defaultBatchSize)
        case BroadcastPartition() =>
          new AllBroadcast(
            inputPhysicalOp,
            fromPort,
            currentPhysicalOp,
            inputPort,
            defaultBatchSize
          )
        case UnknownPartition() =>
          new FullRoundRobin(
            inputPhysicalOp,
            fromPort,
            currentPhysicalOp,
            inputPort,
            defaultBatchSize
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
                getOutputPartition(physicalOpId, fromPort, inputPhysicalOpId, port)
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
