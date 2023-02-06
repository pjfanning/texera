package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.google.protobuf.timestamp.Timestamp
import edu.uci.ics.amber.engine.architecture.controller.{
  Controller,
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.ConsoleMessageTriggered
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PythonConsoleMessageHandler.PythonConsoleMessage
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
  this: ControllerProcessor =>
  registerHandler { (msg: PythonConsoleMessage, sender) =>
    {
      // report the print message to the frontend
      sendToClient(
        ConsoleMessageTriggered(
          workflow.getOperator(sender).id.operator,
          ConsoleMessage(sender.name, msg.timestamp, msg.msgType, msg.source, msg.message)
        )
      )
    }
  }
}
