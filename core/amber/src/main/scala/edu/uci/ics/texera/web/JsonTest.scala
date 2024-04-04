package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.common.{AmberUtils, SerializedState}
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

object JsonTest {

  def main(args: Array[String]): Unit = {
    AmberUtils.startActorWorker(None)
    val a = Map(1 -> 2, 3 -> "1231234")
    val s = SerializedState.fromObjectToString(a)
    val b: AnyRef = SerializedState.stringToObject(s)
    println(b)
  }
}

class JsonTest {}
