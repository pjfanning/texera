package edu.uci.ics.amber.operator.distinct

import com.google.common.base.Preconditions
import edu.uci.ics.amber.core.executor.OpExecInitInfo
import edu.uci.ics.amber.core.tuple.Schema
import edu.uci.ics.amber.core.workflow.{HashPartition, PhysicalOp}
import edu.uci.ics.amber.operator.LogicalOp
import edu.uci.ics.amber.operator.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.amber.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.workflow.{InputPort, OutputPort}

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
        OpExecInitInfo((_, _) => new DistinctOpExec())
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

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.forall(_ == schemas(0)))
    schemas(0)
  }

}