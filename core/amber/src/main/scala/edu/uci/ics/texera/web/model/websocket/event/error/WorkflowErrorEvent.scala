package edu.uci.ics.texera.web.model.websocket.event.error

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent
import edu.uci.ics.texera.workflow.common.ConstraintViolation

import scala.collection.immutable.HashMap

case class WorkflowErrorEvent(
    operatorErrors: Map[String, Set[ConstraintViolation]] =
      new HashMap[String, Set[ConstraintViolation]](),
    generalErrors: Map[String, String] = new HashMap[String, String]()
) extends TexeraWebSocketEvent
