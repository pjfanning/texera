package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

abstract class DeterminantLogger {

  def setCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      msg: Option[WorkflowFIFOMessage]
  ): Unit

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant]

}
