package edu.uci.ics.texera.web.model.websocket.event

import edu.uci.ics.amber.engine.common.amberexception.BreakpointException

case class BreakpointTriggeredEvent(
    breakpoint: BreakpointException,
    operatorId: String
) extends TexeraWebSocketEvent
