package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowReplayCompletedEvent(replayTime:Double, checkpointTime:Double) extends TexeraWebSocketEvent
