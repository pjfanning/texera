package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowInteractionHistoryEvent(history: Seq[Int], isInteraction:Seq[Boolean], checkpointStatus:Seq[String]) extends TexeraWebSocketEvent
