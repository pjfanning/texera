package edu.uci.ics.texera.workflow.common.operators.aggregate

import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecInitInfo
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.PhysicalOp
import edu.uci.ics.amber.engine.common.virtualidentity.{OperatorIdentity, PhysicalLink, PhysicalOpIdentity}
import edu.uci.ics.texera.workflow.common.metadata.{InputPort, OutputPort}
import edu.uci.ics.texera.workflow.common.operators.LogicalOp
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

object AggregateOpDesc {

  def opExecPhysicalPlan(
      executionId: Long,
      id: OperatorIdentity,
      aggFuncs: List[DistributedAggregation[Object]],
      groupByKeys: List[String],
      schemaInfo: OperatorSchemaInfo
  ): PhysicalPlan = {
    val partialLayer =
      PhysicalOp
        .oneToOneLayer(
          executionId,
          PhysicalOpIdentity(id, "localAgg"),
          OpExecInitInfo(_ => new PartialAggregateOpExec(aggFuncs, groupByKeys, schemaInfo))
        )
        // a hacky solution to have unique port names for reference purpose
        .copy(isOneToManyOp = true, inputPorts = List(InputPort("in")))

    val finalLayer = if (groupByKeys == null || groupByKeys.isEmpty) {
      PhysicalOp
        .localLayer(
          executionId,
          PhysicalOpIdentity(id, "globalAgg"),
          OpExecInitInfo(_ => new FinalAggregateOpExec(aggFuncs, groupByKeys, schemaInfo))
        )
        // a hacky solution to have unique port names for reference purpose
        .copy(isOneToManyOp = true, outputPorts = List(OutputPort("out")))
    } else {
      val partitionColumns: Array[Int] =
        if (groupByKeys == null) Array()
        else groupByKeys.indices.toArray // group by columns are always placed in the beginning

      PhysicalOp
        .hashLayer(
          executionId,
          PhysicalOpIdentity(id, "globalAgg"),
          OpExecInitInfo(_ => new FinalAggregateOpExec(aggFuncs, groupByKeys, schemaInfo)),
          partitionColumns
        )
        .copy(isOneToManyOp = true, outputPorts = List(OutputPort("out")))
    }

    new PhysicalPlan(
      List(partialLayer, finalLayer),
      List(PhysicalLink(partialLayer.id, 0, finalLayer.id, 0))
    )
  }

}

abstract class AggregateOpDesc extends LogicalOp {

  override def getPhysicalOp(
      executionId: Long,
      operatorSchemaInfo: OperatorSchemaInfo
  ): PhysicalOp = {
    throw new UnsupportedOperationException("multi-layer op should use getPhysicalPlan")
  }

  override def getPhysicalPlan(
      executionId: Long,
      operatorSchemaInfo: OperatorSchemaInfo
  ): PhysicalPlan = {
    var plan = aggregateOperatorExecutor(executionId, operatorSchemaInfo)
    plan.operators.foreach(op => plan = plan.setOperator(op.copy(isOneToManyOp = true)))
    plan
  }

  def aggregateOperatorExecutor(
      executionId: Long,
      operatorSchemaInfo: OperatorSchemaInfo
  ): PhysicalPlan

}
