package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()

  private var currentChannel: ChannelID = _

  private var lastStep = INIT_STEP

  override def setCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      message: Option[WorkflowFIFOMessage]
  ): Unit = {
    // by default, record all message content in control channels.
    if (currentChannel != channel || channel.isControl) {
      currentChannel = channel
      lastStep = step
      if (channel.isControl && message.isDefined) {
        tempLogs.append(ProcessingStepWithContent(message.get, step))
      } else {
        tempLogs.append(ProcessingStep(channel, step))
      }
    }
  }

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    if (lastStep != step) {
      lastStep = step
      tempLogs.append(ProcessingStep(currentChannel, step))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }
}
