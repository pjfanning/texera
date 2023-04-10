package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

sealed trait WorkflowMessage extends Serializable {
  val from: ActorVirtualIdentity
}

case class WorkflowFIFOMessage(from: ActorVirtualIdentity, isData: Boolean, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

trait WorkflowFIFOMessagePayload extends Serializable

abstract class FIFOMarker(val id: Long) extends WorkflowFIFOMessagePayload

case class EstimationMarker(override val id:Long) extends FIFOMarker(id)

case class GlobalCheckpointMarker(override val id:Long, markerCollectionCount:Map[ActorVirtualIdentity, Long]) extends FIFOMarker(id)

case class WorkflowRecoveryMessage(
    from: ActorVirtualIdentity,
    payload: RecoveryPayload
) extends WorkflowMessage

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    from: ActorVirtualIdentity
) extends WorkflowMessage
