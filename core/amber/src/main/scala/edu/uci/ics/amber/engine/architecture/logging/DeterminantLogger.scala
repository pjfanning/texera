package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}

abstract class DeterminantLogger {

  def setCurrentStepWithMessage(
      step: Long,
      msg:WorkflowFIFOMessage
  ): Unit

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant]

}
