package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.web.model.websocket.request.LogicalPlanPojo
import edu.uci.ics.texera.web.storage.ExecutionStateStore
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, Schema}
import edu.uci.ics.texera.workflow.common.workflow.{LogicalPlan, SinkInjectionTransformer, WorkflowCompiler}

import scala.collection.mutable.ArrayBuffer

object WorkflowEditingService {
  def staticCheckWorkflow(
         logicalPlanPojo: LogicalPlanPojo,
         context: WorkflowContext): (Map[OperatorIdentity, List[Option[Schema]]], Map[OperatorIdentity, Throwable]) = {
    val errorList = new ArrayBuffer[(OperatorIdentity, Throwable)]()

    // convert the pojo to logical plan and inject sink
    var logicalPlan: LogicalPlan = LogicalPlan(logicalPlanPojo)
    logicalPlan = SinkInjectionTransformer.transform(
      logicalPlanPojo.opsToViewResult,
      logicalPlan
    )

    // propagate the schema, and do static error check
    val operatorIdToInputSchemasMapping = logicalPlan.propagateWorkflowSchema(context, Some(errorList))
    (operatorIdToInputSchemasMapping, errorList.toMap)
  }
}
