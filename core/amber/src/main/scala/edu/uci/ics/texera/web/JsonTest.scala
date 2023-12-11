package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

import java.net.URI

object JsonTest {

  def main(args: Array[String]): Unit = {
    val a = RUNNING
    val om = Utils.objectMapper

    val s = ReplayLogStorage.getLogStorage(Some(new URI("file:///recovery-logs/0/0/")))
    s.getReader("WF0-CSVFileScan-operator-b1c5eda5-d2ee-4d32-b39c-8039809a51d4-main-0")
      .mkLogRecordIterator()
      .foreach {
        println
      }

    val str = om.writeValueAsString(a)
    println(str)

    val des = om.readValue(str, classOf[WorkflowAggregatedState])
    println(des)

  }
}

class JsonTest {}
