package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, FuncDelegate, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.lbmq.LinkedBlockingMultiQueue
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util
import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter

object WorkerInternalQueue {

  final val DATA_QUEUE_PRIORITY = 1
  final val CONTROL_QUEUE_PRIORITY = 0

  def getPriority(channelEndpointID: ChannelEndpointID): Int ={
    if(channelEndpointID.isControlChannel){CONTROL_QUEUE_PRIORITY}else{DATA_QUEUE_PRIORITY}
  }

  def transferContent(from :WorkerInternalQueue, to:WorkerInternalQueue, excludedChannels:Set[ChannelEndpointID]): Unit = {
    from.getAllMessages.foreach{
      case (channel, messages) =>
        if(!excludedChannels.contains(channel)){
          messages.foreach(to.enqueuePayload)
        }
    }
  }

}

abstract class WorkerInternalQueue extends Serializable {

  def enableAllDataQueue(enable:Boolean):Unit

  def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit

  def enqueuePayload(message:DPMessage): Unit

  def peek(): Option[DPMessage]

  def take(dp:DataProcessor): DPMessage

  def getDataQueueLength: Int

  def getControlQueueLength: Int

  def getAllMessages:Map[ChannelEndpointID, Iterable[DPMessage]]

}

class WorkerInternalQueueImpl(@transient creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  protected val lbmq = new LinkedBlockingMultiQueue[ChannelEndpointID, DPMessage]()

  override def getAllMessages:Map[ChannelEndpointID, Iterable[DPMessage]] = {
    lbmq.enableAllSubQueue()
    val arr = new util.ArrayList[DPMessage]()
    lbmq.drainTo(arr)
    arr.asScala.groupBy(_.channel)
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

  override def peek(): Option[DPMessage] = {
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
}
