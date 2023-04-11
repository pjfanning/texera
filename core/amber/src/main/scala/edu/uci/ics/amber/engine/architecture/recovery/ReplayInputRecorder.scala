package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, FIFOMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ReplayInputRecorder {
  private val channelData = new mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]()
  def recordPayload(channelId:ChannelEndpointID, payload:WorkflowFIFOMessagePayload): Unit ={
    if(payload.isInstanceOf[FIFOMarker]){
      return
    }
    channelData.getOrElseUpdate(channelId, new mutable.ArrayBuffer[WorkflowFIFOMessagePayload]()).append(payload)
  }

  def clearAll(): Unit ={
    channelData.clear()
  }

  def getRecordedInputForReplay:mutable.HashMap[ChannelEndpointID, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]] = channelData

}
