package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

case class ChannelEndpointID(endpointWorker:ActorVirtualIdentity, isControlChannel:Boolean)

sealed trait WorkflowMessage extends Serializable

case class WorkflowFIFOMessage(channel: ChannelEndpointID, sequenceNumber: Long, payload:WorkflowFIFOMessagePayload) extends WorkflowMessage

sealed trait WorkflowFIFOMessagePayload extends Serializable

trait WorkflowFIFOMessagePayloadWithPiggyback extends WorkflowFIFOMessagePayload{
  var piggybacked:AmberInternalPayload = _
}

// system messages: fault tolerance, checks, shutdowns
sealed trait AmberInternalPayload extends WorkflowFIFOMessagePayload{
  def toPayloadWithState(actor:WorkflowActor): AmberInternalPayloadWithState
}

trait IdempotentInternalPayload extends AmberInternalPayload
trait OneTimeInternalPayload extends AmberInternalPayload{
  val id:Long
}
trait MarkerAlignmentInternalPayload extends AmberInternalPayload{
  val id:Long
  val alignmentMap:Map[ActorVirtualIdentity,Set[ChannelEndpointID]]
  def toPayloadWithState(actor:WorkflowActor):AmberInternalPayloadWithState with MarkerCollectionSupport
}

trait AmberInternalPayloadWithState extends WorkflowFIFOMessagePayload{
  val syncFuture = new CompletableFuture[Unit]()
}

trait MarkerCollectionSupport{
  def onReceiveMarker(channel:ChannelEndpointID):Unit
  def onReceivePayload(channel:ChannelEndpointID, p: WorkflowFIFOMessagePayload):Unit
}

// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    actorVirtualIdentity: ActorVirtualIdentity
) extends WorkflowMessage
