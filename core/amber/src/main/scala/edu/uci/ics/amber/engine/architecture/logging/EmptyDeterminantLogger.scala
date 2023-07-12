package edu.uci.ics.amber.engine.architecture.logging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowExecutionPayload}

class EmptyDeterminantLogger extends DeterminantLogger {

  override def setCurrentSenderWithPayload(
      channel: ChannelEndpointID,
      step: Long,
      payload: WorkflowExecutionPayload
  ): Unit = {}

  override def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = { Array.empty }

  override def enableOutputCommit(enabled: Boolean): Unit = {}
}
