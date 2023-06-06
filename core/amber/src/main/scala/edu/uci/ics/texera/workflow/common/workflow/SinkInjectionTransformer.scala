package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc
import edu.uci.ics.texera.workflow.operators.sink.managed.ProgressiveSinkOpDesc
import edu.uci.ics.texera.workflow.operators.visualization.VisualizationOperator

object SinkInjectionTransformer {

  def transform(logicalPlan: LogicalPlan): LogicalPlan = {
    var resultPlan = logicalPlan

    logicalPlan.getTerminalOperators.foreach(terminalOpId => {
      val terminalOp = logicalPlan.getOperator(terminalOpId)
      if (! terminalOp.isInstanceOf[SinkOpDesc]) {
        terminalOp.operatorInfo.outputPorts.indices.foreach(out => {
          val sink = new ProgressiveSinkOpDesc()
          resultPlan = resultPlan
            .addOperator(sink)
            .addEdge(terminalOp.operatorID, sink.operatorID, out, 0)
        })
      }
    })

    // pre-process: set output mode for sink based on the visualization operator before it
    logicalPlan.getTerminalOperators.foreach(sinkOpId => {
      val sinkOp = logicalPlan.getOperator(sinkOpId)
      val upstream = logicalPlan.getUpstream(sinkOpId)
      if (upstream.nonEmpty) {
        (upstream.head, sinkOp) match {
          // match the combination of a visualization operator followed by a sink operator
          case (viz: VisualizationOperator, sink: ProgressiveSinkOpDesc) =>
            sink.setOutputMode(viz.outputMode())
            sink.setChartType(viz.chartType())
          case _ =>
          //skip
        }
      }
    })

  }



}
