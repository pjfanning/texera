package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

sealed trait WorkflowMessage extends Serializable {
  val from: ActorVirtualIdentity
}

case class WorkflowFIFOMessage(from: ActorVirtualIdentity, isData: Boolean, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

trait WorkflowFIFOMessagePayload extends Serializable

case class SnapshotMarker(id: Long, estimation:Boolean) extends WorkflowFIFOMessagePayload

case class WorkflowRecoveryMessage(
    from: ActorVirtualIdentity,
    payload: RecoveryPayload
) extends WorkflowMessage

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    from: ActorVirtualIdentity
) extends WorkflowMessage
