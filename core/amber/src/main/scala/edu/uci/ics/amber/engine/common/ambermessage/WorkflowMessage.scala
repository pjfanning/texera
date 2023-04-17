package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ChannelEndpointID(endpointWorker:ActorVirtualIdentity, isControlChannel:Boolean)

sealed trait WorkflowMessage extends Serializable

case class WorkflowFIFOMessage(channel: ChannelEndpointID, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

trait WorkflowFIFOMessagePayload extends Serializable

// system messages: fault tolerance, checks, shutdowns
sealed trait AmberInternalPayload extends WorkflowFIFOMessagePayload

trait IdempotentInternalPayload extends AmberInternalPayload
trait OneTimeInternalPayload extends AmberInternalPayload{
  val id:Long
}
trait MarkerAlignmentInternalPayload extends AmberInternalPayload{
  val id:Long
  val alignmentMap:Map[ActorVirtualIdentity,Set[ChannelEndpointID]]
  def onReceiveMarker(channel:ChannelEndpointID):Unit
  def isAligned:Boolean
  def onReceivePayload(channel:ChannelEndpointID, p: WorkflowFIFOMessagePayload):Unit
}

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    actorVirtualIdentity: ActorVirtualIdentity
) extends WorkflowMessage
