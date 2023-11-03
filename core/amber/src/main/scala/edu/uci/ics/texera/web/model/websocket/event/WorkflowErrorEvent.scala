package edu.uci.ics.texera.web.model.websocket.event

import edu.uci.ics.texera.web.workflowruntimestate.JobError

case class WorkflowErrorEvent(
    errors: Seq[JobError]
) extends TexeraWebSocketEvent
