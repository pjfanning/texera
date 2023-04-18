package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

object ChannelEndpointID{
  def apply(endpointWorker:ActorVirtualIdentity, isControlChannel:Boolean):ChannelEndpointID = {
    new ChannelEndpointID(endpointWorker, isControlChannel)
  }
}

class ChannelEndpointID(val endpointWorker:ActorVirtualIdentity, val isControlChannel:Boolean)

// always log.
case object OutsideWorldChannelEndpointID extends ChannelEndpointID(CLIENT, true)

// not covered by fault-tolerance layer
case object InternalChannelEndpointID extends ChannelEndpointID(ActorVirtualIdentity("Internal"), true)

sealed trait WorkflowMessage extends Serializable

case class WorkflowFIFOMessage(channel: ChannelEndpointID, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
                          actorVirtualIdentity: ActorVirtualIdentity
                        ) extends WorkflowMessage

