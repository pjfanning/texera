package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessagePayload}

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()

  private var currentChannel: ChannelID = _

  private var lastStep = INIT_STEP

  override def setCurrentSenderWithPayload(
      channel: ChannelID,
      step: Long,
      payload: WorkflowFIFOMessagePayload
  ): Unit = {
    // by default, record all message content in control channels.
    if (currentChannel != channel || channel.isControl) {
      currentChannel = channel
      lastStep = step
      val recordedPayload = if (channel.isControl) {
        payload
      } else {
        null
      }
      tempLogs.append(ProcessingStep(currentChannel, step, recordedPayload))
    }
  }

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    if (lastStep != step) {
      lastStep = step
      tempLogs.append(ProcessingStep(currentChannel, step, null))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }
}
