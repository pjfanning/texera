package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}

import scala.collection.mutable

class ControllerReplayQueue(@transient controlProcessor:ControlProcessor, @transient replayOrderEnforcer: ReplayOrderEnforcer, @transient processPayload:(ChannelEndpointID, ControlPayload) => Unit) extends Serializable{

  private val messageQueues = mutable
    .HashMap[ChannelEndpointID, mutable.Queue[ControlPayload]]()

  def getAllMessages:Map[ChannelEndpointID, Iterable[ControlPayload]] = {
    messageQueues.toMap
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
