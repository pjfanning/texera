package edu.uci.ics.texera.workflow.common.workflow

case class OperatorPort(operatorID: String, portOrdinal: Integer = 0, portName: String = "")

case class LogicalLink(origin: OperatorPort, destination: OperatorPort)
