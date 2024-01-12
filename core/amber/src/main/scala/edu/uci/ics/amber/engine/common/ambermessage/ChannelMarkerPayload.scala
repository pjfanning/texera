package edu.uci.ics.amber.engine.common.ambermessage

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

sealed trait ChannelMarkerType
case object RequireAlignment extends ChannelMarkerType
case object NoAlignment extends ChannelMarkerType

final case class ChannelMarkerPayload(
    id: String,
    markerType: ChannelMarkerType,
    scope: Set[ChannelID],
    commandMapping: Map[ActorVirtualIdentity, ControlInvocation]
) extends WorkflowFIFOMessagePayload



// for checkpoint use only
final case class DelayedCallPayload(closure:() => Unit) extends WorkflowFIFOMessagePayload