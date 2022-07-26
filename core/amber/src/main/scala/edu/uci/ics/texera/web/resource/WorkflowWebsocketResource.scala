package edu.uci.ics.texera.web.resource

import java.util.concurrent.atomic.AtomicInteger
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.Utils.aggregatedStateToString
import edu.uci.ics.texera.web.{ServletAwareConfigurator, SessionState}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User
import edu.uci.ics.texera.web.model.websocket.event.{OperatorStatistics, OperatorStatisticsUpdateEvent, TexeraWebSocketEvent, WebResultUpdateEvent, WorkflowErrorEvent, WorkflowStateEvent}
import edu.uci.ics.texera.web.model.websocket.request._
import edu.uci.ics.texera.web.model.websocket.response._
import edu.uci.ics.texera.web.service.JobResultService.{PaginationMode, WebPaginationUpdate, WebResultUpdate}
import edu.uci.ics.texera.web.service.{WorkflowCacheService, WorkflowService}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.workflow.common.workflow.WorkflowCompiler.ConstraintViolationException

import java.util
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
    try {
      request match {
        case wIdRequest: RegisterWIdRequest =>
          // hack to refresh frontend run button state
          send(session, WorkflowStateEvent("Uninitialized"))
          val workflowState = WorkflowService.getOrCreate(wIdRequest.wId, uidOpt)

          if (aggregatedStateToString(workflowState.status) != "Completed" || aggregatedStateToString(workflowState.status) != "Aborted") {
            println("--------------------------------------------------")
            println("update state")
            println(Utils.aggregatedStateToString(workflowState.status))
            print("--------------------------------------------------")
            send(session, WorkflowStateEvent(Utils.aggregatedStateToString(workflowState.status)))


            send(session, OperatorStatisticsUpdateEvent(Map("CSVFileScan-operator-51b96cce-0d61-4d9d-a723-73f055f29ffa"->new OperatorStatistics("Completed", 0, 100))))
            var myVar = new PaginationMode()
            var myList = List(1,2,3,4,5,6,7,8,9,10)
            send(session, WebResultUpdateEvent(Map("SimpleSink-operator-99694039-09a0-4442-9e51-0830ba816e93"->WebPaginationUpdate(myVar, 100, myList)))) //append the eId to retrieve mongo result here
          }else {
            sessionState.subscribe(workflowState)
          }

          send(session, RegisterWIdResponse("wid registered"))

        case heartbeat: HeartBeatRequest =>
          send(session, HeartBeatResponse())
        case paginationRequest: ResultPaginationRequest =>
          workflowStateOpt.foreach(state =>
            send(session, state.resultService.handleResultPagination(paginationRequest))
          )
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
