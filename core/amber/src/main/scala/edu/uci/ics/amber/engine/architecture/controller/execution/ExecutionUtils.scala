package edu.uci.ics.amber.engine.architecture.controller.execution

import edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkflowAggregatedState
import edu.uci.ics.amber.engine.architecture.worker.statistics.{
  PortTupleMetricsMapping,
  TupleMetrics
}
import edu.uci.ics.amber.engine.common.executionruntimestate.{OperatorMetrics, OperatorStatistics}

object ExecutionUtils {

  /**
    * Handle the case when a logical operator has two physical operators within a same region (e.g., Aggregate operator)
    */
  def aggregateMetrics(metrics: Iterable[OperatorMetrics]): OperatorMetrics = {
    if (metrics.isEmpty) {
      // Return a default OperatorMetrics if metrics are empty
      return OperatorMetrics(
        WorkflowAggregatedState.UNINITIALIZED,
        OperatorStatistics(Seq.empty, Seq.empty, 0, 0, 0, 0)
      )
    }

    val aggregatedState = aggregateStates(
      metrics.map(_.operatorState),
      WorkflowAggregatedState.COMPLETED,
      WorkflowAggregatedState.RUNNING,
      WorkflowAggregatedState.UNINITIALIZED,
      WorkflowAggregatedState.PAUSED,
      WorkflowAggregatedState.READY
    )

    def sumMetrics(
        extractor: OperatorMetrics => Iterable[PortTupleMetricsMapping]
    ): Seq[PortTupleMetricsMapping] = {
      val filteredMetrics = metrics.flatMap(extractor).filterNot(_.portId.internal)
      aggregatePortMetrics(filteredMetrics)
    }

    val inputMetricsSum = sumMetrics(_.operatorStatistics.inputMetrics)
    val outputMetricsSum = sumMetrics(_.operatorStatistics.outputMetrics)

    val numWorkersSum = metrics.map(_.operatorStatistics.numWorkers).sum
    val dataProcessingTimeSum = metrics.map(_.operatorStatistics.dataProcessingTime).sum
    val controlProcessingTimeSum = metrics.map(_.operatorStatistics.controlProcessingTime).sum
    val idleTimeSum = metrics.map(_.operatorStatistics.idleTime).sum

    OperatorMetrics(
      aggregatedState,
      OperatorStatistics(
        inputMetricsSum,
        outputMetricsSum,
        numWorkersSum,
        dataProcessingTimeSum,
        controlProcessingTimeSum,
        idleTimeSum
      )
    )
  }

  def aggregateStates[T](
      states: Iterable[T],
      completedState: T,
      runningState: T,
      uninitializedState: T,
      pausedState: T,
      readyState: T
  ): WorkflowAggregatedState = {
    states match {
      case _ if states.isEmpty                     => WorkflowAggregatedState.UNINITIALIZED
      case _ if states.forall(_ == completedState) => WorkflowAggregatedState.COMPLETED
      case _ if states.exists(_ == runningState)   => WorkflowAggregatedState.RUNNING
      case _ =>
        val unCompletedStates = states.filter(_ != completedState)
        if (unCompletedStates.forall(_ == uninitializedState)) {
          WorkflowAggregatedState.UNINITIALIZED
        } else if (unCompletedStates.forall(_ == pausedState)) {
          WorkflowAggregatedState.PAUSED
        } else if (unCompletedStates.forall(_ == readyState)) {
          WorkflowAggregatedState.RUNNING
        } else {
          WorkflowAggregatedState.UNKNOWN
        }
    }
  }

  def aggregatePortMetrics(
      metrics: Iterable[PortTupleMetricsMapping]
  ): Seq[PortTupleMetricsMapping] = {
    metrics
      .groupBy(_.portId)
      .view
      .map {
        case (portId, mappings) =>
          val totalCount = mappings.map(_.tupleMetrics.count).sum
          val totalSize = mappings.map(_.tupleMetrics.size).sum
          PortTupleMetricsMapping(portId, TupleMetrics(totalCount, totalSize))
      }
      .toSeq
  }
}
