package edu.uci.ics.texera.web.model.websocket.event

case class WorkflowCheckpointStatusEvent(id:String, status:String, elapsedMs:Long, checkpointSize:Long)
  extends TexeraWebSocketEvent
