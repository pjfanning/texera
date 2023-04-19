package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, InternalChannelEndpointID}

import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable

class RecoveryInternalQueueImpl(creditMonitor: CreditMonitor, val replayOrderEnforcer: ReplayOrderEnforcer) extends WorkerInternalQueue {

  private val messageQueues = mutable
    .HashMap[ChannelEndpointID, LinkedBlockingQueue[DPMessage]]()
  private val systemCommandQueue = new LinkedBlockingQueue[DPMessage]()

  override def peek(currentStep: Long): Option[DPMessage] = {
    replayOrderEnforcer.forwardReplayProcess(currentStep)
    // output a dummy message
    Some(DPMessage(replayOrderEnforcer.currentChannel, null))
  }

  override def take(currentStep: Long): DPMessage = {
    if(!systemCommandQueue.isEmpty){
      systemCommandQueue.take()
    }else{
      replayOrderEnforcer.forwardReplayProcess(currentStep)
      val currentChannel = replayOrderEnforcer.currentChannel
      if(!currentChannel.isControlChannel){
        creditMonitor.increaseCredit(currentChannel.endpointWorker)
      }
      messageQueues.getOrElseUpdate(currentChannel, new LinkedBlockingQueue()).take()
    }
  }

  override def getDataQueueLength: Int = 0

  override def getControlQueueLength: Int = 0

  override def enqueuePayload(message: DPMessage): Unit = {
    if(message.channel == InternalChannelEndpointID){
      // system delegate
      systemCommandQueue.put(message)
    }
    if(!message.channel.isControlChannel){
      creditMonitor.decreaseCredit(message.channel.endpointWorker)
    }
    messageQueues.getOrElseUpdate(message.channel, new LinkedBlockingQueue()).put(message)
  }

  override def enableAllDataQueue(enable: Boolean): Unit = {}

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {}

  override def getAllMessages: Iterable[DPMessage] = {
    val result = mutable.ArrayBuffer[DPMessage]()
    systemCommandQueue.forEach(m => result.append(m))
    messageQueues.foreach{
      case (channel, messages) =>
        messages.forEach(m => result.append(m))
    }
    result
  }
}
