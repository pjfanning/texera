package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.principal.{OperatorResult, OperatorStatistics}
import edu.uci.ics.amber.engine.common.amberexception.BreakpointException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object ControllerEvent {

  case class WorkflowCompleted(
      // map from sink operator ID to the result list of tuples
      result: Map[String, OperatorResult]
  ) extends ControlCommand[Unit]

  case class WorkflowPaused() extends ControlCommand[Unit]

  case class WorkflowStatusUpdate(
      operatorStatistics: Map[String, OperatorStatistics]
  ) extends ControlCommand[Unit]

  case class WorkflowResultUpdate(operatorResults: Map[String, OperatorResult])
      extends ControlCommand[Unit]

  case class BreakpointTriggered(
      breakpoint: BreakpointException,
      operatorId: String = null
  ) extends ControlCommand[Unit]

  case class PythonPrintTriggered(
      message: String,
      operatorID: String = null
  ) extends ControlCommand[Unit]

  case class ReportCurrentProcessingTuple(
      operatorID: String,
      tuple: Array[(ITuple, ActorVirtualIdentity)]
  ) extends ControlCommand[Unit]

}
