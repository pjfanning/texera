package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, EmitMarkers, FIFOMarkerControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ChannelEndpointID(endpointWorker:ActorVirtualIdentity, isControlChannel:Boolean)

sealed trait WorkflowMessage extends Serializable

case class WorkflowFIFOMessage(channel: ChannelEndpointID, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

trait WorkflowFIFOMessagePayload extends Serializable

case class FIFOMarker(id:Long, markerCounts:Map[ActorVirtualIdentity, Long], controlIdMap:Map[ActorVirtualIdentity, Long], sharedCommand:FIFOMarkerControlCommand[_]) extends WorkflowFIFOMessagePayload

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    actorVirtualIdentity: ActorVirtualIdentity
) extends WorkflowMessage
