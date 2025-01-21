package edu.uci.ics.amber.operator.distinct

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.core.executor.OpExecWithClassName
import edu.uci.ics.amber.core.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.core.workflow.{GoToSpecificNode, HashPartition, InputPort, OutputPort, PhysicalOp}
import edu.uci.ics.amber.operator.LogicalOp
import edu.uci.ics.amber.operator.metadata.annotations.UIWidget
import edu.uci.ics.amber.operator.metadata.{OperatorGroupConstants, OperatorInfo}

class DistinctOpDesc extends LogicalOp {

  @JsonProperty(required = false)
  @JsonSchemaTitle("nodeAddr")
  @JsonSchemaInject(json = UIWidget.UIWidgetTextArea)
  var nodeAddr: String = _

  @JsonProperty(defaultValue = "true")
  @JsonSchemaTitle("location preference(default)")
  @JsonPropertyDescription("Whether use default RoundRobinPreference")
  var UseRoundRobin: Boolean = true

  override def getPhysicalOp(
                              workflowId: WorkflowIdentity,
                              executionId: ExecutionIdentity
                            ): PhysicalOp = {
    val baseOp = PhysicalOp
      .oneToOnePhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecWithClassName("edu.uci.ics.amber.operator.distinct.DistinctOpExec")
      )
      .withInputPorts(operatorInfo.inputPorts)
      .withOutputPorts(operatorInfo.outputPorts)
      .withPartitionRequirement(List(Option(HashPartition())))
      .withDerivePartition(_ => HashPartition())
    if (!UseRoundRobin) {
      baseOp.withLocationPreference(Some(GoToSpecificNode(nodeAddr))) // Use the actual `nodeAddr`
    } else {
      baseOp // Return without modifying location preference
    }
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Distinct",
      "Remove duplicate tuples",
      OperatorGroupConstants.CLEANING_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort(blocking = true))
    )

}
