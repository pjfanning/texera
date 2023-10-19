package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.util.hashing.MurmurHash3

object ChannelEndpointID {
  def apply(endpointWorker: ActorVirtualIdentity, isControlChannel: Boolean): ChannelEndpointID = {
    new ChannelEndpointID(endpointWorker, isControlChannel)
  }
}

class ChannelEndpointID(val endpointWorker: ActorVirtualIdentity, val isControlChannel: Boolean)
    extends Serializable {
  def canEqual(other: Any): Boolean = other.isInstanceOf[ChannelEndpointID]

  override def equals(other: Any): Boolean =
    other match {
      case that: ChannelEndpointID =>
        (that canEqual this) && endpointWorker == that.endpointWorker && isControlChannel == that.isControlChannel
      case _ => false
    }

  override def hashCode(): Int = {
    var h = MurmurHash3.seqSeed
    h = MurmurHash3.mix(h, endpointWorker.##)
    h = MurmurHash3.mix(h, isControlChannel.##)
    MurmurHash3.finalizeHash(h, 2)
  }
  override def toString: String = {
    s"Channel(${endpointWorker.name}, ${if (isControlChannel) "control" else "data"})"
  }
}

case object OutsideWorldChannelEndpointID extends ChannelEndpointID(CLIENT, true)

sealed trait WorkflowMessage extends Serializable

case object WorkflowMessage {
  def getInMemSize(msg: WorkflowMessage): Long = {
    msg match {
      case dataMsg: WorkflowDataMessage =>
        dataMsg.payload match {
          case df: DataFrame => df.inMemSize
          case _             => 200L
        }
      case _ => 200L
    }
  }
}

case class WorkflowControlMessage(
    channel: ChannelEndpointID,
    sequenceNumber: Long,
    payload: ControlPayload
) extends WorkflowMessage

case class WorkflowDataMessage(
    channel: ChannelEndpointID,
    sequenceNumber: Long,
    payload: DataPayload
) extends WorkflowMessage

case class WorkflowRecoveryMessage(
    from: ActorVirtualIdentity,
    payload: RecoveryPayload
)
// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    from: ActorVirtualIdentity,
    sequenceNumber: Long = -1
) extends WorkflowMessage
