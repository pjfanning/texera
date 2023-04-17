package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class NetworkInputPort(
    val actorId: ActorVirtualIdentity,
    val handler: (ChannelEndpointID, WorkflowFIFOMessagePayload) => Unit
) extends AmberLogging {

  private val inputChannels =
    new mutable.HashMap[ChannelEndpointID, AmberFIFOChannel]()

  def handleMessage(
      workflowFIFOMessage: WorkflowFIFOMessage
  ): Unit = {
    val channelId = workflowFIFOMessage.channel
    val entry = inputChannels.getOrElseUpdate(channelId, new AmberFIFOChannel())
    entry.acceptMessage(workflowFIFOMessage.sequenceNumber, workflowFIFOMessage.payload).foreach{
      payload =>
        handler.apply(channelId, payload)
    }
  }

  def overwriteControlFIFOSeqNum(seqMap: Map[ChannelEndpointID, Long]): Unit = {
    seqMap.foreach {
      case (identity, l) =>
        val entry = inputChannels.getOrElseUpdate(identity, new AmberFIFOChannel())
        entry.setCurrent(l)
    }
  }

  def getActiveChannels: Iterable[ChannelEndpointID] = inputChannels.keys

  def getFIFOState: Map[ChannelEndpointID, Long] = inputChannels.map(x => (x._1,x._2.current)).toMap

  def setFIFOState(fifoState: Map[ChannelEndpointID, Long]): Unit = {
    inputChannels.clear()
    fifoState.foreach{
      case (id, current)  =>
        val enforcer = new AmberFIFOChannel()
        enforcer.current = current
        inputChannels(id) = enforcer
    }
  }

  def increaseFIFOSeqNum(channelID: ChannelEndpointID): Unit = {
    inputChannels.getOrElseUpdate(channelID, new AmberFIFOChannel()).current += 1
  }

}
