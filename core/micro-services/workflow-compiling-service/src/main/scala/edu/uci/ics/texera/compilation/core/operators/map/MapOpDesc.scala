package edu.uci.ics.texera.compilation.core.operators.map

import edu.uci.ics.amber.core.workflow.PhysicalOp
import edu.uci.ics.amber.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.texera.compilation.core.operators.{LogicalOp, StateTransferFunc}

import scala.util.{Failure, Success, Try}

abstract class MapOpDesc extends LogicalOp {

  override def runtimeReconfiguration(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity,
      oldOpDesc: LogicalOp,
      newOpDesc: LogicalOp
  ): Try[(PhysicalOp, Option[StateTransferFunc])] = {
    val inputSchemas = oldOpDesc.operatorInfo.inputPorts
      .map(inputPort => oldOpDesc.inputPortToSchemaMapping(inputPort.id))
      .toArray
    val outputSchemas = oldOpDesc.operatorInfo.outputPorts
      .map(outputPort => oldOpDesc.outputPortToSchemaMapping(outputPort.id))
      .toArray
    val newOutputSchema = newOpDesc.getOutputSchema(inputSchemas)
    if (!newOutputSchema.equals(outputSchemas.head)) {
      Failure(
        new UnsupportedOperationException(
          "reconfigurations that change output schema are not supported"
        )
      )
    } else {
      Success(newOpDesc.getPhysicalOp(workflowId, executionId), None)
    }
  }

}
