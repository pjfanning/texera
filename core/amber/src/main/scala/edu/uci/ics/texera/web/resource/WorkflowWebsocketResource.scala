package edu.uci.ics.texera.web.resource

import com.twitter.util.Future
import com.twitter.util.Future.Unit.unit
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
import org.jooq.types.UInteger

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
    val uIdOpt = session.getUserProperties.asScala
      .get(classOf[User].getName)
      .map(_.asInstanceOf[User].getUid)
    val sessionState = SessionStateManager.getState(session.getId)

    val responseFuture = sessionState.getCurrentWorkflowState match {
      case Some(_) => handleRequestWithWorkflowState(request, session, uIdOpt)
      case None    => handleRequestWithoutWorkflowState(request, session, uIdOpt)
    }
    responseFuture
      .onSuccess {
        case event: TexeraWebSocketEvent =>
          send(session, event)
        case _ => // do nothing
      }
      .onFailure(throwable => {
        logger.error("error", throwable)
        send(
          session,
          WorkflowErrorEvent(generalErrors =
            Map(
              "exception" -> (throwable.getMessage + "\n" + throwable.getStackTrace
                .mkString("\n"))
            )
          )
        )
      })

  }

  def handleRequestWithoutWorkflowState(
      request: TexeraWebSocketRequest,
      session: Session,
      uIdOpt: Option[UInteger]
  ): Future[TexeraWebSocketEvent] = {

    val sessionState = SessionStateManager.getState(session.getId)
    request match {
      case wIdRequest: RegisterWIdRequest =>
        // hack to refresh frontend run button state
        send(session, WorkflowStateEvent(Uninitialized))
        val workflowState = uIdOpt match {
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
        Future(RegisterWIdResponse("wid registered"))
      case heartbeat: HeartBeatRequest =>
        Future(HeartBeatResponse())
      case _ => Future.exception(new NotImplementedError())
    }
  }

  def handleRequestWithWorkflowState(
      request: TexeraWebSocketRequest,
      session: Session,
      uIdOpt: Option[UInteger]
  ): Future[Any] = {
    val sessionState = SessionStateManager.getState(session.getId)
    val workflowState = sessionState.getCurrentWorkflowState.get

    request match {

      case heartbeat: HeartBeatRequest =>
        Future(HeartBeatResponse())
      case execute: WorkflowExecuteRequest =>
        workflowState
          .initExecutionState(execute, uIdOpt)
          .flatMap(workflowJobService => workflowJobService.startWorkflow())
          .handle {
            case x: ConstraintViolationException =>
              WorkflowErrorEvent(operatorErrors = x.violations)
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
        workflowState.getJobService().exportResult(uIdOpt.get, resultExportRequest)

      case cacheStatusUpdateRequest: CacheStatusUpdateRequest =>
        if (WorkflowCacheService.isAvailable) {
          workflowState.operatorCache.updateCacheStatus(cacheStatusUpdateRequest)
        } else {
          unit
        }
      case pythonExpressionEvaluateRequest: PythonExpressionEvaluateRequest =>
        workflowState
          .getJobService()
          .workflowRuntimeService
          .evaluatePythonExpression(pythonExpressionEvaluateRequest)
      case _ => Future.exception(new NotImplementedError())
    }
  }

  private def send(session: Session, msg: TexeraWebSocketEvent): Unit = {
    session.getAsyncRemote.sendText(objectMapper.writeValueAsString(msg))
  }

}
