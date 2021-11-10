package edu.uci.ics.texera.workflow.operators.sink.managed

import java.util.Collections.singletonList

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.base.Preconditions
import edu.uci.ics.amber.engine.operators.OpExecConfig
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OperatorGroupConstants, OperatorInfo}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.sink.SinkOpDesc

import scala.collection.JavaConverters.asScalaBuffer
import scala.collection.immutable.List

class AppendOnlyTableSinkOpDesc extends SinkOpDesc {

  @JsonIgnore
  var schema: Schema = _

  // op result storage
  @JsonIgnore
  private var storage:OpResultStorage = null

  override def operatorExecutor(operatorSchemaInfo: OperatorSchemaInfo): OpExecConfig = {
    schema = operatorSchemaInfo.outputSchema
    new AppendOnlyTableSinkOpExecConfig(operatorIdentifier, operatorSchemaInfo, storage.create(operatorID, operatorSchemaInfo.outputSchema))
  }

  override def operatorInfo: OperatorInfo = {
    return new OperatorInfo(
      "Table Results",
      "View the edu.uci.ics.texera.workflow results as an append-only table",
      OperatorGroupConstants.RESULT_GROUP,
      asScalaBuffer(singletonList(new InputPort("", false))).toList, List.empty)
  }

  override def getOutputSchema(schemas: Array[Schema]): Schema = {
    Preconditions.checkArgument(schemas.length == 1)
    schemas(0)
  }

  @JsonIgnore
  def setStorage(opResultStorage: OpResultStorage): Unit = {
    this.storage = opResultStorage
  }
}
