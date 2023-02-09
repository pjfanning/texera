package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowInteractionHistoryEvent(history: Seq[Int]) extends TexeraWebSocketEvent
