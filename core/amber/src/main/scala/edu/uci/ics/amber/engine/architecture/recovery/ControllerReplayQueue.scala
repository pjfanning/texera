package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}

import scala.collection.mutable

class ControllerReplayQueue(controlProcessor:ControlProcessor, replayOrderEnforcer: ReplayOrderEnforcer, processPayload:(ChannelEndpointID, ControlPayload) => Unit){

  private val messageQueues = mutable
    .HashMap[ChannelEndpointID, mutable.Queue[ControlPayload]]()

  def enqueuePayload(channelEndpointID: ChannelEndpointID, payload:ControlPayload): Unit = {
    messageQueues.getOrElseUpdate(channelEndpointID, new mutable.Queue()).enqueue(payload)
    var continue = true
    while(continue){
      replayOrderEnforcer.forwardReplayProcess(controlProcessor.determinantLogger.getStep)
      val currentChannel = replayOrderEnforcer.currentChannel
      if(messageQueues.getOrElseUpdate(currentChannel, new mutable.Queue()).nonEmpty){
        processPayload(currentChannel, messageQueues(currentChannel).dequeue())
      }else{
        continue = false
      }
    }
  }

  def processAllMessagesInQueue(): Unit ={
    messageQueues.foreach{
      case (channel, payloads) =>
        payloads.foreach{
          payload =>
            controlProcessor.processControlPayload(channel, payload)
        }
    }
    messageQueues.clear()
  }


}
