package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import edu.uci.ics.texera.workflow.operators.sink.managed.ProgressiveSinkOpDesc
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationOperator

object SinkInjectionTransformer {

  def transform(logicalPlan: LogicalPlan): LogicalPlan = {
    var resultPlan = logicalPlan

    // for any terminal operator without a sink, add a sink
    val nonSinkTerminalOps = logicalPlan.getTerminalOperators.filter(opId =>
      ! logicalPlan.getOperator(opId).isInstanceOf[SinkOpDesc]
    )
    // for any operators marked as cache (view result) without a sink, add a sink
    val viewResultOps = logicalPlan.cachedOperatorIds.filter(opId =>
      ! logicalPlan.getDownstream(opId).exists(op => op.isInstanceOf[SinkOpDesc])
    )

    val operatorsToAddSink = (nonSinkTerminalOps ++ viewResultOps).toSet
    operatorsToAddSink.foreach(opId => {
      val op = logicalPlan.getOperator(opId)
      op.operatorInfo.outputPorts.indices.foreach(outPort => {
        val sink = new ProgressiveSinkOpDesc()
        resultPlan = resultPlan
          .addOperator(sink)
          .addEdge(op.operatorID, sink.operatorID, outPort)
      })
    })

    // check precondition: all the terminal operators should sinks now
    assert(resultPlan.getTerminalOperators.forall(o => resultPlan.getOperator(o).isInstanceOf[SinkOpDesc]))

    var finalCachedOpIds = Set[String]()

    // for each sink:
    // set the corresponding upstream ID and port
    // set output mode based on the visualization operator before it
    resultPlan.getTerminalOperators.foreach(sinkOpId => {
      val sinkOp = resultPlan.getOperator(sinkOpId).asInstanceOf[ProgressiveSinkOpDesc]
      val upstream = resultPlan.getUpstream(sinkOpId).headOption
      val edge = resultPlan.links.find(l =>
        l.origin.operatorID == upstream.map(_.operatorID).orNull
          && l.destination.operatorID == sinkOpId
      )
      assert(upstream.nonEmpty)
      if (upstream.nonEmpty && edge.nonEmpty) {
        finalCachedOpIds += upstream.get
        // set upstream ID and port
        sinkOp.setUpstreamId(upstream.get.operatorID)
        sinkOp.setUpstreamPort(edge.get.origin.portOrdinal)
        // set output mode for visualization operator
        (upstream.get, sinkOp) match {
          // match the combination of a visualization operator followed by a sink operator
          case (viz: VisualizationOperator, sink: ProgressiveSinkOpDesc) =>
            sink.setOutputMode(viz.outputMode())
            sink.setChartType(viz.chartType())
          case _ =>
          //skip
        }
      }
    })

    resultPlan.copy(cachedOperatorIds = finalCachedOpIds.toList)
  }

}
