package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowCheckpointedEvent(checkpointed:Seq[Int]) extends TexeraWebSocketEvent
