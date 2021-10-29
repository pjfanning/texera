package edu.uci.ics.texera.web.service

import com.google.common.collect.EvictingQueue
import com.twitter.util.Future
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.{
  ConditionalGlobalBreakpoint,
  CountGlobalBreakpoint
}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent._
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.AssignBreakpointHandler.AssignGlobalBreakpoint
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ModifyLogicHandler.ModifyLogic
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.RetryWorkflowHandler.RetryWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.StartWorkflowHandler.StartWorkflow
import edu.uci.ics.amber.engine.architecture.principal.{OperatorState, OperatorStatistics}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.web.SnapshotMulticast
import edu.uci.ics.texera.web.model.common.FaultedTupleFrontend
import edu.uci.ics.texera.web.model.websocket.event._
import edu.uci.ics.texera.web.model.websocket.event.error.WorkflowExecutionErrorEvent
import edu.uci.ics.texera.web.model.websocket.event.python.PythonPrintTriggeredEvent
import edu.uci.ics.texera.web.model.websocket.request.python.PythonExpressionEvaluateRequest
import edu.uci.ics.texera.web.model.websocket.request.{RemoveBreakpointRequest, SkipTupleRequest}
import edu.uci.ics.texera.web.model.websocket.response.python.PythonExpressionEvaluateResponse
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.workflow.{
  Breakpoint,
  BreakpointCondition,
  ConditionBreakpoint,
  CountBreakpoint
}
import rx.lang.scala.subjects.BehaviorSubject
import rx.lang.scala.{Observable, Observer}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object JobRuntimeService {
  val bufferSize: Int = AmberUtils.amberConfig.getInt("web-server.python-console-buffer-size")
}

class JobRuntimeService(workflowStatus: BehaviorSubject[ExecutionStatusEnum], client: AmberClient)
    extends SnapshotMulticast[TexeraWebSocketEvent]
    with LazyLogging {

  val operatorRuntimeStateMap: mutable.HashMap[String, OperatorRuntimeState] =
    new mutable.HashMap[String, OperatorRuntimeState]()
  var workflowError: Throwable = _

  /** *
    *  Utility Functions
    */

  def startWorkflow(): Future[Unit] = {
    val f = client.sendAsync(StartWorkflow())
    workflowStatus.onNext(Initializing)
    f.map { _ =>
      workflowStatus.onNext(Running)
    }
  }

  registerCallbacks()

  def getStatus: ExecutionStatusEnum = workflowStatus.asJavaSubject.getValue

  def getStatusObservable: Observable[ExecutionStatusEnum] = workflowStatus

  def addBreakpoint(
      operatorID: String,
      breakpoint: Breakpoint
  ): Future[List[ActorVirtualIdentity]] = {
    val breakpointID = "breakpoint-" + operatorID + "-" + System.currentTimeMillis()
    breakpoint match {
      case conditionBp: ConditionBreakpoint =>
        val column = conditionBp.column
        val predicate: Tuple => Boolean = conditionBp.condition match {
          case BreakpointCondition.EQ =>
            tuple => {
              tuple.getField(column).toString.trim == conditionBp.value
            }
          case BreakpointCondition.LT =>
            tuple => tuple.getField(column).toString.trim < conditionBp.value
          case BreakpointCondition.LE =>
            tuple => tuple.getField(column).toString.trim <= conditionBp.value
          case BreakpointCondition.GT =>
            tuple => tuple.getField(column).toString.trim > conditionBp.value
          case BreakpointCondition.GE =>
            tuple => tuple.getField(column).toString.trim >= conditionBp.value
          case BreakpointCondition.NE =>
            tuple => tuple.getField(column).toString.trim != conditionBp.value
          case BreakpointCondition.CONTAINS =>
            tuple => tuple.getField(column).toString.trim.contains(conditionBp.value)
          case BreakpointCondition.NOT_CONTAINS =>
            tuple => !tuple.getField(column).toString.trim.contains(conditionBp.value)
        }

        client.sendAsync(
          AssignGlobalBreakpoint(
            new ConditionalGlobalBreakpoint(
              breakpointID,
              tuple => {
                val texeraTuple = tuple.asInstanceOf[Tuple]
                predicate.apply(texeraTuple)
              }
            ),
            operatorID
          )
        )
      case countBp: CountBreakpoint =>
        client.sendAsync(
          AssignGlobalBreakpoint(new CountGlobalBreakpoint(breakpointID, countBp.count), operatorID)
        )
    }
  }

  override def sendSnapshotTo(observer: Observer[TexeraWebSocketEvent]): Unit = {
    observer.onNext(OperatorStatisticsUpdateEvent(operatorRuntimeStateMap.map {
      case (opId, state) => (opId, state.stats)
    }.toMap))
    operatorRuntimeStateMap.foreach {
      case (opId, state) =>
        if (state.faults.nonEmpty) {
          observer.onNext(BreakpointTriggeredEvent(state.faults.toArray, opId))
        }
        if (!state.pythonMessages.isEmpty) {
          val stringBuilder = new StringBuilder()
          state.pythonMessages.forEach(s => stringBuilder.append(s))
          observer.onNext(PythonPrintTriggeredEvent(stringBuilder.toString(), opId))
        }
    }
    if (workflowError != null) {
      observer.onNext(WorkflowExecutionErrorEvent(workflowError.getLocalizedMessage))
    }
  }

  def skipTuple(tupleReq: SkipTupleRequest): Future[Unit] = {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }

  def modifyLogic(operatorDescriptor: OperatorDescriptor): Future[Unit] = {
    client.sendAsync(ModifyLogic(operatorDescriptor))
  }

  def retryWorkflow(): Future[Unit] = {
    clearTriggeredBreakpoints()
    val f = client.sendAsync(RetryWorkflow())
    workflowStatus.onNext(Resuming)
    f.map { _ =>
      workflowStatus.onNext(Running)
    }
  }

  def clearTriggeredBreakpoints(): Unit = {
    operatorRuntimeStateMap.values.foreach { state =>
      state.faults.clear()
    }
  }

  def pauseWorkflow(): Future[Unit] = {
    val f = client.sendAsync(PauseWorkflow())
    workflowStatus.onNext(Pausing)
    f.map { _ =>
      workflowStatus.onNext(Paused)
    }
  }

  def resumeWorkflow(): Future[Unit] = {
    clearTriggeredBreakpoints()
    val f = client.sendAsync(ResumeWorkflow())
    workflowStatus.onNext(Resuming)
    f.map { _ =>
      workflowStatus.onNext(Running)
    }
  }

  def killWorkflow(): Future[Unit] = {
    client.shutdown()
    Future.value(workflowStatus.onNext(Completed))
  }

  def removeBreakpoint(removeBreakpoint: RemoveBreakpointRequest): Future[Unit] = {
    Future.exception(new NotImplementedError())
  }

  def evaluatePythonExpression(
      request: PythonExpressionEvaluateRequest
  ): Future[PythonExpressionEvaluateResponse] = {
    client.sendAsync(EvaluatePythonExpression(request.expression, request.operatorId))
  }

  private[this] def registerCallbacks(): Unit = {
    registerCallbackOnBreakpoint()
    registerCallbackOnFatalError()
    registerCallbackOnPythonPrint()
    registerCallbackOnWorkflowComplete()
    registerCallbackOnWorkflowStatusUpdate()
  }

  /** *
    *  Callback Functions to register upon construction
    */
  private[this] def registerCallbackOnBreakpoint(): Unit = {
    client
      .getObservable[BreakpointTriggered]
      .subscribe((evt: BreakpointTriggered) => {
        val faults = operatorRuntimeStateMap(evt.operatorID).faults
        for (elem <- evt.report) {
          val actorPath = elem._1._1.toString
          val faultedTuple = elem._1._2
          if (faultedTuple != null) {
            faults += BreakpointFault(actorPath, FaultedTupleFrontend.apply(faultedTuple), elem._2)
          }
        }
        workflowStatus.onNext(Paused)
        send(BreakpointTriggeredEvent(faults.toArray, evt.operatorID))
      })
  }

  private[this] def registerCallbackOnWorkflowStatusUpdate(): Unit = {
    client
      .getObservable[WorkflowStatusUpdate]
      .subscribe((evt: WorkflowStatusUpdate) => {
        evt.operatorStatistics.foreach {
          case (opId, statistics) =>
            if (!operatorRuntimeStateMap.contains(opId)) {
              operatorRuntimeStateMap(opId) = new OperatorRuntimeState()
            }
            operatorRuntimeStateMap(opId).stats = statistics
        }
        send(OperatorStatisticsUpdateEvent(evt))
      })
  }

  private[this] def registerCallbackOnWorkflowComplete(): Unit = {
    client
      .getObservable[WorkflowCompleted]
      .subscribe((evt: WorkflowCompleted) => {
        client.shutdown()
        workflowStatus.onNext(Completed)
      })
  }

  private[this] def registerCallbackOnPythonPrint(): Unit = {
    client
      .getObservable[PythonPrintTriggered]
      .subscribe((evt: PythonPrintTriggered) => {
        operatorRuntimeStateMap(evt.operatorID).pythonMessages.add(evt.message)
        send(PythonPrintTriggeredEvent(evt))
      })
  }

  private[this] def registerCallbackOnFatalError(): Unit = {
    client
      .getObservable[FatalError]
      .subscribe((evt: FatalError) => {
        client.shutdown()
        workflowError = evt.e
        workflowStatus.onNext(Aborted)
        send(WorkflowExecutionErrorEvent(evt.e.getLocalizedMessage))
      })
  }

  class OperatorRuntimeState {
    val pythonMessages: EvictingQueue[String] =
      EvictingQueue.create[String](JobRuntimeService.bufferSize)
    val faults: mutable.ArrayBuffer[BreakpointFault] = new ArrayBuffer[BreakpointFault]()
    var stats: OperatorStatistics = OperatorStatistics(OperatorState.Uninitialized, 0, 0)
  }

}
