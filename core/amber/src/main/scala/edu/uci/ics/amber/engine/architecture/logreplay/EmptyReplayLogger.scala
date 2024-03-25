package edu.uci.ics.amber.engine.architecture.logreplay
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.virtualidentity.{ChannelIdentity, EmbeddedControlMessageIdentity}

class EmptyReplayLogger extends ReplayLogger {

  override def drainCurrentLogRecords(step: Long): Array[ReplayLogRecord] = {
    Array.empty
  }

  def markAsReplayDestination(id: EmbeddedControlMessageIdentity): Unit = {}

  override def logCurrentStepWithMessage(
      step: Long,
      channelId: ChannelIdentity,
      msg: Option[WorkflowFIFOMessage]
  ): Unit = {}
}
