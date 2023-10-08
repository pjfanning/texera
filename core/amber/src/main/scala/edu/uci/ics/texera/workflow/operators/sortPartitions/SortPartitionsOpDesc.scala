package edu.uci.ics.texera.workflow.operators.sortPartitions

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.workflow.common.metadata.{
  InputPort,
  OperatorGroupConstants,
  OperatorInfo,
  OutputPort
}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.common.workflow.RangePartition

class SortPartitionsOpDesc extends OperatorDescriptor {

  @JsonProperty(required = true)
  @JsonSchemaTitle("Attribute")
  @JsonPropertyDescription("Attribute to sort (must be numerical).")
  @AutofillAttributeName
  var sortAttributeName: String = _

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo) = {
    val partitionRequirement = List(
      Option(
        RangePartition(
          List(operatorSchemaInfo.inputSchemas(0).getIndex(sortAttributeName))
        )
      )
    )

    OpExecConfig
      .manyToOneLayer(
        operatorIdentifier,
        p =>
          new SortPartitionOpExec(
            sortAttributeName
          )
      )
      .copy(
        partitionRequirement = partitionRequirement
      )
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Sort Partitions",
      "Sort Partitions",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort("")),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    schemas(0)
  }
}
