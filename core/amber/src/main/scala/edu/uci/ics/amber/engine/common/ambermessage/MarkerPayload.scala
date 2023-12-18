package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

sealed trait MarkerType
case object RequireAlignment extends MarkerType
case object NoAlignment extends MarkerType

final case class MarkerPayload(
    id: String,
    markerType: MarkerType,
    scope: PhysicalPlan,
    command: ControlInvocation
) extends WorkflowFIFOMessagePayload
