package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.{AssignBreakpointHandler, DebugCommandHandler, EpochMarkerHandler, EvaluatePythonExpressionHandler, FatalErrorHandler, LinkCompletedHandler, LinkWorkersHandler, LocalBreakpointTriggeredHandler, LocalOperatorExceptionHandler, ModifyLogicHandler, MonitoringHandler, PauseHandler, PythonConsoleMessageHandler, QueryWorkerStatisticsHandler, RegionsTimeSlotExpiredHandler, ReportCheckpointStatsHandler, ResumeHandler, RetryWorkflowHandler, SkewDetectionHandler, StartWorkflowHandler, WorkerExecutionCompletedHandler, WorkerExecutionStartedHandler}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReshapeState
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.MonitoringHandler.ControllerInitiateMonitoring
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.SkewDetectionHandler.ControllerInitiateSkewDetection
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration, MILLISECONDS}

class ControllerAsyncRPCHandlerInitializer(val cp: ControllerProcessor)
    extends AsyncRPCHandlerInitializer(cp.asyncRPCClient, cp.asyncRPCServer)
    with AmberLogging
    with LinkWorkersHandler
    with AssignBreakpointHandler
    with WorkerExecutionCompletedHandler
    with WorkerExecutionStartedHandler
    with LocalBreakpointTriggeredHandler
    with LocalOperatorExceptionHandler
    with PauseHandler
    with QueryWorkerStatisticsHandler
    with ResumeHandler
    with StartWorkflowHandler
    with LinkCompletedHandler
    with FatalErrorHandler
    with PythonConsoleMessageHandler
    with RetryWorkflowHandler
    with ModifyLogicHandler
    with EvaluatePythonExpressionHandler
    with MonitoringHandler
    with SkewDetectionHandler
    with RegionsTimeSlotExpiredHandler
    with DebugCommandHandler
      with EpochMarkerHandler with ReportCheckpointStatsHandler{

  val actorId: ActorVirtualIdentity = cp.actorId

  @transient
  var statusUpdateAskHandle: Option[Cancellable] = None
  @transient
  var monitoringHandle: Option[Cancellable] = None
  var workflowReshapeState: WorkflowReshapeState = new WorkflowReshapeState()
  var workflowStartTimeStamp: Long = System.currentTimeMillis()
  var workflowPauseStartTime: Long = 0L
  var suppressStatusUpdate = false

  def enableStatusUpdate(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (cp.controllerConfig.statusUpdateIntervalMs.nonEmpty && statusUpdateAskHandle.isEmpty) {
      println("status update enabled")
      statusUpdateAskHandle = Option(
        cp.actorContext.system.scheduler.scheduleAtFixedRate(
          0.milliseconds,
          FiniteDuration.apply(cp.controllerConfig.statusUpdateIntervalMs.get, MILLISECONDS),
          cp.actorContext.self,
          ControlInvocation(ControllerInitiateQueryStatistics())
        )(cp.executor)
      )
    }
  }

  def enableMonitoring(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.monitoringEnabled && cp.controllerConfig.monitoringIntervalMs.nonEmpty && monitoringHandle.isEmpty
    ) {
      monitoringHandle = Option(
        cp.actorContext.system.scheduler.scheduleAtFixedRate(
          0.milliseconds,
          FiniteDuration.apply(cp.controllerConfig.monitoringIntervalMs.get, MILLISECONDS),
          cp.actorContext.self,
          ControlInvocation(
            ControllerInitiateMonitoring()
          )
        )(cp.executor)
      )
    }
  }

  def enableSkewHandling(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.reshapeSkewHandlingEnabled && cp.controllerConfig.skewDetectionIntervalMs.nonEmpty && workflowReshapeState.skewDetectionHandle.isEmpty
    ) {
      workflowReshapeState.skewDetectionHandle = Option(
        cp.actorContext.system.scheduler.scheduleAtFixedRate(
          Constants.reshapeSkewDetectionInitialDelayInMs.milliseconds,
          FiniteDuration.apply(cp.controllerConfig.skewDetectionIntervalMs.get, MILLISECONDS),
          cp.actorContext.self,
          ControlInvocation(
            ControllerInitiateSkewDetection()
          )
        )(cp.executor)
      )
    }
  }

  def disableStatusUpdate(): Unit = {
    if (statusUpdateAskHandle.nonEmpty) {
      println("status update disabled")
      statusUpdateAskHandle.get.cancel()
      statusUpdateAskHandle = Option.empty
    }
  }

  def disableMonitoring(): Unit = {
    if (monitoringHandle.nonEmpty) {
      monitoringHandle.get.cancel()
      monitoringHandle = Option.empty
    }
  }

  def disableSkewHandling(): Unit = {
    if (workflowReshapeState.skewDetectionHandle.nonEmpty) {
      workflowReshapeState.skewDetectionHandle.get.cancel()
      workflowReshapeState.skewDetectionHandle = Option.empty
    }
  }

}
