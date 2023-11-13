package edu.uci.ics.amber.engine.architecture.logging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessagePayload}

class EmptyDeterminantLogger extends DeterminantLogger {
  override def setCurrentSenderWithPayload(
      channel: ChannelID,
      step: Long,
      payload: WorkflowFIFOMessagePayload
  ): Unit = {}

  override def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    Array.empty
  }
}
