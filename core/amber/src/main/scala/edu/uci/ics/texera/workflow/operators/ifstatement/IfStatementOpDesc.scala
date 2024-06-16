package edu.uci.ics.texera.workflow.operators.ifstatement

import com.fasterxml.jackson.annotation.JsonProperty
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
class IfStatementOpDesc extends LogicalOp {
  @JsonProperty(required = true)
  @JsonSchemaTitle("Expression")
  var exp: String = _

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "If Statement",
      "If Statement",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort(PortIdentity(0),"Condition"), InputPort(PortIdentity(1),"Data")),
      outputPorts = List(OutputPort(PortIdentity(0),"True"), OutputPort(PortIdentity(1),"False"))
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = ???
}
