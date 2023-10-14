package edu.uci.ics.texera.workflow.operators.partsofspeech
import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{
  InputPort,
  OperatorGroupConstants,
  OperatorInfo,
  OutputPort
}
import edu.uci.ics.texera.workflow.common.operators.map.MapOpDesc
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, OperatorSchemaInfo, Schema}

@JsonSchemaInject(json = """
{
  "attributeTypeRules": {
    "attribute": {
      "enum": ["string"]
    }
  }
}
""")
class PartsOfSpeechOpDesc extends MapOpDesc {
  @JsonProperty(value = "attribute", required = true)
  @JsonPropertyDescription("column to perform Parts of Speech analysis on")
  @AutofillAttributeName
  var attribute: String = _

  @JsonProperty(
    value = "result attribute",
    required = true,
    defaultValue = "Parts of Speech"
  )
  @JsonPropertyDescription("column name of the Parts of Speech result")
  var resultAttribute: String = _

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo) = {
    if (attribute == null)
      throw new RuntimeException("partsofspeech analysis: attribute is null")
    OpExecConfig.oneToOneLayer(
      operatorIdentifier,
      _ => new PartsOfSpeechOpExec(this, operatorSchemaInfo)
    )
  }

  override def operatorInfo =
    new OperatorInfo(
      "Parts of Speech",
      "analysis the Parts of Speech of a text using machine learning",
      OperatorGroupConstants.ANALYTICS_GROUP,
      List(InputPort("")),
      List(OutputPort("")),
      supportReconfiguration = true
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    if (resultAttribute == null || resultAttribute.trim.isEmpty)
      return null
    Schema.newBuilder.add(schemas(0)).add(resultAttribute, AttributeType.STRING).build
  }

}
