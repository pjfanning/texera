package edu.uci.ics.texera.web

import edu.uci.ics.texera.Utils.objectMapper
import edu.uci.ics.texera.web.service.WorkflowService
import io.reactivex.rxjava3.disposables.Disposable

import javax.websocket.Session
import scala.collection.mutable

object SessionState {
  private val sessionIdToSessionState = new mutable.HashMap[String, SessionState]()

  def getState(sId: String): SessionState = {
    sessionIdToSessionState(sId)
  }

  def setState(sId: String, state: SessionState): Unit = {
    sessionIdToSessionState.put(sId, state)
  }

  def removeState(sId: String): Unit = {
    sessionIdToSessionState(sId).unsubscribe()
    sessionIdToSessionState(sId).comparisonUnsubscribe()
    sessionIdToSessionState.remove(sId)
  }
}

class SessionState(session: Session) {
  private var currentWorkflowState: Option[WorkflowService] = None
  private var workflowSubscription = Disposable.empty()
  private var jobSubscription = Disposable.empty()

  private var comparisonWorkflowState: Option[WorkflowService] = None
  private var comparisonWorkflowSubscription = Disposable.empty()
  private var comparisonJobSubscription = Disposable.empty()

  def getCurrentWorkflowState: Option[WorkflowService] = currentWorkflowState
  def getCurrentComparisonWorkflowState: Option[WorkflowService] = comparisonWorkflowState

  def unsubscribe(): Unit = {
    workflowSubscription.dispose()
    jobSubscription.dispose()
    if (currentWorkflowState.isDefined) {
      currentWorkflowState.get.disconnect()
      currentWorkflowState = None
    }
  }

  def comparisonUnsubscribe(): Unit = {
    comparisonWorkflowSubscription.dispose()
    comparisonJobSubscription.dispose()
    if (comparisonWorkflowState.isDefined) {
      comparisonWorkflowState.get.disconnect()
      comparisonWorkflowState = None
    }
  }

  def subscribe(workflowService: WorkflowService): Unit = {
    unsubscribe()
    currentWorkflowState = Some(workflowService)
    workflowSubscription = workflowService.connect(evt =>
      session.getAsyncRemote.sendText(objectMapper.writeValueAsString(evt))
    )
    jobSubscription = workflowService.connectToJob(evt =>
      session.getAsyncRemote.sendText(objectMapper.writeValueAsString(evt))
    )
  }

  def comparisonSubscribe(workflowService: WorkflowService, workflowServiceToCompare: WorkflowService): Unit = {
    subscribe(workflowService)
    comparisonWorkflowState = Some(workflowServiceToCompare)
    comparisonWorkflowSubscription = workflowServiceToCompare.connect(evt =>
      session.getAsyncRemote.sendText(objectMapper.writeValueAsString(evt))
    )
    comparisonJobSubscription = workflowServiceToCompare.connectToJob(evt =>
      session.getAsyncRemote.sendText(objectMapper.writeValueAsString(evt))
    )
  }
}
