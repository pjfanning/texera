package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{ActorContext, Cancellable}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.MonitoringHandler.ControllerInitiateMonitoring
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.SkewDetectionHandler.ControllerInitiateSkewDetection
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers._
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayload
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.{
  AsyncRPCClient,
  AsyncRPCHandlerInitializer,
  AsyncRPCServer
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration, MILLISECONDS}

trait ControllerAsyncRPCHandlerInitializer
    extends AsyncRPCHandlerInitializer
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
    with DebugCommandHandler {

  this: ControllerProcessor =>

  var statusUpdateAskHandle: Option[Cancellable] = None
  var monitoringHandle: Option[Cancellable] = None
  var workflowReshapeState: WorkflowReshapeState = new WorkflowReshapeState()
  var interactionHistory: mutable.ArrayBuffer[(Int, Map[ActorVirtualIdentity, Long])] =
    new ArrayBuffer[(Int, Map[ActorVirtualIdentity, Long])]()
  val workflowStartTimeStamp: Long = System.currentTimeMillis()
  var suppressStatusUpdate = false

  def enableStatusUpdate(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (controllerConfig.statusUpdateIntervalMs.nonEmpty && statusUpdateAskHandle.isEmpty) {
      println("status update enabled")
      statusUpdateAskHandle = Option(
        actorContext.system.scheduler.scheduleAtFixedRate(
          0.milliseconds,
          FiniteDuration.apply(controllerConfig.statusUpdateIntervalMs.get, MILLISECONDS),
          actorContext.self,
          ControlInvocation(
            AsyncRPCClient.IgnoreReplyAndDoNotLog,
            ControllerInitiateQueryStatistics()
          )
        )
      )
    }
  }

  def enableMonitoring(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.monitoringEnabled && controllerConfig.monitoringIntervalMs.nonEmpty && monitoringHandle.isEmpty
    ) {
      monitoringHandle = Option(
        actorContext.system.scheduler.scheduleAtFixedRate(
          0.milliseconds,
          FiniteDuration.apply(controllerConfig.monitoringIntervalMs.get, MILLISECONDS),
          actorContext.self,
          ControlInvocation(
            AsyncRPCClient.IgnoreReplyAndDoNotLog,
            ControllerInitiateMonitoring()
          )
        )
      )
    }
  }

  def enableSkewHandling(): Unit = {
    if (suppressStatusUpdate) {
      return
    }
    if (
      Constants.reshapeSkewHandlingEnabled && controllerConfig.skewDetectionIntervalMs.nonEmpty && workflowReshapeState.skewDetectionHandle.isEmpty
    ) {
      workflowReshapeState.skewDetectionHandle = Option(
        actorContext.system.scheduler.scheduleAtFixedRate(
          Constants.reshapeSkewDetectionInitialDelayInMs.milliseconds,
          FiniteDuration.apply(controllerConfig.skewDetectionIntervalMs.get, MILLISECONDS),
          actorContext.self,
          ControlInvocation(
            AsyncRPCClient.IgnoreReplyAndDoNotLog,
            ControllerInitiateSkewDetection()
          )
        )
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
