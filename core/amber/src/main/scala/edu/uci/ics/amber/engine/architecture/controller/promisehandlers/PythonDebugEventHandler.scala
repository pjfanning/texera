package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.PythonDebugEventTriggered
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PythonDebugEventHandler.PythonDebugEvent
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object PythonDebugEventHandler {

  final case class PythonDebugEvent(message: String) extends ControlCommand[Unit]
}

trait PythonDebugEventHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: PythonDebugEvent, sender) =>
    {
      // report the print message to the frontend
      sendToClient(
        PythonDebugEventTriggered(
          msg.message,
          workflow.getOperator(sender).id.operator,
          sender.toString
        )
      )
    }
  }
}
