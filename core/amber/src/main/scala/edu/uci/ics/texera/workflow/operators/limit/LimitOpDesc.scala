package edu.uci.ics.texera.workflow.operators.limit

import com.fasterxml.jackson.annotation.{JsonProperty, JsonPropertyDescription}
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{
  InputPort,
  OperatorGroupConstants,
  OperatorInfo,
  OutputPort
}
import edu.uci.ics.texera.workflow.common.operators.{LogicalOp, StateTransferFunc}
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.util.OperatorDescriptorUtils.equallyPartitionGoal

import scala.util.{Success, Try}

class LimitOpDesc extends LogicalOp {

  @JsonProperty(required = true)
  @JsonSchemaTitle("Limit")
  @JsonPropertyDescription("the max number of output rows")
  var limit: Int = _

  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity,
      operatorSchemaInfo: OperatorSchemaInfo
  ): PhysicalOp = {
    val limitPerWorker = equallyPartitionGoal(limit, AmberConfig.numWorkerPerOperatorByDefault)
    PhysicalOp.oneToOnePhysicalOp(
      workflowId,
      executionId,
      operatorIdentifier,
      OpExecInitInfo((idx, _, _) => new LimitOpExec(limitPerWorker(idx)))
    )
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Limit",
      "Limit the number of output rows",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort("")),
      outputPorts = List(OutputPort()),
      supportReconfiguration = true
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = schemas(0)

  override def runtimeReconfiguration(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity,
      newLogicalOp: LogicalOp,
      operatorSchemaInfo: OperatorSchemaInfo
  ): Try[(PhysicalOp, Option[StateTransferFunc])] = {
    val newPhysicalOp = newLogicalOp.getPhysicalOp(workflowId, executionId, operatorSchemaInfo)
    val stateTransferFunc: StateTransferFunc = (oldOp, newOp) => {
      val oldLimitOp = oldOp.asInstanceOf[LimitOpExec]
      val newLimitOp = newOp.asInstanceOf[LimitOpExec]
      newLimitOp.count = oldLimitOp.count
    }
    Success(newPhysicalOp, Some(stateTransferFunc))
  }
}
