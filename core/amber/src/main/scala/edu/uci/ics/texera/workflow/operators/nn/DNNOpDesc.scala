package edu.uci.ics.texera.workflow.operators.nn

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.annotations.{AutofillAttributeName, AutofillAttributeNameList}
import edu.uci.ics.texera.workflow.common.operators.mlmodel.MLModelOpDesc
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo

class DNNOpDesc extends MLModelOpDesc {
  @JsonProperty(value = "features", required = true)
  @JsonPropertyDescription("column representing feature")
  @AutofillAttributeNameList
  var xAttr: List[String] = List()

  @JsonProperty(value = "Y", required = true)
  @JsonPropertyDescription("column representing ground truth")
  @AutofillAttributeName
  var yAttr: String = _

  @JsonProperty(value = "layers", required = true)
  @JsonPropertyDescription("number of layers")
  var layers: Int = _

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo) =
    OpExecConfig.manyToOneLayer(
      operatorIdentifier,
      _ => new DNNOpExec(xAttr, yAttr, layers)
    )

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Deep Neural Network",
      "Trains a DNN model",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
}
