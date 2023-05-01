package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowCheckpointedEvent(checkpointed: Map[Int, Boolean]) extends TexeraWebSocketEvent
