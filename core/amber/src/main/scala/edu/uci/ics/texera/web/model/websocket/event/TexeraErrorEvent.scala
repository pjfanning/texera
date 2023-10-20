package edu.uci.ics.texera.web.model.websocket.event

import edu.uci.ics.texera.web.workflowruntimestate.JobError
import edu.uci.ics.texera.workflow.common.ConstraintViolation

import scala.collection.immutable.HashMap

case class TexeraErrorEvent(
    errors:Seq[JobError]
) extends TexeraWebSocketEvent
