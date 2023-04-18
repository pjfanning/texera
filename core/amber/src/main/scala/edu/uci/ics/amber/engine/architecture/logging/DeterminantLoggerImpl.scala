package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, OutsideWorldChannelEndpointID}

import scala.collection.mutable

class DeterminantLoggerImpl extends DeterminantLogger {

  private val tempLogs = mutable.ArrayBuffer[InMemDeterminant]()

  private val channelsToRecord:Set[ChannelEndpointID] = Set(OutsideWorldChannelEndpointID)

  private var currentChannel:ChannelEndpointID = _

  override def recordPayload(channel: ChannelEndpointID, payload: ControlPayload): Unit = {
    if(channelsToRecord.contains(channel)){
      tempLogs.append(RecordedPayload(currentChannel, payload))
    }
  }

  override def setCurrentSender(channel: ChannelEndpointID): Unit = {
    currentChannel = channel
    tempLogs.append(StepsOnChannel(currentChannel, totalValidStep))
  }

  def drainCurrentLogRecords(): Array[InMemDeterminant] = {
    if(tempLogs.nonEmpty && totalValidStep != tempLogs.last.asInstanceOf[StepsOnChannel].steps){
      tempLogs.append(StepsOnChannel(currentChannel, totalValidStep))
    }
    val result = tempLogs.toArray
    tempLogs.clear()
    result
  }
}
