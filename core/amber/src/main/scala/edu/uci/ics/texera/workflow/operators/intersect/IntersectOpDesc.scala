package edu.uci.ics.texera.workflow.operators.intersect

import com.google.common.base.Preconditions
import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}
import edu.uci.ics.amber.engine.common.workflow.{InputPort, OutputPort, PortIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

class IntersectOpDesc extends LogicalOp {

  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {
    PhysicalOp
      .hashPhysicalOp(
        workflowId,
        executionId,
        operatorIdentifier,
        OpExecInitInfo((_, _, _) => new IntersectOpExec()),
        operatorInfo.inputPorts
          .map(inputPort => inputPortToSchemaMapping(inputPort.id))
          .head
          .getAttributes
          .toArray
          .indices
          .toList
      )
      .withInputPorts(operatorInfo.inputPorts, inputPortToSchemaMapping)
      .withOutputPorts(operatorInfo.outputPorts, outputPortToSchemaMapping)
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Intersect",
      "Take the intersect of two inputs",
      OperatorGroupConstants.UTILITY_GROUP,
      inputPorts = List(InputPort(PortIdentity()), InputPort(PortIdentity(1))),
      outputPorts = List(OutputPort())
    )

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.forall(_ == schemas(0)))
    schemas(0)
  }

}
