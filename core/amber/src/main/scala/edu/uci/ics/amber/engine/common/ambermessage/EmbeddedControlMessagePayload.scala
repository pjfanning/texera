package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  EmbeddedControlMessageIdentity
}

sealed trait EmbeddedControlMessageType
case object RequireAlignment extends EmbeddedControlMessageType
case object NoAlignment extends EmbeddedControlMessageType

final case class EmbeddedControlMessagePayload(
                                       id: EmbeddedControlMessageIdentity,
                                       markerType: EmbeddedControlMessageType,
                                       scope: Set[ChannelIdentity],
                                       commandMapping: Map[ActorVirtualIdentity, ControlInvocation]
) extends WorkflowFIFOMessagePayload
