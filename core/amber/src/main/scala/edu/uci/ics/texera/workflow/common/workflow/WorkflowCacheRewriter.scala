package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import edu.uci.ics.texera.workflow.operators.source.cache.CacheSourceOpDesc

object WorkflowCacheRewriter {

  def transform(
    logicalPlan: LogicalPlan,
    storage: OpResultStorage,
    availableCache: Map[String, String], // key: operator ID in workflow, value: cache key in storage
    operatorsToUseCache: Set[String], // user-specified operators to reuse cache if possible
  ): LogicalPlan = {
    var resultPlan = logicalPlan

    operatorsToUseCache.intersect(availableCache.keySet).foreach(opId => {
      val cacheId = availableCache(opId)
      val materializationReader = new CacheSourceOpDesc(cacheId, storage)
      resultPlan = resultPlan.addOperator(materializationReader)
      // replace the connection of all outgoing edges of opId with the cache
      val edgesToReplace = resultPlan.getUpstreamEdges(opId)
      edgesToReplace.foreach(e => {
        resultPlan = resultPlan.removeEdge(e.origin.operatorID, e.destination.operatorID,
          e.origin.portOrdinal, e.destination.portOrdinal)
        resultPlan = resultPlan.addEdge(materializationReader.operatorID, e.destination.operatorID,
          0, e.destination.portOrdinal)
      })
    })

    // remove sinks directly connected to operators that are already cached
    val unnecessarySinks = resultPlan.getTerminalOperators.filter(sink => {
      availableCache.contains(resultPlan.getUpstream(sink).head.operatorID)
    })
    unnecessarySinks.foreach(o => {
      resultPlan = resultPlan.removeOperator(o)
    })

    // operators that are no longer reachable by any sink don't need to run
    val allOperators = resultPlan.operators.map(op => op.operatorID).toSet
    assert(allOperators.forall(o => resultPlan.getOperator(o).isInstanceOf[SinkOpDesc]))
    val usefulOperators = resultPlan.terminalOperators.flatMap(o => resultPlan.getAncestorOpIds(o)).toSet
    allOperators.diff(usefulOperators).foreach(o => {
      resultPlan = resultPlan.removeOperator(o)
    })

    resultPlan
  }

}
