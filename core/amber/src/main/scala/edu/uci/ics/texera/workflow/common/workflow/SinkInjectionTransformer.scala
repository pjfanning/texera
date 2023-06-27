package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import edu.uci.ics.texera.workflow.operators.sink.managed.ProgressiveSinkOpDesc
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationOperator

object SinkInjectionTransformer {

  def transform(logicalPlan: LogicalPlan, context: WorkflowContext): LogicalPlan = {
    var resultPlan = logicalPlan

    // for any terminal operator without a sink, add a sink
    logicalPlan.getTerminalOperators.foreach(terminalOpId => {
      val terminalOp = logicalPlan.getOperator(terminalOpId)
      if (!terminalOp.isInstanceOf[SinkOpDesc]) {
        terminalOp.operatorInfo.outputPorts.indices.foreach(out => {
          val sink = new ProgressiveSinkOpDesc()
          sink.setContext(context)
          resultPlan = resultPlan
            .addOperator(sink)
            .addEdge(terminalOp.operatorID, sink.operatorID, out, 0)
        })
      }
    })

    // for each sink:
    // set the corresponding upstream ID and port
    // set output mode based on the visualization operator before it
    // precondition: all the terminal operators are sinks
    resultPlan.getTerminalOperators.foreach(sinkOpId => {
      val sinkOp = resultPlan.getOperator(sinkOpId).asInstanceOf[ProgressiveSinkOpDesc]
      val upstream = resultPlan.getUpstream(sinkOpId).headOption
      val edge = resultPlan.links.find(l =>
        l.origin.operatorID == upstream.map(_.operatorID).orNull
          && l.destination.operatorID == sinkOpId
      )
      if (upstream.nonEmpty && edge.nonEmpty) {
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

    resultPlan
  }

}
