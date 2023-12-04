package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage

import scala.collection.mutable

class ReplayLogGenerator {
  val orderEnforcer: ReplayOrderEnforcer = new ReplayOrderEnforcer()

  def generate(
                   logStorage: ReplayLogStorage,
                 ): Tuple2[mutable.Queue[ProcessingStep], mutable.Queue[WorkflowFIFOMessage]] = {
    val logs = logStorage.getReader.mkLogRecordIterator().toArray
    val steps = mutable.Queue[ProcessingStep]()
    val messages = mutable.Queue[WorkflowFIFOMessage]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        messages.enqueue(message)
      case other =>
        throw new RuntimeException(s"cannot handle $other in the log")
    }
     Tuple2(steps, messages)
  }
}
