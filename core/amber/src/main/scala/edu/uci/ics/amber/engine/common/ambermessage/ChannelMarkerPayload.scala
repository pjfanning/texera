package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

sealed trait ChannelMarkerType
case object RequireAlignment extends ChannelMarkerType
case object NoAlignment extends ChannelMarkerType

final case class ChannelMarkerPayload(
    id: String,
    markerType: ChannelMarkerType,
    scope: PhysicalPlan,
    commandMapping: Map[ActorVirtualIdentity, ControlInvocation]
) extends WorkflowFIFOMessagePayload
