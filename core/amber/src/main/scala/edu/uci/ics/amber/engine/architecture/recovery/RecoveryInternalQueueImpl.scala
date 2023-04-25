package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueueImpl
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.CONTROL_QUEUE_PRIORITY
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DPMessage, InternalChannelEndpointID}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


class RecoveryInternalQueueImpl(val actorId:ActorVirtualIdentity, @transient creditMonitor: CreditMonitor, @transient val replayOrderEnforcer: ReplayOrderEnforcer) extends WorkerInternalQueueImpl(creditMonitor) with AmberLogging {

  lbmq.addSubQueue(InternalChannelEndpointID, CONTROL_QUEUE_PRIORITY)
  private val systemCmdQueue = lbmq.getSubQueue(InternalChannelEndpointID)

  override def peek(): Option[DPMessage] = {
    // output a dummy message
    if(!systemCmdQueue.isEmpty){
      Some(DPMessage(InternalChannelEndpointID, null))
    }else{
      Some(DPMessage(replayOrderEnforcer.currentChannel, null))
    }
  }

  override def take(): DPMessage = {
    val currentChannel = replayOrderEnforcer.currentChannel
    if(!currentChannel.isControlChannel){
      creditMonitor.increaseCredit(currentChannel.endpointWorker)
    }
    logger.info(s"taking message from channel = $currentChannel")
    lbmq.disableSubQueueExcept(InternalChannelEndpointID, currentChannel)
    lbmq.take()
  }

  override def enableAllDataQueue(enable: Boolean): Unit = {}

  override def enableDataQueue(channelEndpointID: ChannelEndpointID, enable: Boolean): Unit = {}

}
