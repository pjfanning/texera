package edu.uci.ics.texera.compilation.core.operators.filter

import com.google.common.base.Preconditions
import edu.uci.ics.amber.core.tuple.Schema
import edu.uci.ics.amber.core.workflow.PhysicalOp
import edu.uci.ics.amber.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.texera.compilation.core.operators.{LogicalOp, StateTransferFunc}

import scala.util.{Success, Try}

abstract class FilterOpDesc extends LogicalOp {

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    schemas(0)
  }

  override def runtimeReconfiguration(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity,
      oldOpDesc: LogicalOp,
      newOpDesc: LogicalOp
  ): Try[(PhysicalOp, Option[StateTransferFunc])] = {
    Success(newOpDesc.getPhysicalOp(workflowId, executionId), None)
  }

}
