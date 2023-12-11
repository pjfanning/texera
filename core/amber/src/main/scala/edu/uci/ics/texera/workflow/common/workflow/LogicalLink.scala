package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity

case class OperatorPort(
    operatorId: OperatorIdentity,
    portOrdinal: Integer = 0,
    portName: String = ""
)

case class LogicalLink(origin: OperatorPort, destination: OperatorPort)
