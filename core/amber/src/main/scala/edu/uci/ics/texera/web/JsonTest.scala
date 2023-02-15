package edu.uci.ics.texera.web

import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING
import edu.uci.ics.texera.workflow.operators.nn.DNNOpExec

object JsonTest {

  def main(args: Array[String]): Unit = {

    val a = new DNNOpExec(List("a","b"),"c",1)
    a.readjustWeight()

  }
}

class JsonTest {}
