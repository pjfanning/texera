package edu.uci.ics.texera.web.model.websocket.response

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent

case class ModifyLogicResponse(
    opId: String,
    isValid: Boolean,
    errorMessage: String
) extends TexeraWebSocketEvent
