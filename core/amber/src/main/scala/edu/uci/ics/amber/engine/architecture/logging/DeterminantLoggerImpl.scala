package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.ChannelStepCursor.INIT_STEP
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, ControllerChannelEndpointID, OutsideWorldChannelEndpointID, WorkflowExecutionPayload, WorkflowFIFOMessagePayload}

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()

  private val channelsToRecord:Set[ChannelEndpointID] = Set(OutsideWorldChannelEndpointID, ControllerChannelEndpointID)

  private var currentChannel:ChannelEndpointID = _

  private var lastStep = INIT_STEP

  private var outputCommitEnabled = false

  override def setCurrentSenderWithPayload(channel: ChannelEndpointID, step: Long, payload: WorkflowExecutionPayload): Unit = {
    if(currentChannel != channel || channelsToRecord.contains(channel)){
      currentChannel = channel
      lastStep = step
      val recordedPayload = if(channelsToRecord.contains(channel)){
        payload
      }else{
        null
      }
      tempLogs.append(StepsOnChannel(currentChannel, step, recordedPayload))
    }
  }

  def drainCurrentLogRecords(step: Long): Array[InMemDeterminant] = {
    if(!outputCommitEnabled){
      return Array.empty
    }
    if(lastStep != step){
      lastStep = step
      tempLogs.append(StepsOnChannel(currentChannel, step, null))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }

  override def enableOutputCommit(enabled: Boolean): Unit = {
    outputCommitEnabled = enabled
  }
}
