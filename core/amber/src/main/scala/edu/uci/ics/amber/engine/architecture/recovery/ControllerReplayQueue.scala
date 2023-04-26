package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ControllerReplayQueue(controlProcessor:ControlProcessor, replayOrderEnforcer: ReplayOrderEnforcer, processPayload:(ChannelEndpointID, ControlPayload) => Unit){

  private val messageQueues = mutable
    .HashMap[ChannelEndpointID, mutable.Queue[ControlPayload]]()

  def getAllMessages:Iterable[(ChannelEndpointID,ControlPayload)] = {
    val buffer = new ArrayBuffer[(ChannelEndpointID,ControlPayload)]()
    messageQueues.foreach{
      case (d, payloads) =>
        payloads.foreach{
          payload =>
            buffer.append((d, payload))
        }
    }
    buffer
  }

  def enqueuePayload(channelEndpointID: ChannelEndpointID, payload:ControlPayload): Unit = {
    messageQueues.getOrElseUpdate(channelEndpointID, new mutable.Queue()).enqueue(payload)
    var continue = true
    while(continue){
      val currentStep = controlProcessor.cursor.getStep
      replayOrderEnforcer.forwardReplayProcess(currentStep)
      val currentChannel = replayOrderEnforcer.currentChannel
      if(messageQueues.getOrElseUpdate(currentChannel, new mutable.Queue()).nonEmpty){
        val controlPayload = messageQueues(currentChannel).dequeue()
        println(s"reprocessing message from channel = $currentChannel content = $controlPayload at step = $currentStep")
        processPayload(currentChannel, controlPayload)
      }else{
        continue = false
        println(s"waiting message from channel = $currentChannel at step = $currentStep")
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
