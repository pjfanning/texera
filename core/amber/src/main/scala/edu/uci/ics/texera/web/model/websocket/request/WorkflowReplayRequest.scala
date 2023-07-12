package edu.uci.ics.texera.web.model.websocket.request

case class WorkflowReplayRequest(replayPos: Int, plannerStrategy: String, replayTimeLimit: Int)
    extends TexeraWebSocketRequest

case class WorkflowCheckpointRequest() extends TexeraWebSocketRequest
