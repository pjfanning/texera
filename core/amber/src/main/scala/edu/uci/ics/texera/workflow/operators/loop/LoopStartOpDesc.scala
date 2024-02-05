package edu.uci.ics.texera.workflow.operators.loop

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema}

class LoopStartOpDesc extends LogicalOp {

  @JsonProperty(required = true)
  @JsonSchemaTitle("Iteration")
  @JsonPropertyDescription("the max number of iterations")
  var termination: Int = _

  @JsonProperty(defaultValue = "false")
  @JsonSchemaTitle("Append Iteration Number")
  var append: Boolean = false

  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {
    val outputSchema = outputPortToSchemaMapping(operatorInfo.outputPorts.head.id)
    PhysicalOp
      .oneToOnePhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecInitInfo((_, _, operatorConfig) => {
          new LoopStartOpExec(outputSchema, operatorConfig.workerConfigs.head.workerId, termination)
        })
      )
      .withInputPorts(operatorInfo.inputPorts, inputPortToSchemaMapping)
      .withOutputPorts(operatorInfo.outputPorts, outputPortToSchemaMapping)
      .withSuggestedWorkerNum(1)
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "LoopStart",
      "Limit the number of output rows",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort()),
      supportReconfiguration = true
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    if (append) {
      Schema.newBuilder()
        .add("Iteration", AttributeType.INTEGER)
        .add(schemas(0))
        .build()
    } else {
      schemas(0)
    }
  }

}
