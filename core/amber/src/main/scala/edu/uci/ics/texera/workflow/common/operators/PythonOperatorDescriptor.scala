package edu.uci.ics.texera.workflow.common.operators

import edu.uci.ics.amber.engine.architecture.deploysemantics.{PhysicalOp, SchemaPropagationFunc}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecInitInfo, OpExecInitInfoWithCode}
import edu.uci.ics.amber.engine.common.virtualidentity.{ExecutionIdentity, WorkflowIdentity}

trait PythonOperatorDescriptor extends LogicalOp {
  override def getPhysicalOp(
      workflowId: WorkflowIdentity,
      executionId: ExecutionIdentity
  ): PhysicalOp = {


    if (asSource()) {
      PhysicalOp
        .sourcePhysicalOp(
          workflowId,
          executionId,
          operatorIdentifier,
          OpExecInitInfoWithCode((_,_ ) => {(generatePythonCode(), "python")})
        )
        .withInputPorts(operatorInfo.inputPorts)
        .withOutputPorts(operatorInfo.outputPorts)
        .withParallelizable(parallelizable())
        .withPropagateSchema(
          SchemaPropagationFunc(inputSchemas =>
            Map(
              operatorInfo.outputPorts.head.id -> getOutputSchema(
                operatorInfo.inputPorts.map(_.id).map(inputSchemas(_)).toArray
              )
            )
          )
        )
    } else {
      PhysicalOp
        .oneToOnePhysicalOp(
          workflowId,
          executionId,
          operatorIdentifier,
          OpExecInitInfoWithCode((_,_ ) => {(generatePythonCode(), "python")})
        )
        .withInputPorts(operatorInfo.inputPorts)
        .withOutputPorts(operatorInfo.outputPorts)
        .withParallelizable(parallelizable())
        .withPropagateSchema(
          SchemaPropagationFunc(inputSchemas =>
            Map(
              operatorInfo.outputPorts.head.id -> getOutputSchema(
                operatorInfo.inputPorts.map(_.id).map(inputSchemas(_)).toArray
              )
            )
          )
        )
    }
  }

  def parallelizable(): Boolean = false
  def asSource(): Boolean = false

  /**
    * This method is to be implemented to generate the actual Python source code
    * based on operators predicates.
    *
    * @return a String representation of the executable Python source code.
    */
  def generatePythonCode(): String

}
