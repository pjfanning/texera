package edu.uci.ics.texera.web.service

import com.google.protobuf.timestamp.Timestamp
import com.twitter.util.{Await, Duration}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.StartWorkflowHandler.StartWorkflow
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity
import edu.uci.ics.texera.web.model.websocket.request.WorkflowExecuteRequest
import edu.uci.ics.texera.web.storage.JobStateStore
import edu.uci.ics.texera.web.storage.JobStateStore.updateWorkflowState
import edu.uci.ics.texera.web.workflowruntimestate.ErrorType.{COMPILATION_ERROR, FAILURE}
import edu.uci.ics.texera.web.workflowruntimestate.JobError
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{FAILED, READY, RUNNING}
import edu.uci.ics.texera.web.{SubscriptionManager, TexeraWebApplication, WebsocketInput}
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.workflow.{LogicalPlan, WorkflowCompiler}
import edu.uci.ics.texera.workflow.operators.udf.python.source.PythonUDFSourceOpDescV2
import edu.uci.ics.texera.workflow.operators.udf.python.{
  DualInputPortsPythonUDFOpDescV2,
  PythonUDFOpDescV2
}

import java.time.Instant

class WorkflowJobService(
    workflowContext: WorkflowContext,
    resultService: JobResultService,
    request: WorkflowExecuteRequest,
    lastCompletedLogicalPlan: Option[LogicalPlan]
) extends SubscriptionManager
    with LazyLogging {

  val errorHandler: Throwable => Unit = { t =>
    {
      t.printStackTrace()
      stateStore.statsStore.updateState(stats => stats.withEndTimeStamp(System.currentTimeMillis()))
      stateStore.jobMetadataStore.updateState { jobInfo =>
        updateWorkflowState(FAILED, jobInfo).addErrors(
          JobError(FAILURE, Timestamp(Instant.now), t.getMessage, t.getStackTrace.mkString("\n"))
        )
      }
    }
  }
  val wsInput = new WebsocketInput(errorHandler)
  val stateStore = new JobStateStore()

  var logicalPlan: LogicalPlan = _
  var workflowCompiler: WorkflowCompiler = _
  var workflow: Workflow = _

  workflowCompilation()

  def workflowCompilation(): Unit = {
    logicalPlan = LogicalPlan(request.logicalPlan, workflowContext)
    logicalPlan.initializeLogicalPlan(stateStore)
    try {
      workflowCompiler = createWorkflowCompiler(logicalPlan)
      workflow = workflowCompiler.amberWorkflow(
        WorkflowIdentity(workflowContext.jobId),
        resultService.opResultStorage,
        lastCompletedLogicalPlan
      )
    } catch {
      case e: Throwable =>
        stateStore.jobMetadataStore.updateState { metadataStore =>
          updateWorkflowState(FAILED, metadataStore)
            .addErrors(
              JobError(
                COMPILATION_ERROR,
                Timestamp(Instant.now),
                e.getMessage,
                e.getStackTrace.mkString("\n")
              )
            )
        }
    }
  }

  private val controllerConfig = {
    val conf = ControllerConfig.default
    if (
      workflowCompiler.logicalPlan.operators.exists {
        case _: DualInputPortsPythonUDFOpDescV2 => true
        case _: PythonUDFOpDescV2               => true
        case _: PythonUDFSourceOpDescV2         => true
        case _                                  => false
      }
    ) {
      conf.supportFaultTolerance = false
    }
    conf
  }

  // Runtime starts from here:
  var client: AmberClient = _
  var jobBreakpointService: JobBreakpointService = _
  var jobReconfigurationService: JobReconfigurationService = _
  var jobStatsService: JobStatsService = _
  var jobRuntimeService: JobRuntimeService = _
  var jobPythonService: JobConsoleService = _

  def startWorkflow(): Unit = {
    client = TexeraWebApplication.createAmberRuntime(
      workflow,
      controllerConfig,
      errorHandler
    )
    jobBreakpointService = new JobBreakpointService(client, stateStore)
    jobReconfigurationService =
      new JobReconfigurationService(client, stateStore, workflowCompiler, workflow)
    jobStatsService = new JobStatsService(client, stateStore)
    jobRuntimeService = new JobRuntimeService(
      client,
      stateStore,
      wsInput,
      jobBreakpointService,
      jobReconfigurationService
    )
    jobPythonService = new JobConsoleService(client, stateStore, wsInput, jobBreakpointService)

    for (pair <- workflowCompiler.logicalPlan.breakpoints) {
      Await.result(
        jobBreakpointService.addBreakpoint(pair.operatorID, pair.breakpoint),
        Duration.fromSeconds(10)
      )
    }
    resultService.attachToJob(stateStore, workflowCompiler.logicalPlan, client)
    stateStore.jobMetadataStore.updateState(jobInfo =>
      updateWorkflowState(READY, jobInfo.withEid(workflowContext.executionID)).withErrors(Seq.empty)
    )
    stateStore.statsStore.updateState(stats => stats.withStartTimeStamp(System.currentTimeMillis()))
    client.sendAsyncWithCallback[Unit](
      StartWorkflow(),
      _ => stateStore.jobMetadataStore.updateState(jobInfo => updateWorkflowState(RUNNING, jobInfo))
    )
  }

  private[this] def createWorkflowCompiler(
      logicalPlan: LogicalPlan
  ): WorkflowCompiler = {
    new WorkflowCompiler(logicalPlan)
  }

  override def unsubscribeAll(): Unit = {
    super.unsubscribeAll()
    if (client != null) {
      // runtime created
      jobBreakpointService.unsubscribeAll()
      jobRuntimeService.unsubscribeAll()
      jobPythonService.unsubscribeAll()
      jobStatsService.unsubscribeAll()
      jobReconfigurationService.unsubscribeAll()
    }
  }

}
