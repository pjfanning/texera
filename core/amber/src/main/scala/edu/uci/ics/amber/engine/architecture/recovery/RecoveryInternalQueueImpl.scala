package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueueImpl
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{ThreadSyncChannelID, getPriority}
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessor
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


class RecoveryInternalQueueImpl(val actorId:ActorVirtualIdentity, @transient creditMonitor: CreditMonitor, @transient val replayOrderEnforcer: ReplayOrderEnforcer) extends WorkerInternalQueueImpl(creditMonitor) with AmberLogging {

  replayOrderEnforcer.getAllReplayChannels.foreach{
    channel => addSubQueue(channel)
  }
  private val systemCmdQueue = lbmq.getSubQueue(ThreadSyncChannelID)

  override def peek(dp:DataProcessor): Option[DPMessage] = {
    // output a dummy message
    if(replayOrderEnforcer.isReplayCompleted(dp.cursor.getStep)){
      Some(DPMessage(ThreadSyncChannelID, null))
    }else {
      if (!systemCmdQueue.isEmpty) {
        Some(DPMessage(ThreadSyncChannelID, null))
      } else {
        Some(DPMessage(replayOrderEnforcer.currentChannel, null))
      }
    }
  }

  override def take(dp:DataProcessor): DPMessage = {
    if(replayOrderEnforcer.isReplayCompleted(dp.cursor.getStep)){
      lbmq.disableSubQueueExcept(ThreadSyncChannelID)
      lbmq.take()
    }else{
      if(!systemCmdQueue.isEmpty){
        lbmq.take()
      }else{
        val currentChannel = replayOrderEnforcer.currentChannel
        if(!currentChannel.isControlChannel){
          creditMonitor.increaseCredit(currentChannel.endpointWorker)
        }
        if(replayOrderEnforcer.isPayloadRecorded){
          DPMessage(currentChannel, replayOrderEnforcer.getRecordedPayload)
        }else{
          lbmq.disableSubQueueExcept(ThreadSyncChannelID, currentChannel)
          logger.info(s"message to take from = $currentChannel at step = ${dp.cursor.getStep}")
          val res = lbmq.take()
          logger.info(s"message to process = $res")
          res
        }
      }
    }
  }


  override def enableAllDataQueue(enable: Boolean): Unit = {}

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {}

}
