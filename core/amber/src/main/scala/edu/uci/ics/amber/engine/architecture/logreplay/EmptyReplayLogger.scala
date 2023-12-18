package edu.uci.ics.amber.engine.architecture.logreplay
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

class EmptyReplayLogger extends ReplayLogger {

  override def drainCurrentLogRecords(step: Long): Array[ReplayLogRecord] = {
    Array.empty
  }

  def markAsReplayDestination(id: String): Unit = {}

  override def logCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      msg: Option[WorkflowFIFOMessage]
  ): Unit = {}
}
