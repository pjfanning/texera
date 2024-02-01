package edu.uci.ics.texera.web.model.websocket.event

case class OperatorStateEvent(opId:String, state:String) extends TexeraWebSocketEvent
