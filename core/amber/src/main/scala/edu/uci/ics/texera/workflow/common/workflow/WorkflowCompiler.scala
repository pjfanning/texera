package edu.uci.ics.texera.workflow.common.workflow

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowPipelinedRegionsBuilder
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.{ConstraintViolation, WorkflowContext}

object WorkflowCompiler {

  def isSink(operatorID: String, workflowCompiler: WorkflowCompiler): Boolean = {
    val outLinks =
      workflowCompiler.logicalPlan.links.filter(link => link.origin.operatorID == operatorID)
    outLinks.isEmpty
  }

  class ConstraintViolationException(val violations: Map[String, Set[ConstraintViolation]])
      extends RuntimeException

}

class WorkflowCompiler(val logicalPlan: LogicalPlan, val context: WorkflowContext) {
  logicalPlan.operatorMap.values.foreach(initOperator)

  lazy val finalLogicalPlan = transformLogicalPlan(logicalPlan)

  def initOperator(operator: OperatorDescriptor): Unit = {
    operator.setContext(context)
  }

  def validate: Map[String, Set[ConstraintViolation]] =
    this.logicalPlan.operatorMap
      .map(o => (o._1, o._2.validate().toSet))
      .filter(o => o._2.nonEmpty)

  def transformLogicalPlan(originalPlan: LogicalPlan): LogicalPlan = {

    // logical plan transformation: add a sink operator for terminal operators without a sink
    SinkInjectionTransformer.transform(originalPlan, context)
  }

  def amberWorkflow(workflowId: WorkflowIdentity, opResultStorage: OpResultStorage): Workflow = {
    val physicalPlan0 = finalLogicalPlan.toPhysicalPlan(this.context, opResultStorage)

    // create pipelined regions.
    val physicalPlan1 = new WorkflowPipelinedRegionsBuilder(
      workflowId,
      logicalPlan,
      physicalPlan0,
      new MaterializationRewriter(context, opResultStorage)
    ).buildPipelinedRegions()

    // assign link strategies
    val physicalPlan2 = new PartitionEnforcer(physicalPlan1).enforcePartition()

    new Workflow(workflowId, physicalPlan2)
  }

}
