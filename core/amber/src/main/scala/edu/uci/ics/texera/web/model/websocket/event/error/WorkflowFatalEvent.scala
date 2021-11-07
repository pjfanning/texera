package edu.uci.ics.texera.web.model.websocket.event.error

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent

case class WorkflowFatalEvent(operatorId: String, message: String, trace: String)
    extends TexeraWebSocketEvent
