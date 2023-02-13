package edu.uci.ics.texera.workflow.common.operators.map

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo

import scala.util.{Failure, Success, Try}

abstract class MapOpDesc extends OperatorDescriptor {

  override def supportReconfiguration: Boolean = true

  override def runtimeReconfiguration(
      newOpDesc: OperatorDescriptor,
      operatorSchemaInfo: OperatorSchemaInfo
  ): Try[OpExecConfig] = {
    val newSchemas = newOpDesc.getOutputSchema(operatorSchemaInfo.inputSchemas)
    if (!newSchemas.equals(operatorSchemaInfo.outputSchemas(0))) {
      Failure(
        new UnsupportedOperationException(
          "reconfigurations that change output schema are not supported"
        )
      )
    } else {
      Success(newOpDesc.operatorExecutor(operatorSchemaInfo))
    }
  }

}
