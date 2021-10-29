package edu.uci.ics.texera.web

import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent
import edu.uci.ics.texera.web.service.WorkflowService
import rx.lang.scala.subscriptions.CompositeSubscription
import rx.lang.scala.{Observer, Subscription}

import javax.websocket.Session

class SessionState(session: Session) {
  private val observer: Observer[TexeraWebSocketEvent] = new WebsocketSubscriber(session)
  private var operatorCacheSubscription: Subscription = Subscription()
  private var jobSubscription: Subscription = Subscription()
  private var currentWorkflowState: Option[WorkflowService] = None

  def getCurrentWorkflowState: Option[WorkflowService] = currentWorkflowState

  def subscribe(workflowService: WorkflowService): Unit = {
    unsubscribe()
    currentWorkflowState = Some(workflowService)
    workflowService.connect()
    operatorCacheSubscription = workflowService.operatorCache.subscribe(observer)
    workflowService.getJobServiceObservable.subscribe(jobService => {
      jobSubscription.unsubscribe()
      jobSubscription = CompositeSubscription(
        jobService.subscribeRuntimeComponents(observer),
        jobService.subscribe(observer)
      )
    })
  }

  def unsubscribe(): Unit = {
    operatorCacheSubscription.unsubscribe()
    jobSubscription.unsubscribe()
    if (currentWorkflowState.isDefined) {
      currentWorkflowState.get.disconnect()
      currentWorkflowState = None
    }
  }
}
