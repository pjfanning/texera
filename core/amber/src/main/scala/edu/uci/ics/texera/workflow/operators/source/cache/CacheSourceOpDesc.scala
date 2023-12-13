package edu.uci.ics.texera.workflow.operators.source.cache

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.workflow.common.metadata.{OperatorGroupConstants, OperatorInfo, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.schema.{OperatorSchemaInfo, Schema}
import edu.uci.ics.texera.workflow.operators.sink.storage.SinkStorageReader

import java.util.Collections.singletonList
import scala.collection.JavaConverters.asScalaBuffer

class CacheSourceOpDesc(resultStorage: SinkStorageReader)
    extends SourceOperatorDescriptor {

  override def sourceSchema(): Schema = resultStorage.getSchema

  override def getPhysicalOp(
      executionId: Long,
      operatorSchemaInfo: OperatorSchemaInfo
  ): PhysicalOp = {
    PhysicalOp.sourcePhysicalOperator(
      executionId,
      operatorIdentifier,
      OpExecInitInfo(_ => new CacheSourceOpExec(resultStorage)
    ))
  }

  override def operatorInfo: OperatorInfo =
    OperatorInfo(
      "Cache Source Operator",
      "Retrieve the cached output to src",
      OperatorGroupConstants.UTILITY_GROUP,
      List.empty,
      asScalaBuffer(singletonList(OutputPort(""))).toList
    )
}
