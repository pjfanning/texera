package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.breakpoint.localbreakpoint.LocalBreakpoint
import QueryAndRemoveBreakpointsHandler.QueryAndRemoveBreakpoints
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.PAUSED
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object QueryAndRemoveBreakpointsHandler {

  final case class QueryAndRemoveBreakpoints(ids: Array[String])
      extends ControlCommand[Array[LocalBreakpoint]]
}

trait QueryAndRemoveBreakpointsHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: QueryAndRemoveBreakpoints, sender) =>
    dp.stateManager.assertState(PAUSED)
    val ret = dp.breakpointManager.getBreakpoints(msg.ids)
    dp.breakpointManager.removeBreakpoints(msg.ids)
    ret
  }

}
