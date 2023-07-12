package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.breakpoint.localbreakpoint.LocalBreakpoint
import AssignLocalBreakpointHandler.AssignLocalBreakpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object AssignLocalBreakpointHandler {
  final case class AssignLocalBreakpoint(bp: LocalBreakpoint) extends ControlCommand[Unit]
}

trait AssignLocalBreakpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: AssignLocalBreakpoint, sender) =>
    dp.breakpointManager.registerOrReplaceBreakpoint(msg.bp)

  }
}
