package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.logging._
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{CONTROL_MESSAGE, DATA_MESSAGE, DPMessage, QueueHeadStatus}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, DataPayload, FIFOMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import scala.collection.mutable

class RecoveryInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  @transient
  private var records: Iterator[StepsOnChannel] = _
  @transient
  private var onRecoveryComplete: () => Unit = _
  @transient
  private var onReplayComplete: () => Unit = _
  @transient
  private var switchStep = 0L

  private val messageQueues = mutable
    .HashMap[ChannelEndpointID, LinkedBlockingQueue[DPMessage]]()
  private val systemCommandQueue = new LinkedBlockingQueue[DPMessage]()
  private var currentChannel: ChannelEndpointID = _
  private var nextChannel: ChannelEndpointID = _
  private var replayTo = -1L

  def setReplayTo(dest: Long, onReplayEnd: () => Unit): Unit = {
    replayTo = dest
    onReplayComplete = onReplayEnd
  }

  def initialize(
                  records: Iterator[StepsOnChannel],
                  currentDPStep: Long,
                  onRecoveryComplete: () => Unit
  ): Unit = {
    this.onRecoveryComplete = onRecoveryComplete
    // restore replay progress by dropping some of the entries
    switchStep = 0L
    this.records = records
    while (records.hasNext && switchStep < currentDPStep) {
      loadNextDeterminant()
    }
    if(!records.hasNext){
      //recovery already completed
      onRecoveryComplete()
    }
  }


  private def loadNextDeterminant(): Unit = {
    val cc = records.next()
    nextChannel = cc.channel
    switchStep = cc.steps
  }

  override def enqueueSystemCommand(
      control: AsyncRPCServer.ControlCommand[_]
        with AsyncRPCServer.SkipReply
        with AsyncRPCServer.SkipFaultTolerance
  ): Unit = {
    systemCommandQueue.put(DPMessage(ChannelEndpointID(SELF, true), ControlInvocation(control)))
  }


  override def getQueueHeadStatus(currentStep: Long): QueueHeadStatus = {
    forwardRecoveryProgress(currentStep)
    if(currentChannel.isControlChannel){
      CONTROL_MESSAGE
    }else{
      DATA_MESSAGE
    }
  }

  private def forwardRecoveryProgress(currentStep: Long): Unit = {
    while(currentStep == switchStep){
      currentChannel = nextChannel
      if(records.hasNext){
        loadNextDeterminant()
      }else{
        // recovery completed
        onRecoveryComplete()
      }
    }
  }

  override def take(currentStep: Long): DPMessage = {
    if(!systemCommandQueue.isEmpty){
      systemCommandQueue.take()
    }else{
      forwardRecoveryProgress(currentStep)
      if(currentStep == replayTo){
        // replay completed
        onReplayComplete()
        systemCommandQueue.take()
      }else{
        if(!currentChannel.isControlChannel){
          creditMonitor.increaseCredit(currentChannel.endpointWorker)
        }
        messageQueues.getOrElseUpdate(currentChannel, new LinkedBlockingQueue()).take()
      }
    }
  }

  override def getDataQueueLength: Int = 0

  override def getControlQueueLength: Int = 0

  override def enqueuePayload(message: DPMessage): Unit = {
    if(!message.channel.isControlChannel){
      creditMonitor.decreaseCredit(message.channel.endpointWorker)
    }
    messageQueues.getOrElseUpdate(message.channel, new LinkedBlockingQueue()).put(message)
  }

  override def enableAllDataQueue(enable: Boolean): Unit = {}

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {}
}
