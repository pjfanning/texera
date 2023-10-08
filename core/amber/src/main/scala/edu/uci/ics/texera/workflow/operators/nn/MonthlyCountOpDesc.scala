package edu.uci.ics.texera.workflow.operators.nn

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.operators.mlmodel.MLModelOpDesc
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, OperatorSchemaInfo, Schema}

class MonthlyCountOpDesc extends OperatorDescriptor {

  private val outSchema = new Schema(new Attribute("count",AttributeType.INTEGER))

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo) =
    OpExecConfig.manyToOneLayer(
      operatorIdentifier,
      _ => new MonthlyCountOpExec(outSchema)
    )

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Monthly Tweet Count",
      "bluh bluh bluh",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    outSchema
  }
}
