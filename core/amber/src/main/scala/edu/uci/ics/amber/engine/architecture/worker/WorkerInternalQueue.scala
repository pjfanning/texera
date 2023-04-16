package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DataPayload, EpochMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable

object WorkerInternalQueue {
  case class DPMessage(channel: ChannelEndpointID, payload:WorkflowFIFOMessagePayload)

  sealed trait QueueHeadStatus
  case object NO_MESSAGE extends QueueHeadStatus
  case object CONTROL_MESSAGE extends QueueHeadStatus
  case object DATA_MESSAGE extends QueueHeadStatus

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

  def getQueueHeadStatus(currentStep: Long): QueueHeadStatus

  def take(currentStep: Long): DPMessage

  def getDataQueueLength: Int

  def getControlQueueLength: Int

}

class WorkerInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  private var allDisabled = false
  private val enabledDataQueues = mutable.HashSet[ChannelEndpointID]()
  private val disabledDataQueues = mutable.HashSet[ChannelEndpointID]()
  private val dataQueues:mutable.HashMap[ChannelEndpointID, LinkedBlockingQueue[DPMessage]] = mutable.HashMap()
  private val controlQueue:LinkedBlockingQueue[DPMessage] = new LinkedBlockingQueue()
  private val inputLock = new ReentrantLock()
  private var availableFuture:CompletableFuture[Unit] = CompletableFuture.completedFuture(Unit)

  override def enqueuePayload(message:DPMessage): Unit = {
    inputLock.lock()
    if(message.channel.isControlChannel) {
      controlQueue.put(message)
    }else{
      creditMonitor.decreaseCredit(message.channel.endpointWorker)
      if(dataQueues.contains(message.channel)){
        dataQueues(message.channel).put(message)
      }else{
        if(allDisabled){
          disabledDataQueues.add(message.channel)
        }else{
          enabledDataQueues.add(message.channel)
        }
        val newQueue = new LinkedBlockingQueue[DPMessage]()
        newQueue.put(message)
        dataQueues(message.channel) = newQueue
      }
    }
    if(!controlQueue.isEmpty || enabledDataQueues.exists(id => !dataQueues(id).isEmpty)){
      availableFuture.complete(Unit)
    }
    inputLock.unlock()
  }

  override def getQueueHeadStatus(currentStep: Long): QueueHeadStatus = {
    inputLock.lock()
    val result = if(!controlQueue.isEmpty){
      CONTROL_MESSAGE
    }else if(enabledDataQueues.exists(id => !dataQueues(id).isEmpty)){
      DATA_MESSAGE
    }else{
      NO_MESSAGE
    }
    inputLock.unlock()
    result
  }

  override def take(currentStep: Long): DPMessage = {
    inputLock.lock()
    val queueEmpty = controlQueue.isEmpty && enabledDataQueues.forall(id => dataQueues(id).isEmpty)
    val controlQueueEmptyWhilePaused = controlQueue.isEmpty && enabledDataQueues.isEmpty
    if(queueEmpty || controlQueueEmptyWhilePaused){
      availableFuture = new CompletableFuture[Unit]()
    }
    inputLock.unlock()
    availableFuture.get()
    // we are sure the queue is not empty.
    if(!controlQueue.isEmpty){
      controlQueue.take()
    }else{
      val nonEmptyChannelId = enabledDataQueues.find(id => !dataQueues(id).isEmpty).get
      val msg = dataQueues(nonEmptyChannelId).take()
      creditMonitor.increaseCredit(msg.channel.endpointWorker)
      msg
    }
  }

  override def getDataQueueLength: Int = {
    inputLock.lock()
    val result = dataQueues.values.map(_.size).sum
    inputLock.unlock()
    result
  }

  override def getControlQueueLength: Int = {
    inputLock.lock()
    val result = controlQueue.size
    inputLock.unlock()
    result
  }

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {
    inputLock.lock()
    if(enable){
      disabledDataQueues.remove(channelEndpointID)
      enabledDataQueues.add(channelEndpointID)
    }else{
      enabledDataQueues.remove(channelEndpointID)
      disabledDataQueues.add(channelEndpointID)
    }
    inputLock.unlock()
  }

  override def enableAllDataQueue(enable: Boolean): Unit = {
    inputLock.lock()
    if(enable){
      allDisabled = false
      disabledDataQueues.foreach(enabledDataQueues.add)
      disabledDataQueues.clear()
    }else{
      allDisabled = true
      enabledDataQueues.foreach(disabledDataQueues.add)
      enabledDataQueues.clear()
    }
    inputLock.unlock()
  }
}
