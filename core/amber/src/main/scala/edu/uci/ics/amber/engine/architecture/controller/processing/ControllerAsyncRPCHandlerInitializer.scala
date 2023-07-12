package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.{
  AssignBreakpointHandler,
  DebugCommandHandler,
  EpochMarkerHandler,
  EvaluatePythonExpressionHandler,
  FatalErrorHandler,
  LinkCompletedHandler,
  LinkWorkersHandler,
  LocalBreakpointTriggeredHandler,
  LocalOperatorExceptionHandler,
  ModifyLogicHandler,
  MonitoringHandler,
  PauseHandler,
  PythonConsoleMessageHandler,
  QueryWorkerStatisticsHandler,
  RegionsTimeSlotExpiredHandler,
  ResumeHandler,
  RetryWorkflowHandler,
  SkewDetectionHandler,
  StartWorkflowHandler,
  WorkerExecutionCompletedHandler,
  WorkerExecutionStartedHandler
}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReshapeState
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.MonitoringHandler.ControllerInitiateMonitoring
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.SkewDetectionHandler.ControllerInitiateSkewDetection
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ModifyLogicHandler
import edu.uci.ics.amber.engine.common.ambermessage.ControlInvocation
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.concurrent.duration.{DurationInt, FiniteDuration, MILLISECONDS}

class ControllerAsyncRPCHandlerInitializer(val cp: ControlProcessor)
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
    with EpochMarkerHandler {

  val actorId: ActorVirtualIdentity = cp.actorId

  @transient
  var statusUpdateAskHandle: Cancellable = null
  @transient
  var monitoringHandle: Cancellable = null
  var workflowReshapeState: WorkflowReshapeState = new WorkflowReshapeState()
  var workflowStartTimeStamp: Long = System.currentTimeMillis()
  var workflowPauseStartTime: Long = 0L
  var suppressStatusUpdate = false

  def enableStatusUpdate(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      cp.controller.controllerConfig.statusUpdateIntervalMs.nonEmpty && statusUpdateAskHandle == null
    ) {
      println("status update enabled")
      statusUpdateAskHandle = cp.actorService.scheduleWithFixedDelay(
        0.milliseconds,
        FiniteDuration
          .apply(cp.controller.controllerConfig.statusUpdateIntervalMs.get, MILLISECONDS),
        () => cp.actorService.self ! ControlInvocation(ControllerInitiateQueryStatistics())
      )
    }
  }

  def enableMonitoring(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.monitoringEnabled && cp.controller.controllerConfig.monitoringIntervalMs.nonEmpty && monitoringHandle == null
    ) {
      monitoringHandle = cp.actorService.scheduleWithFixedDelay(
        0.milliseconds,
        FiniteDuration
          .apply(cp.controller.controllerConfig.statusUpdateIntervalMs.get, MILLISECONDS),
        () => cp.actorService.self ! ControlInvocation(ControllerInitiateMonitoring())
      )
    }
  }

  def enableSkewHandling(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.reshapeSkewHandlingEnabled && cp.controller.controllerConfig.skewDetectionIntervalMs.nonEmpty && workflowReshapeState.skewDetectionHandle == null
    ) {
      workflowReshapeState.skewDetectionHandle = cp.actorService.scheduleWithFixedDelay(
        0.milliseconds,
        FiniteDuration
          .apply(cp.controller.controllerConfig.statusUpdateIntervalMs.get, MILLISECONDS),
        () => cp.actorService.self ! ControlInvocation(ControllerInitiateSkewDetection())
      )
    }
  }

  def disableStatusUpdate(): Unit = {
    if (statusUpdateAskHandle != null) {
      println("status update disabled")
      statusUpdateAskHandle.cancel()
      statusUpdateAskHandle = null
    }
  }

  def disableMonitoring(): Unit = {
    if (monitoringHandle != null) {
      monitoringHandle.cancel()
      monitoringHandle = null
    }
  }

  def disableSkewHandling(): Unit = {
    if (workflowReshapeState.skewDetectionHandle != null) {
      workflowReshapeState.skewDetectionHandle.cancel()
      workflowReshapeState.skewDetectionHandle = null
    }
  }

}
