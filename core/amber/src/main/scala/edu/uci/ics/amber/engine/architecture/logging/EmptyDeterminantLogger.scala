package edu.uci.ics.amber.engine.architecture.logging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

class EmptyDeterminantLogger extends DeterminantLogger {

  override def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    Array.empty
  }

  override def setCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      msg: Option[WorkflowFIFOMessage]
  ): Unit = {}
}
