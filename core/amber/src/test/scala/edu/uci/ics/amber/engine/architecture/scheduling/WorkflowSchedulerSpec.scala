package edu.uci.ics.amber.engine.architecture.scheduling

import com.typesafe.scalalogging.Logger
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, ExecutionState, Workflow}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.COMPLETED
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  LinkIdentity,
  OperatorIdentity,
  WorkflowIdentity
}
import edu.uci.ics.amber.engine.e2e.TestOperators
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.{
  LogicalPlan,
  OperatorLink,
  OperatorPort,
  WorkflowCompiler
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.LoggerFactory

class WorkflowSchedulerSpec extends AnyFlatSpec with MockFactory {

  def buildWorkflow(
      operators: List[OperatorDescriptor],
      links: List[OperatorLink]
  ): Workflow = {
    val context = new WorkflowContext
    context.jobId = "workflow-test"

    val texeraWorkflowCompiler = new WorkflowCompiler(
      LogicalPlan(context, operators, links, List())
    )
    texeraWorkflowCompiler.amberWorkflow(WorkflowIdentity("workflow-test"), new OpResultStorage())
  }

  def setOperatorCompleted(
      workflow: Workflow,
      executionState: ExecutionState,
      opID: String
  ): Unit = {
    val opIdentity = new OperatorIdentity(workflow.workflowId.id, opID)
    val layers = workflow.physicalPlan.layersOfLogicalOperator(opIdentity)
    layers.foreach { layer =>
      executionState.getOperatorExecution(layer.id).setAllWorkerState(COMPLETED)
    }
  }

  "Scheduler" should "correctly schedule regions in headerlessCsv->keyword->sink workflow" in {
    val headerlessCsvOpDesc = TestOperators.headerlessSmallCsvScanOpDesc()
    val keywordOpDesc = TestOperators.keywordSearchOpDesc("column-1", "Asia")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(headerlessCsvOpDesc, keywordOpDesc, sink),
      List(
        OperatorLink(
          OperatorPort(headerlessCsvOpDesc.operatorID, 0),
          OperatorPort(keywordOpDesc.operatorID, 0)
        ),
        OperatorLink(OperatorPort(keywordOpDesc.operatorID, 0), OperatorPort(sink.operatorID, 0))
      )
    )
    val executionState = new ExecutionState(workflow)
    val scheduler =
      new WorkflowScheduler(workflow, ControllerConfig.default, executionState, null)
    Set(headerlessCsvOpDesc.operatorID, keywordOpDesc.operatorID, sink.operatorID).foreach(opID =>
      setOperatorCompleted(workflow, executionState, opID)
    )
    scheduler.schedulingPolicy.addToRunningRegions(
      scheduler.schedulingPolicy.startWorkflow(workflow),
      null
    )
    val nextRegions =
      scheduler.schedulingPolicy.onWorkerCompletion(
        workflow,
        executionState,
        ActorVirtualIdentity("Scan worker 0")
      )
    assert(nextRegions.isEmpty)
    assert(scheduler.schedulingPolicy.getCompletedRegions().size == 1)
  }

  "Scheduler" should "correctly schedule regions in buildcsv->probecsv->hashjoin->hashjoin->sink workflow" in {
    val buildCsv = TestOperators.headerlessSmallCsvScanOpDesc()
    val probeCsv = TestOperators.smallCsvScanOpDesc()
    val hashJoin1 = TestOperators.joinOpDesc("column-1", "Region")
    val hashJoin2 = TestOperators.joinOpDesc("column-2", "Country")
    val sink = TestOperators.sinkOpDesc()
    val workflow = buildWorkflow(
      List(
        buildCsv,
        probeCsv,
        hashJoin1,
        hashJoin2,
        sink
      ),
      List(
        OperatorLink(
          OperatorPort(buildCsv.operatorID, 0),
          OperatorPort(hashJoin1.operatorID, 0)
        ),
        OperatorLink(
          OperatorPort(probeCsv.operatorID, 0),
          OperatorPort(hashJoin1.operatorID, 1)
        ),
        OperatorLink(
          OperatorPort(buildCsv.operatorID, 0),
          OperatorPort(hashJoin2.operatorID, 0)
        ),
        OperatorLink(
          OperatorPort(hashJoin1.operatorID, 0),
          OperatorPort(hashJoin2.operatorID, 1)
        ),
        OperatorLink(
          OperatorPort(hashJoin2.operatorID, 0),
          OperatorPort(sink.operatorID, 0)
        )
      )
    )
    val logger = Logger(
      LoggerFactory.getLogger(s"WorkflowSchedulerTest")
    )
    val executionState = new ExecutionState(workflow)
    val scheduler =
      new WorkflowScheduler(workflow, ControllerConfig.default, executionState, null)
    scheduler.schedulingPolicy.addToRunningRegions(
      scheduler.schedulingPolicy.startWorkflow(workflow),
      null
    )
    Set(buildCsv.operatorID).foreach(opID => setOperatorCompleted(workflow, executionState, opID))

    var nextRegions =
      scheduler.schedulingPolicy.onWorkerCompletion(
        workflow,
        executionState,
        ActorVirtualIdentity("Build Scan worker 0")
      )
    assert(nextRegions.isEmpty)

    nextRegions = scheduler.schedulingPolicy.onLinkCompletion(
      workflow,
      executionState,
      LinkIdentity(
        workflow.physicalPlan
          .layersOfLogicalOperator(
            new OperatorIdentity(workflow.workflowId.id, buildCsv.operatorID)
          )
          .last
          .id,
        workflow.physicalPlan
          .layersOfLogicalOperator(
            new OperatorIdentity(workflow.workflowId.id, hashJoin1.operatorID)
          )
          .head
          .id
      )
    )
    assert(nextRegions.isEmpty)
    nextRegions = scheduler.schedulingPolicy.onLinkCompletion(
      workflow,
      executionState,
      LinkIdentity(
        workflow.physicalPlan
          .layersOfLogicalOperator(
            new OperatorIdentity(workflow.workflowId.id, buildCsv.operatorID)
          )
          .last
          .id,
        workflow.physicalPlan
          .layersOfLogicalOperator(
            new OperatorIdentity(workflow.workflowId.id, hashJoin2.operatorID)
          )
          .head
          .id
      )
    )
    assert(nextRegions.nonEmpty)
    assert(scheduler.schedulingPolicy.getCompletedRegions().size == 1)
    scheduler.schedulingPolicy.addToRunningRegions(nextRegions, null)
    Set(probeCsv.operatorID, hashJoin1.operatorID, hashJoin2.operatorID, sink.operatorID).foreach(
      opID => setOperatorCompleted(workflow, executionState, opID)
    )
    nextRegions = scheduler.schedulingPolicy.onWorkerCompletion(
      workflow,
      executionState,
      ActorVirtualIdentity("Probe Scan worker 0")
    )
    assert(nextRegions.isEmpty)
    assert(scheduler.schedulingPolicy.getCompletedRegions().size == 2)
  }

}
