package edu.uci.ics.texera.web.model.websocket.response

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent

case class RegisterWorkflowIdResponse(message: String, supportFaultTolerance: Boolean)
    extends TexeraWebSocketEvent

case class ClusterStatusUpdateEvent(numWorkers: Int) extends TexeraWebSocketEvent
