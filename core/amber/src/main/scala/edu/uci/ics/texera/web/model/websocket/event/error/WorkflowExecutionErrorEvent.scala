package edu.uci.ics.texera.web.model.websocket.event.error

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent

case class WorkflowExecutionErrorEvent(message: String) extends TexeraWebSocketEvent
