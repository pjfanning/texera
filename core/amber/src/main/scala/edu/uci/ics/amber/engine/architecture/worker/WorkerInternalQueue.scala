package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.lbmq.LinkedBlockingMultiQueue
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

object WorkerInternalQueue {

  final val DATA_QUEUE_PRIORITY = 2
  final val CONTROL_QUEUE_PRIORITY = 1
  final val THREAD_SYNC_PRIORITY = 0
  // not covered by fault-tolerance layer
  case object ThreadSyncChannelID extends ChannelEndpointID(ActorVirtualIdentity("DP-Main Sync"), true)

  def getPriority(channelEndpointID: ChannelEndpointID): Int ={
    if(channelEndpointID.isControlChannel){CONTROL_QUEUE_PRIORITY}else{DATA_QUEUE_PRIORITY}
  }

  def transferContent(from :WorkerInternalQueue, to:WorkerInternalQueue): Unit = {
    from.getAllMessages.foreach{
      case (channel, messages) =>
          messages.foreach(to.enqueuePayload)
    }
  }

}

abstract class WorkerInternalQueue extends Serializable {

  def enableAllDataQueue(enable:Boolean):Unit

  def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit

  def enqueuePayload(message:DPMessage): Unit

  def peek(dp:DataProcessor): Option[DPMessage]

  def take(dp:DataProcessor): DPMessage

  def getDataQueueLength: Int

  def getControlQueueLength: Int

  def getQueuedMessageCount(channelEndpointID: ChannelEndpointID): Int

  def getAllMessages:Map[ChannelEndpointID, Iterable[DPMessage]]

}

class WorkerInternalQueueImpl(@transient creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  protected val lbmq = new LinkedBlockingMultiQueue[ChannelEndpointID, DPMessage]()

  lbmq.addSubQueue(ThreadSyncChannelID, THREAD_SYNC_PRIORITY)

  override def getAllMessages:Map[ChannelEndpointID, Iterable[DPMessage]] = {
    val result = mutable.HashMap[ChannelEndpointID, mutable.Queue[DPMessage]]()
    lbmq.priorityGroups.forEach{
      g =>
        g.queues.forEach{
          q =>
            val newQ = new mutable.Queue[DPMessage]()
            q.iterator.forEachRemaining(dp => newQ.enqueue(dp))
            result(q.key) = newQ
        }
    }
    result.toMap
  }

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

  override def peek(dp:DataProcessor): Option[DPMessage] = {
    Option(lbmq.peek())
  }

  override def take(dp:DataProcessor): DPMessage = {
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

  override def getQueuedMessageCount(channelEndpointID: ChannelEndpointID): Int = {
    val subQueue = lbmq.getSubQueue(channelEndpointID)
    if(subQueue == null){
      0
    }else{
      subQueue.size
    }
  }
}
