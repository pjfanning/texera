package edu.uci.ics.texera.workflow.operators.projection

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.texera.workflow.common.metadata._
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeNameReorderList
import edu.uci.ics.texera.workflow.common.operators.OneToOneOpExecConfig
import edu.uci.ics.texera.workflow.common.operators.map.MapOpDesc
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}

import scala.jdk.CollectionConverters.asJavaIterableConverter

class ProjectionOpDescV2 extends MapOpDesc {

  @JsonProperty(required = true)
  @JsonSchemaTitle("Attributes")
  @JsonPropertyDescription("A subset of columns to keep")
  @AutofillAttributeNameReorderList
  var attributes: List[String] = List()

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo): OneToOneOpExecConfig = {
    new OneToOneOpExecConfig(operatorIdentifier, _ => new ProjectionOpExecV2(attributes))
  }

  override def operatorInfo: OperatorInfo = {
    OperatorInfo(
      "Projection V2",
      "Keeps the column",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort())
    )
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    Preconditions.checkArgument(attributes.nonEmpty)
    Schema.newBuilder
      .add(attributes.map(attribute => schemas(0).getAttribute(attribute)).asJava)
      .build()
  }
}