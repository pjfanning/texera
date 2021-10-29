package edu.uci.ics.texera.web.resource

import com.twitter.util.Future.Unit.unit
import com.twitter.util.{Future, FuturePool}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User
import edu.uci.ics.texera.web.model.websocket.event.error.WorkflowErrorEvent
import edu.uci.ics.texera.web.model.websocket.event.{
  TexeraWebSocketEvent,
  Uninitialized,
  WorkflowStateEvent
}
import edu.uci.ics.texera.web.model.websocket.request._
import edu.uci.ics.texera.web.model.websocket.request.python.PythonExpressionEvaluateRequest
import edu.uci.ics.texera.web.model.websocket.response._
import edu.uci.ics.texera.web.service.{WorkflowCacheService, WorkflowService}
import edu.uci.ics.texera.web.{ServletAwareConfigurator, SessionState, SessionStateManager}
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler.ConstraintViolationException

import java.util.concurrent.atomic.AtomicInteger
import javax.websocket._
import javax.websocket.server.ServerEndpoint
import scala.jdk.CollectionConverters.mapAsScalaMapConverter

object WorkflowWebsocketResource {
  val nextExecutionID = new AtomicInteger(0)
}

@ServerEndpoint(
  value = "/wsapi/workflow-websocket",
  configurator = classOf[ServletAwareConfigurator]
)
class WorkflowWebsocketResource extends LazyLogging {

  final val objectMapper = Utils.objectMapper

  @OnOpen
  def myOnOpen(session: Session, config: EndpointConfig): Unit = {
    SessionStateManager.setState(session.getId, new SessionState(session))
    logger.info("connection open")
  }

  @OnClose
  def myOnClose(session: Session, cr: CloseReason): Unit = {
    SessionStateManager.removeState(session.getId)
  }

  @OnMessage
  def myOnMsg(session: Session, message: String): Unit = {
    val request: TexeraWebSocketRequest =
      objectMapper.readValue(message, classOf[TexeraWebSocketRequest])

    val sessionState = SessionStateManager.getState(session.getId)
    sessionState.getCurrentWorkflowState match {
      case Some(_) => handleRequestAfterWorkflowState(request, session)
      case None    => handleRequestBeforeWorkflowState(request, session)
    }

  }

  def handleRequestBeforeWorkflowState(
      request: TexeraWebSocketRequest,
      session: Session
  ): Future[Unit] = {

    val uidOpt = session.getUserProperties.asScala
      .get(classOf[User].getName)
      .map(_.asInstanceOf[User].getUid)
    val sessionState = SessionStateManager.getState(session.getId)
    request match {
      case wIdRequest: RegisterWIdRequest =>
        // hack to refresh frontend run button state
        send(session, WorkflowStateEvent(Uninitialized))
        val workflowState = uidOpt match {
          case Some(user) =>
            val workflowStateId = user + "-" + wIdRequest.wId
            WorkflowService.getOrCreate(workflowStateId)
          case None =>
            // use a fixed wid for reconnection
              val workflowStateId = "dummy wid"
              WorkflowService.getOrCreate(workflowStateId)
            // Alternative:
            // anonymous session:set immediately cleanup
            //WorkflowService.getOrCreate("anonymous session " + session.getId, 0)
        }
        sessionState.subscribe(workflowState)
        send(session, RegisterWIdResponse("wid registered"))
      case heartbeat: HeartBeatRequest =>
        Future(HeartBeatResponse())
      case _ => Future(throw new UnsupportedOperationException)
    }
  }

  def handleRequestAfterWorkflowState(
      request: TexeraWebSocketRequest,
      session: Session
  ): Future[Any] = {
    val uidOpt = session.getUserProperties.asScala
      .get(classOf[User].getName)
      .map(_.asInstanceOf[User].getUid)
    val sessionState = SessionStateManager.getState(session.getId)
    val workflowState = sessionState.getCurrentWorkflowState.get

    (request match {

      case heartbeat: HeartBeatRequest =>
        Future(HeartBeatResponse())
      case execute: WorkflowExecuteRequest =>
        workflowState
          .initExecutionState(execute, uidOpt)
          .onSuccess(workflowJobService => workflowJobService.startWorkflow())
          .onFailure {
            case x: ConstraintViolationException =>
              send(session, WorkflowErrorEvent(operatorErrors = x.violations))
          }

      case newLogic: ModifyLogicRequest =>
        workflowState.getJobService().modifyLogic(newLogic)
      case pause: WorkflowPauseRequest =>
        workflowState.getJobService().workflowRuntimeService.pauseWorkflow()
      case resume: WorkflowResumeRequest =>
        workflowState.getJobService().workflowRuntimeService.resumeWorkflow()
      case kill: WorkflowKillRequest =>
        workflowState.getJobService().workflowRuntimeService.killWorkflow()
      case skipTupleRequest: SkipTupleRequest =>
        workflowState.getJobService().workflowRuntimeService.skipTuple(skipTupleRequest)
      case retryRequest: RetryRequest =>
        workflowState.getJobService().workflowRuntimeService.retryWorkflow()
      case req: AddBreakpointRequest =>
        workflowState
          .getJobService()
          .workflowRuntimeService
          .addBreakpoint(req.operatorID, req.breakpoint)

      case paginationRequest: ResultPaginationRequest =>
        workflowState
          .getJobService()
          .workflowResultService
          .handleResultPagination(paginationRequest)

      case resultExportRequest: ResultExportRequest =>
        workflowState.getJobService().exportResult(uidOpt.get, resultExportRequest)

      case cacheStatusUpdateRequest: CacheStatusUpdateRequest =>
        if (WorkflowCacheService.isAvailable) {
          workflowState.operatorCache.updateCacheStatus(cacheStatusUpdateRequest)
        }
        unit
      case pythonExpressionEvaluateRequest: PythonExpressionEvaluateRequest =>
        workflowState
          .getJobService()
          .workflowRuntimeService
          .evaluatePythonExpression(pythonExpressionEvaluateRequest)
      case _ => Future(throw new UnsupportedOperationException)
    }).onSuccess {
      case event: TexeraWebSocketEvent =>
        send(session, event)
      case _ =>
    }.onFailure(throwable =>
      send(
        session,
        WorkflowErrorEvent(generalErrors =
          Map("exception" -> (throwable.getMessage + "\n" + throwable.getStackTrace.mkString("\n")))
        )
      )
    )

  }

  private def send(session: Session, msg: TexeraWebSocketEvent): Future[Unit] = {
    FuturePool.unboundedPool {
      session.getAsyncRemote.sendText(objectMapper.writeValueAsString(msg)).get
    }
  }

}
