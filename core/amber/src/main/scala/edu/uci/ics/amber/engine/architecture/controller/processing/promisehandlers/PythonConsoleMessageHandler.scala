package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.google.protobuf.timestamp.Timestamp
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.ConsoleMessageTriggered
import PythonConsoleMessageHandler.PythonConsoleMessage
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.web.workflowruntimestate.ConsoleMessage

object PythonConsoleMessageHandler {

  final case class PythonConsoleMessage(
      timestamp: Timestamp,
      msgType: String,
      source: String,
      message: String
  ) extends ControlCommand[Unit]

}

trait PythonConsoleMessageHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: PythonConsoleMessage, sender) =>
    {
      // report the print message to the frontend
      sendToClient(
        ConsoleMessageTriggered(
          cp.workflow.getOperator(sender).id.operator,
          ConsoleMessage(sender.name, msg.timestamp, msg.msgType, msg.source, msg.message)
        )
      )
    }
  }
}
