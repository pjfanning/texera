package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessagePayload}

abstract class DeterminantLogger {

  def setCurrentSenderWithPayload(channel: ChannelID, step:Long, payload:WorkflowFIFOMessagePayload):Unit

  def drainCurrentLogRecords(step:Long): Array[InMemDeterminant]

}
