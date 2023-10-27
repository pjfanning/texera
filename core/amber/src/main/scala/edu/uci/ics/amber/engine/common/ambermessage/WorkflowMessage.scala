package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.util.hashing.MurmurHash3

object ChannelID {
  def apply(
      from: ActorVirtualIdentity,
      to: ActorVirtualIdentity,
      isControlChannel: Boolean
  ): ChannelID = {
    new ChannelID(from, to, isControlChannel)
  }
}

class ChannelID(
    val from: ActorVirtualIdentity,
    val to: ActorVirtualIdentity,
    val isControlChannel: Boolean
) extends Serializable {
  def canEqual(other: Any): Boolean = other.isInstanceOf[ChannelID]

  override def equals(other: Any): Boolean =
    other match {
      case that: ChannelID =>
        (that canEqual this) && from == that.from && to == that.to && isControlChannel == that.isControlChannel
      case _ => false
    }

  override def hashCode(): Int = {
    var h = MurmurHash3.seqSeed
    h = MurmurHash3.mix(h, from.##)
    h = MurmurHash3.mix(h, to.##)
    h = MurmurHash3.mix(h, isControlChannel.##)
    MurmurHash3.finalizeHash(h, 2)
  }
  override def toString: String = {
    s"Channel(${from.name},${to.name} ${if (isControlChannel) "control" else "data"})"
  }
}

sealed trait WorkflowMessage extends Serializable

case object WorkflowMessage {
  def getInMemSize(msg: WorkflowMessage): Long = {
    msg match {
      case dataMsg: WorkflowFIFOMessage =>
        dataMsg.payload match {
          case df: DataFrame => df.inMemSize
          case _             => 200L
        }
      case _ => 200L
    }
  }
}

case class WorkflowFIFOMessage(
    channel: ChannelID,
    sequenceNumber: Long,
    payload: WorkflowFIFOMessagePayload
) extends WorkflowMessage

case class WorkflowRecoveryMessage(
    from: ActorVirtualIdentity,
    payload: RecoveryPayload
)
// sent from network communicator to next worker to poll for credit information
case class CreditRequest(
    channelEndpointID: ChannelID
) extends WorkflowMessage

case class CreditResponse(
    channelEndpointID: ChannelID,
    credit: Int
) extends WorkflowMessage
