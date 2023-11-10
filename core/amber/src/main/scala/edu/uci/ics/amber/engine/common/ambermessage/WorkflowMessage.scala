package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

case class ChannelID(
    from: ActorVirtualIdentity,
    to: ActorVirtualIdentity,
    isControlChannel: Boolean
) {
  override def toString: String = {
    s"Channel(${from.name},${to.name},${if (isControlChannel) "control" else "data"})"
  }
}

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

sealed trait WorkflowMessage extends Serializable

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
