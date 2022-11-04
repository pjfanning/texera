package edu.uci.ics.texera.web.model.websocket.event.python

import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent
import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent

object PythonConsoleUpdateEvent {
  def apply(event: ControllerEvent.PythonPrintTriggered): PythonConsoleUpdateEvent = {
    PythonConsoleUpdateEvent(event.message, event.operatorID)
  }
}

case class PythonConsoleUpdateEvent(
    message: String,
    operatorID: String
) extends TexeraWebSocketEvent
