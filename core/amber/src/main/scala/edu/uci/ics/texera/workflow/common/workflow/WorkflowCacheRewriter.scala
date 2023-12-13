package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.Utils.objectMapper
import edu.uci.ics.texera.web.service.{ExecutionsMetadataPersistService, WorkflowCacheChecker}
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.operators.source.cache.CacheSourceOpDesc
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationConstants

import scala.collection.mutable

object WorkflowCacheRewriter {

  def transform(
      logicalPlan: LogicalPlan,
      lastCompletedPlan: Option[LogicalPlan],
      storage: OpResultStorage,
      opsToReuseCache: Set[OperatorIdentity]
  ): LogicalPlan = {
    val validCachesFromLastExecution =
      new WorkflowCacheChecker(lastCompletedPlan, logicalPlan).getValidCacheReuse

    var resultPlan = logicalPlan
    // an operator can reuse cache if
    // 1: the user wants the operator to reuse past result
    // 2: the operator is equivalent to the last run
    val opsCanUseCache = opsToReuseCache.intersect(validCachesFromLastExecution)

//    // remove sinks directly connected to operators that are already reusing cache
//    val unnecessarySinks = resultPlan.getTerminalOperatorIds.filter(sink => {
//      opsCanUseCache.contains(resultPlan.getUpstreamOps(sink).head.operatorIdentifier)
//    })
//    unnecessarySinks.foreach(o => {
//      resultPlan = resultPlan.removeOperator(o)
//    })

    opsCanUseCache.foreach(opId => {
      val opToReuseCache = lastCompletedPlan.get.getOperator(opId)
      // replace the connection of all outgoing edges of opId with the cache
      val edgesToReplace = resultPlan.getDownstreamEdges(opId)
      // To reuse the same cache source if the data comes from the same output port,
      // we maintain a mapping from port index to cache source desc.
      val cacheSources = new mutable.HashMap[Int, CacheSourceOpDesc]()
      edgesToReplace.foreach(e => {
        resultPlan = resultPlan.removeEdge(
          e.origin.operatorId,
          e.destination.operatorId,
          e.origin.portOrdinal,
          e.destination.portOrdinal
        )
        val materializationReader = cacheSources.getOrElseUpdate(e.origin.portOrdinal,
          new CacheSourceOpDesc(opToReuseCache.outputPortsInfo(e.origin.portOrdinal).storage.get))
        resultPlan = resultPlan.addOperator(materializationReader)
        resultPlan = resultPlan.addEdge(
          materializationReader.operatorIdentifier,
          e.destination.operatorId,
          0,
          e.destination.portOrdinal
        )
      })
    })

    // after an operator is replaced with reading from cached result
    // its upstream operators can be removed if it's not used by other sinks
    val allOperators = resultPlan.operators.map(op => op.operatorIdentifier).toSet
    val sinkOps =
      resultPlan.operators.filter(op => op.resultStorage.isDefined).map(o => o.operatorIdentifier)
    val usefulOperators = sinkOps ++ sinkOps.flatMap(o => resultPlan.getAncestorOpIds(o)).toSet
    // remove operators that are no longer reachable by any sink
    allOperators
      .diff(usefulOperators.toSet)
      .foreach(o => {
        resultPlan = resultPlan.removeOperator(o)
      })

    assert(
      resultPlan.getTerminalOperatorIds.forall(o =>
        resultPlan.getOperator(o).resultStorage.isDefined
      )
    )

    resultPlan.propagateWorkflowSchema(None)

    resultPlan

  }

}
