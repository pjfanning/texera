package edu.uci.ics.texera.web.resource

import java.util.concurrent.atomic.AtomicInteger
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{ServletAwareConfigurator, SessionState}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User
import edu.uci.ics.texera.web.model.websocket.event.{
  OperatorStatistics,
  OperatorStatisticsUpdateEvent,
  TexeraWebSocketEvent,
  WebResultUpdateEvent,
  WorkflowErrorEvent,
  WorkflowStateEvent
}
import edu.uci.ics.texera.web.model.websocket.request._
import edu.uci.ics.texera.web.model.websocket.response._
import edu.uci.ics.texera.web.service.{WorkflowService}
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler.ConstraintViolationException
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

  private def send(session: Session, msg: TexeraWebSocketEvent): Unit = {
    session.getAsyncRemote.sendText(objectMapper.writeValueAsString(msg))
  }

  @OnOpen
  def myOnOpen(session: Session, config: EndpointConfig): Unit = {
    SessionState.setState(session.getId, new SessionState(session))
    logger.info("connection open")
  }

  @OnClose
  def myOnClose(session: Session, cr: CloseReason): Unit = {
    SessionState.removeState(session.getId)
  }

  @OnMessage
  def myOnMsg(session: Session, message: String): Unit = {
    val request = objectMapper.readValue(message, classOf[TexeraWebSocketRequest])
    val uidOpt = session.getUserProperties.asScala
      .get(classOf[User].getName)
      .map(_.asInstanceOf[User].getUid)
    val sessionState = SessionState.getState(session.getId)
    val workflowStateOpt = sessionState.getCurrentWorkflowState
    val comparisonWorkflowStateOpt = sessionState.getCurrentComparisonWorkflowState

    try {
      request match {
        case viewExecutionRequest: RegisterEIdRequest =>
          val workflowState = WorkflowService.retrieveExecution(viewExecutionRequest)
          send(session, WorkflowStateEvent(Utils.aggregatedStateToString(workflowState.status)))
          send(
            session,OperatorStatisticsUpdateEvent(workflowState.getWorkflowStatsMessage(viewExecutionRequest.eId))
          )
          send(
            session,
            WebResultUpdateEvent(
              workflowState.getResultUpdateMessage()
            )
          )
          sessionState.subscribe(workflowState)
        case comparisonRequest: CompareEIdExecutionRequest =>
          val (workflowState, workflowState_to_compare) = WorkflowService.retrieveExecutionsToCompare(comparisonRequest)

          send(
            session,OperatorStatisticsUpdateEvent(workflowState.getComparisonWorkflowStatsMessage(comparisonRequest.eId1)++workflowState_to_compare.getComparisonWorkflowStatsMessage(comparisonRequest.eId2))
          )

          send(
            session,
            WebResultUpdateEvent(
              workflowState.getComparisonResultUpdateMessage()++workflowState_to_compare.getComparisonResultUpdateMessage()
            )
          )
          sessionState.comparisonSubscribe(workflowState, workflowState_to_compare)
        case wIdRequest: RegisterWIdRequest =>
          // hack to refresh frontend run button state
          send(session, WorkflowStateEvent("Uninitialized"))
          val workflowState = WorkflowService.getOrCreate(wIdRequest.wId, uidOpt)

          if (workflowState.status.isCompleted || workflowState.status.isAborted) {
            send(session, WorkflowStateEvent(Utils.aggregatedStateToString(workflowState.status)))

            send(
              session,OperatorStatisticsUpdateEvent(workflowState.getWorkflowStatsMessage(workflowState.executionID.toInt))
            )
            send(
              session,
              WebResultUpdateEvent(
                workflowState.getResultUpdateMessage()
              )
            )
          }
          sessionState.subscribe(workflowState)

          send(session, RegisterWIdResponse("wid registered"))

        case heartbeat: HeartBeatRequest =>
          send(session, HeartBeatResponse())
        case paginationRequest: ResultPaginationRequest =>
          workflowStateOpt.foreach(state =>
            send(session, state.resultService.handleResultPagination(paginationRequest))
          )
        case comparisonPaginationRequest: ComparisonResultPaginationRequest =>
          println("--------------------")
          println(comparisonPaginationRequest)
          val paginationRequest = ResultPaginationRequest(comparisonPaginationRequest.requestID, comparisonPaginationRequest.operatorID, comparisonPaginationRequest.pageIndex, comparisonPaginationRequest.pageSize)
          if (comparisonPaginationRequest.isTopWorkflow){
            workflowStateOpt.foreach(state =>
              send(session, state.resultService.handleResultPagination(paginationRequest))
            )
          }else{
            comparisonWorkflowStateOpt.foreach(state =>
              send(session, state.resultService.handleResultPagination(paginationRequest))
            )
          }
        case resultExportRequest: ResultExportRequest =>
          workflowStateOpt.foreach(state =>
            send(session, state.exportService.exportResult(uidOpt.get, resultExportRequest))
          )
        case other =>
          workflowStateOpt match {
            case Some(workflow) => workflow.wsInput.onNext(other, uidOpt)
            case None           => throw new IllegalStateException("workflow is not initialized")
          }
      }
    } catch {
      case x: ConstraintViolationException =>
        send(session, WorkflowErrorEvent(operatorErrors = x.violations))
      case err: Exception =>
        send(
          session,
          WorkflowErrorEvent(generalErrors =
            Map("exception" -> (err.getMessage + "\n" + err.getStackTrace.mkString("\n")))
          )
        )
        throw err
    }

  }

}
