package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

abstract class ReplayLogger {

  def setCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      msg: Option[WorkflowFIFOMessage]
  ): Unit

  def drainCurrentLogRecords(step: Long): Array[ReplayLogRecord]

}
