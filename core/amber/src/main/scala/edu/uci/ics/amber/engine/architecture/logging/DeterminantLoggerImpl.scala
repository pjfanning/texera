package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, OutsideWorldChannelEndpointID}

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()

  private val channelsToRecord:Set[ChannelEndpointID] = Set(OutsideWorldChannelEndpointID)

  private var currentChannel:ChannelEndpointID = _

  private var lastStep = INIT_STEP

  private var outputCommitEnabled = false

  override def recordPayload(channel: ChannelEndpointID, payload: ControlPayload): Unit = {
    if(channelsToRecord.contains(channel)){
      tempLogs.append(RecordedPayload(currentChannel, payload))
    }
  }

  override def setCurrentSender(channel: ChannelEndpointID, step: Long): Unit = {
    if(currentChannel != channel){
      currentChannel = channel
      lastStep = step
      tempLogs.append(StepsOnChannel(currentChannel, step))
    }
  }

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    if(!outputCommitEnabled){
      return Array.empty
    }
    if(lastStep != step){
      lastStep = step
      tempLogs.append(StepsOnChannel(currentChannel, step))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }

  override def enableOutputCommit(enabled: Boolean): Unit = {
    outputCommitEnabled = enabled
  }
}
