package edu.uci.ics.amber.engine.architecture.scheduling.resourcePolicies

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.scheduling.Region
import edu.uci.ics.amber.engine.architecture.scheduling.config.ChannelConfig.generateChannelConfigs
import edu.uci.ics.amber.engine.architecture.scheduling.config.LinkConfig.toPartitioning
import edu.uci.ics.amber.engine.architecture.scheduling.config.WorkerConfig.generateWorkerConfigs
import edu.uci.ics.amber.engine.architecture.scheduling.config.{
  LinkConfig,
  OperatorConfig,
  ResourceConfig
}
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowRuntimeStatistics
import edu.uci.ics.texera.workflow.common.workflow.{PartitionInfo, PhysicalPlan}

import scala.collection.mutable

/**
  * Allocates resources for a given region and its operators.
  *
  * @param region The region for which to allocate resources.
  * @return A tuple containing:
  *         1) A new Region instance with new configuration.
  *         2) An estimated cost of the workflow with the new configuration,
  *         represented as a Double value (currently set to 0, but will be
  *         updated in the future).
  * Notes:
  * 1. If the count of in running historical records for one operator are
  *    less than 3, there is no need to allocate the workers, because it is
  *    too fast. ( < 3, the first in running record might has warmup. So we
  *    need at least one useful in running record to get the speed/throughput)
  * 2. The historical data needs to have the same version with the current
  *    workflow.
  * 3. The speed unit can be not exactly 1 second. It is more likely to represent
  *    a ratio.
  */
class NaiveResourceAllocator(physicalPlan: PhysicalPlan, executionClusterInfo: ExecutionClusterInfo)
    extends ResourceAllocator {

  private val threshold = 40

  private val historicalRuntimeStats = new HistoricalRuntimeStats()

  private val operatorConfigs = new mutable.HashMap[PhysicalOpIdentity, OperatorConfig]()

  private val linkPartitionInfos = new mutable.HashMap[PhysicalLink, PartitionInfo]()

  private val linkConfigs = new mutable.HashMap[PhysicalLink, LinkConfig]()

  private def avgHistoryPerOperator(physicalOpIdentity: PhysicalOpIdentity): (Double, Double) = {
    val executionHistory = {
      historicalRuntimeStats.getHistoricalRuntimeStatsByOperatorVersion(physicalOpIdentity)
    }
    if (executionHistory.isEmpty || executionHistory.length <= threshold)
      return (0, 0)

    val recordsByExecutionId: Map[Int, List[WorkflowRuntimeStatistics]] =
      executionHistory
        .filter(stats => Option(stats.getStatus).contains(1.toByte))
        .groupBy(_.getExecutionId.intValue())
        .view
        .mapValues(_.toList)
        .toMap

    def trimLeadingZeroPairs(
        records: List[WorkflowRuntimeStatistics]
    ): List[WorkflowRuntimeStatistics] =
      records.dropWhile(record =>
        record.getInputTupleCnt.intValue() == 0 && record.getOutputTupleCnt.intValue() == 0
      )

    val filteredResult: List[WorkflowRuntimeStatistics] =
      recordsByExecutionId.values.toList.flatMap(trimLeadingZeroPairs)

    val workingTime = if (filteredResult.nonEmpty) {
      filteredResult
        .map(record =>
          (2000000000 - record.getIdleTime.doubleValue() - record.getControlProcessingTime
            .doubleValue()) / 1000000000 / record.getNumWorkers.intValue()
        )
        .sum
    } else 2

    val avgInputTupleCnt = if (filteredResult.nonEmpty) {
      filteredResult
        .map(record => record.getInputTupleCnt.doubleValue() / record.getNumWorkers.intValue())
        .sum / workingTime
    } else 0

    val avgOutputTupleCnt = if (filteredResult.nonEmpty) {
      filteredResult
        .map(record => record.getOutputTupleCnt.doubleValue() / record.getNumWorkers.intValue())
        .sum / workingTime
    } else 0

    (avgInputTupleCnt, avgOutputTupleCnt)
  }

  private def avgHistory(region: Region): Map[PhysicalOp, Double] = {
    val parallelizableOperators =
      region.getOperators.filter(op =>
        op.parallelizable && (op.suggestedWorkerNum.isEmpty || op.suggestedWorkerNum.get == 0)
      )
    parallelizableOperators.map(op => op -> avgHistoryPerOperator(op.id)._1).toMap
  }

  private def allocateWorkers(
      speeds: Map[PhysicalOp, Double],
      totNumWorkers: Int
  ): Map[PhysicalOp, Int] = {
    var numWorkers = speeds.map {
      case (opId, speed) =>
        val workersForOp = math.max(1, (speed / speeds.values.sum * totNumWorkers).toInt)
        opId -> workersForOp
    }
    val restNumWorkers = totNumWorkers - numWorkers.values.sum

    for (_ <- 1 to restNumWorkers) {
      val j = numWorkers.minBy {
        case (opId, workerCount) =>
          workerCount.toDouble / speeds(opId)
      }._1
      numWorkers = numWorkers.updated(j, numWorkers(j) + 1)
    }

    numWorkers
  }
  def allocate(region: Region): (Region, Double) = {
    val histories = avgHistory(region)

    var estimatedCost = 0.0
    var operators: Set[PhysicalOp] = region.getOperators
    if (histories.values.sum != 0) {
      val speeds: Map[PhysicalOp, Double] = histories.map {
        case (op, throughPut) => op -> (if (throughPut != 0) 1 / throughPut else 1)
      }
      val numWorkers =
        allocateWorkers(speeds, executionClusterInfo.getAvailableNumOfWorkers(region))

      // calculate estimated cost
      for (i <- region.getOperators) {
        if (numWorkers.contains(i))
          estimatedCost = Math.min(numWorkers(i) * speeds(i), estimatedCost)
      }

      operators = region.getOperators.map { op =>
        numWorkers.get(op) match {
          case Some(workerNum) => op.withSuggestedWorkerNum(workerNum)
          case None            => op
        }
      }
    }

    val opToOperatorConfigMapping = operators
      .map(physicalOp => physicalOp.id -> OperatorConfig(generateWorkerConfigs(physicalOp)))
      .toMap

    operatorConfigs ++= opToOperatorConfigMapping

    propagatePartitionRequirement(
      region,
      physicalPlan,
      linkPartitionInfos,
      operatorConfigs,
      linkConfigs
    )

    val linkToLinkConfigMapping = region.getLinks.map { physicalLink =>
      physicalLink -> LinkConfig(
        generateChannelConfigs(
          operatorConfigs(physicalLink.fromOpId).workerConfigs.map(_.workerId),
          operatorConfigs(physicalLink.toOpId).workerConfigs.map(_.workerId),
          toPortId = physicalLink.toPortId,
          linkPartitionInfos(physicalLink)
        ),
        toPartitioning(
          operatorConfigs(physicalLink.toOpId).workerConfigs.map(_.workerId),
          linkPartitionInfos(physicalLink)
        )
      )
    }.toMap

    linkConfigs ++= linkToLinkConfigMapping

    val resourceConfig = ResourceConfig(opToOperatorConfigMapping, linkToLinkConfigMapping)

    (region.copy(resourceConfig = Some(resourceConfig)), estimatedCost)

  }
}
