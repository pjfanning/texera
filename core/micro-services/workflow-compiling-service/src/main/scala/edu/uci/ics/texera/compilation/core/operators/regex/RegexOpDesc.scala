package edu.uci.ics.texera.compilation.core.operators.regex

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.core.executor.OpExecInitInfo
import edu.uci.ics.amber.core.workflow.PhysicalOp
import edu.uci.ics.amber.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.workflow.{InputPort, OutputPort}
import edu.uci.ics.texera.compilation.core.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.compilation.core.common.metadata.annotations.AutofillAttributeName
import edu.uci.ics.texera.compilation.core.operators.filter.FilterOpDesc

class RegexOpDesc extends FilterOpDesc {

  @JsonProperty(value = "attribute", required = true)
  @JsonPropertyDescription("column to search regex on")
  @AutofillAttributeName
  var attribute: String = _

  @JsonProperty(value = "regex", required = true)
  @JsonPropertyDescription("regular expression")
  var regex: String = _

  @JsonProperty(required = false, defaultValue = "false")
  @JsonSchemaTitle("Case Insensitive")
  @JsonPropertyDescription("regex match is case sensitive")
  var caseInsensitive: Boolean = _

  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {
    PhysicalOp
      .oneToOnePhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecInitInfo((_, _) => new RegexOpExec(regex, caseInsensitive, attribute))
      )
      .withInputPorts(operatorInfo.inputPorts)
      .withOutputPorts(operatorInfo.outputPorts)
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      userFriendlyName = "Regular Expression",
      operatorDescription = "Search a regular expression in a string column",
      operatorGroupName = OperatorGroupConstants.SEARCH_GROUP,
      inputPorts = List(InputPort()),
      outputPorts = List(OutputPort()),
      supportReconfiguration = true
    )
}
