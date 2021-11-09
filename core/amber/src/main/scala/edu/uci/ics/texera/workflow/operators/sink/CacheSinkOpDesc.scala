package edu.uci.ics.texera.workflow.operators.sink

import edu.uci.ics.amber.engine.operators.OpExecConfig
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}

class CacheSinkOpDesc extends SimpleSinkOpDesc {

  var schema: Schema = _

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo): OpExecConfig = {
    schema = operatorSchemaInfo.outputSchema
    super.operatorExecutor(operatorSchemaInfo)
  }

}
