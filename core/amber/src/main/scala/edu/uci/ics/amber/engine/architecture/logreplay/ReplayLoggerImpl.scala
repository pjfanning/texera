package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

import scala.collection.mutable

class ReplayLoggerImpl extends ReplayLogger {

  private val tempLogs = mutable.ArrayBuffer[ReplayLogRecord]()

  private var currentChannel: ChannelID = _

  private var lastStep = INIT_STEP

  override def setCurrentStepWithMessage(
      step: Long,
      channel: ChannelID,
      message: Option[WorkflowFIFOMessage]
  ): Unit = {
    if (currentChannel == channel && message.isEmpty) {
      return
    }
    currentChannel = channel
    lastStep = step
    tempLogs.append(ProcessingStep(channel, step))
    if (message.isDefined) {
      tempLogs.append(MessageContent(message.get))
    }
  }

  def drainCurrentLogRecords(step: Long): Array[ReplayLogRecord] = {
    if (lastStep != step) {
      lastStep = step
      tempLogs.append(ProcessingStep(currentChannel, step))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }
}
