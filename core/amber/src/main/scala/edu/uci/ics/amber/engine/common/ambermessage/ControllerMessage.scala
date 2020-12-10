package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.architecture.breakpoint.FaultedTuple
import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.GlobalBreakpoint
import edu.uci.ics.amber.engine.architecture.breakpoint.localbreakpoint.LocalBreakpoint
import edu.uci.ics.amber.engine.architecture.controller.ControllerState
import edu.uci.ics.amber.engine.common.ambertag.OperatorIdentifier
import akka.actor.ActorRef

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ControllerMessage {

  final case class AckedControllerInitialization() extends WorkflowMessage

  final case class ContinuedInitialization() extends WorkflowMessage

  final case class ReportState(controllerState: ControllerState.Value) extends WorkflowMessage

  final case class ReportGlobalBreakpointTriggered(
      report: mutable.HashMap[(ActorRef, FaultedTuple), ArrayBuffer[String]],
      operatorID: String = null
  ) extends WorkflowMessage

  final case class PassBreakpointTo(operatorID: String, breakpoint: GlobalBreakpoint)
      extends WorkflowMessage

}
