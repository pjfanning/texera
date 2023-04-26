package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
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
    val entry = inputChannels.getOrElseUpdate(channelId, new AmberFIFOChannel(actorId))
    entry.acceptMessage(workflowFIFOMessage.sequenceNumber, workflowFIFOMessage.payload).foreach{
      payload =>
        handler.apply(channelId, payload)
    }
  }

  def handleFIFOPayload(channelId: ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit ={
    val entry = inputChannels.getOrElseUpdate(channelId, new AmberFIFOChannel(actorId))
    entry.enforceFIFO(payload).foreach{
      payload =>
        handler.apply(channelId, payload)
    }
  }

  def getActiveChannels: Iterable[ChannelEndpointID] = inputChannels.keys

  def getFIFOState: Map[ChannelEndpointID, Long] = inputChannels.map(x => (x._1,x._2.current)).toMap

  def setFIFOState(fifoState: Map[ChannelEndpointID, Long]): Unit = {
    inputChannels.clear()
    fifoState.foreach{
      case (id, current)  =>
        val enforcer = new AmberFIFOChannel(actorId)
        enforcer.current = current
        inputChannels(id) = enforcer
    }
  }

}
