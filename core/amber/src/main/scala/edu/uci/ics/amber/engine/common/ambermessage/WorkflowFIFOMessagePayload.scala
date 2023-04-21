package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

// Outside DP

sealed trait WorkflowFIFOMessagePayload extends Serializable

trait WorkflowFIFOMessagePayloadWithPiggyback extends WorkflowFIFOMessagePayload with WorkflowDPMessagePayload {
  var piggybacked:AmberInternalPayload = _
}

// system messages: fault tolerance, checks, shutdowns
sealed trait AmberInternalPayload extends WorkflowFIFOMessagePayload

trait IdempotentInternalPayload extends AmberInternalPayload
trait OneTimeInternalPayload extends AmberInternalPayload{
  val id:String
}
trait MarkerAlignmentInternalPayload extends AmberInternalPayload{
  val id:String
  val alignmentMap:Map[ActorVirtualIdentity,Set[ChannelEndpointID]]
}

trait MarkerCollectionSupport{
  def onReceiveMarker(channel:ChannelEndpointID):Unit
  def onReceivePayload(channel:ChannelEndpointID, p: WorkflowFIFOMessagePayload):Unit
  def isCompleted:Boolean
}