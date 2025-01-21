package edu.uci.ics.amber.operator.filter

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.{JsonSchemaInject, JsonSchemaTitle}
import edu.uci.ics.amber.core.executor.OpExecWithClassName
import edu.uci.ics.amber.core.workflow.{GoToSpecificNode, InputPort, OutputPort, PhysicalOp}
import edu.uci.ics.amber.operator.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.amber.util.JSONUtils.objectMapper
import edu.uci.ics.amber.core.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.operator.metadata.annotations.UIWidget

class SpecializedFilterOpDesc extends FilterOpDesc {

  @JsonProperty(value = "predicates", required = true)
  @JsonPropertyDescription("multiple predicates in OR")
  var predicates: List[FilterPredicate] = List.empty

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
        OpExecWithClassName(
          "edu.uci.ics.amber.operator.filter.SpecializedFilterOpExec",
          objectMapper.writeValueAsString(this)
        )
      )
      .withInputPorts(operatorInfo.inputPorts)
      .withOutputPorts(operatorInfo.outputPorts)

    if (!UseRoundRobin) {
      baseOp.withLocationPreference(Some(GoToSpecificNode(nodeAddr))) // Use the actual `nodeAddr`
    } else {
      baseOp // Return without modifying location preference
    }
  }

  override def operatorInfo: OperatorInfo = {
    OperatorInfo(
      "Filter",
      "Performs a filter operation",
      OperatorGroupConstants.CLEANING_GROUP,
      List(InputPort()),
      List(OutputPort()),
      supportReconfiguration = true
    )
  }
}
