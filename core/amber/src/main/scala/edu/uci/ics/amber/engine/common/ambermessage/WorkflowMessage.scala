package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ChannelEndpointID(endpointWorker:ActorVirtualIdentity, isControlChannel:Boolean)

sealed trait WorkflowMessage extends Serializable

case class WorkflowFIFOMessage(channel: ChannelEndpointID, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

trait WorkflowFIFOMessagePayload extends Serializable

abstract class FIFOMarker(val id: Long) extends WorkflowFIFOMessagePayload

case class EstimationMarker(override val id:Long) extends FIFOMarker(id)

case class GlobalCheckpointMarker(override val id:Long, markerCollectionCount:Map[ActorVirtualIdentity, Long]) extends FIFOMarker(id)

case class AmberInternalMessage(
    from: ActorVirtualIdentity,
    payload: InternalPayload
) extends WorkflowMessage

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    actorVirtualIdentity: ActorVirtualIdentity
) extends WorkflowMessage
