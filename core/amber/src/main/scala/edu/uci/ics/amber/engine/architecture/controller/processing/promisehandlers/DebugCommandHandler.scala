package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.Controller
import DebugCommandHandler.DebugCommand
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers.WorkerDebugCommandHandler.WorkerDebugCommand
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object DebugCommandHandler {
  final case class DebugCommand(workerId: String, cmd: String) extends ControlCommand[Unit]
}

trait DebugCommandHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: DebugCommand, sender) =>
    {
      send(WorkerDebugCommand(msg.cmd), ActorVirtualIdentity(msg.workerId))
    }
  }
}
