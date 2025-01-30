package edu.uci.ics.amber.operator.split

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.google.common.base.Preconditions
import com.kjetland.jackson.jsonSchema.annotations.{
  JsonSchemaInject,
  JsonSchemaString,
  JsonSchemaTitle
}
import edu.uci.ics.amber.core.executor.OpExecWithClassName
import edu.uci.ics.amber.core.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.core.workflow._
import edu.uci.ics.amber.operator.LogicalOp
import edu.uci.ics.amber.operator.metadata.annotations.HideAnnotation
import edu.uci.ics.amber.operator.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.amber.util.JSONUtils.objectMapper

class SplitOpDesc extends LogicalOp {

  @JsonSchemaTitle("Split Percentage")
  @JsonProperty(defaultValue = "80")
  @JsonPropertyDescription("percentage of data going to the upper port")
  var k: Int = 80

  @JsonSchemaTitle("Auto-Generate Seed")
  @JsonPropertyDescription("Shuffle the data based on a random seed")
  @JsonProperty(defaultValue = "true")
  var random: Boolean = true

  @JsonSchemaTitle("Seed")
  @JsonProperty(defaultValue = "1")
  @JsonPropertyDescription("An int for reproducible output across multiple run")
  @JsonSchemaInject(
    strings = Array(
      new JsonSchemaString(path = HideAnnotation.hideTarget, value = "random"),
      new JsonSchemaString(path = HideAnnotation.hideType, value = HideAnnotation.Type.equals),
      new JsonSchemaString(path = HideAnnotation.hideExpectedValue, value = "true")
    )
  )
  var seed: Int = 1

  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {
    PhysicalOp
      .oneToOnePhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecWithClassName(
          "edu.uci.ics.amber.operator.split.SplitOpExec",
          objectMapper.writeValueAsString(this)
        )
      )
      .withInputPorts(operatorInfo.inputPorts)
      .withOutputPorts(operatorInfo.outputPorts)
      .withParallelizable(false)
      .withPropagateSchema(
        SchemaPropagationFunc(inputSchemas => {
          Preconditions.checkArgument(inputSchemas.size == 1)
          val outputSchema = inputSchemas.values.head
          operatorInfo.outputPorts.map(port => port.id -> outputSchema).toMap
        })
      )
  }

  override def operatorInfo: OperatorInfo = {
    OperatorInfo(
      userFriendlyName = "Split",
      operatorDescription = "Split data to two different ports",
      operatorGroupName = OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(
        OutputPort(PortIdentity()),
        OutputPort(PortIdentity(1))
      ),
      dynamicInputPorts = true,
      dynamicOutputPorts = true
    )
  }

}
