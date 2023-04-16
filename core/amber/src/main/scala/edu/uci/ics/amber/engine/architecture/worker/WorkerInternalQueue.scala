package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DataPayload, EpochMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.lbmq.LinkedBlockingMultiQueue
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable

object WorkerInternalQueue {
  case class DPMessage(channel: ChannelEndpointID, payload:WorkflowFIFOMessagePayload)

  final val DATA_QUEUE_PRIORITY = 1
  final val CONTROL_QUEUE_PRIORITY = 0

}


abstract class WorkerInternalQueue extends Serializable {

  def enqueueSystemCommand(
      control: ControlCommand[_] with SkipReply with SkipFaultTolerance
  ): Unit = {
    enqueuePayload(DPMessage(ChannelEndpointID(SELF, true), ControlInvocation(control)))
  }

  def enableAllDataQueue(enable:Boolean):Unit

  def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit

  def enqueuePayload(message:DPMessage): Unit

  def peek(currentStep: Long): Option[DPMessage]

  def take(currentStep: Long): DPMessage

  def getDataQueueLength: Int

  def getControlQueueLength: Int

}

class WorkerInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  private val lbmq = new LinkedBlockingMultiQueue[ChannelEndpointID, DPMessage]()

  override def enqueuePayload(message:DPMessage): Unit = {
    val subQueue = lbmq.getSubQueue(message.channel)
    if(subQueue == null){
      val priority =
        if(message.channel.isControlChannel){
          CONTROL_QUEUE_PRIORITY
        }else{
          DATA_QUEUE_PRIORITY
        }
      lbmq.addSubQueue(message.channel, priority).add(message)
    }else{
      subQueue.add(message)
    }
    if(!message.channel.isControlChannel) {
      creditMonitor.decreaseCredit(message.channel.endpointWorker)
    }
  }

  override def peek(currentStep: Long): Option[DPMessage] = {
    Option(lbmq.peek())
  }

  override def take(currentStep: Long): DPMessage = {
    lbmq.take()
  }

  override def getDataQueueLength: Int = {
    var result = 0
    lbmq.priorityGroups.forEach{
      group => if(group.priority == DATA_QUEUE_PRIORITY){
        group.queues.forEach{
          q => result += q.size
        }
      }
    }
    result
  }

  override def getControlQueueLength: Int = {
    var result = 0
    lbmq.priorityGroups.forEach{
      group => if(group.priority == CONTROL_QUEUE_PRIORITY){
        group.queues.forEach{
          q => result += q.size
        }
      }
    }
    result
  }

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {
    lbmq.getSubQueue(channelEndpointID).enable(enable)
  }

  override def enableAllDataQueue(enable: Boolean): Unit = {
    lbmq.priorityGroups.forEach{
      group => if(group.priority == DATA_QUEUE_PRIORITY){
        group.queues.forEach{
          q => q.enable(enable)
        }
      }
    }
  }
}
