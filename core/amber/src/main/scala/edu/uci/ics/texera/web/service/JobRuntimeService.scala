package edu.uci.ics.texera.web.service

import com.twitter.util.{Await, Duration}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowPaused,
  WorkflowReplayInfo
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}
import edu.uci.ics.texera.web.model.websocket.event.{
  TexeraWebSocketEvent,
  WorkflowExecutionErrorEvent,
  WorkflowInteractionHistoryEvent,
  WorkflowStateEvent
}
import edu.uci.ics.texera.web.model.websocket.request.{
  RemoveBreakpointRequest,
  SkipTupleRequest,
  WorkflowKillRequest,
  WorkflowPauseRequest,
  WorkflowReplayRequest,
  WorkflowResumeRequest
}
import edu.uci.ics.texera.web.storage.{JobStateStore, WorkflowStateStore}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._

import scala.collection.mutable

class JobRuntimeService(
    client: AmberClient,
    stateStore: JobStateStore,
    wsInput: WebsocketInput,
    breakpointService: JobBreakpointService
) extends SubscriptionManager
    with LazyLogging {

  addSubscription(
    stateStore.jobMetadataStore.registerDiffHandler((oldState, newState) => {
      val outputEvts = new mutable.ArrayBuffer[TexeraWebSocketEvent]()
      // Update workflow state
      if (newState.state != oldState.state || newState.isRecovering != oldState.isRecovering) {
        if (WorkflowService.userSystemEnabled) {
          ExecutionsMetadataPersistService.tryUpdateExistingExecution(newState.eid, newState.state)
        }
        // Check if is recovering
        if (newState.isRecovering && newState.state != COMPLETED) {
          outputEvts.append(WorkflowStateEvent("Recovering"))
        } else {
          outputEvts.append(WorkflowStateEvent(Utils.aggregatedStateToString(newState.state)))
        }
      }
      // Check if new error occurred
      if (newState.error != oldState.error && newState.error != null) {
        outputEvts.append(WorkflowExecutionErrorEvent(newState.error))
      }
      if (newState.interactionHistory != oldState.interactionHistory) {
        outputEvts.append(WorkflowInteractionHistoryEvent(newState.interactionHistory))
      }
      outputEvts
    })
  )

  addSubscription(client.registerCallback[WorkflowReplayInfo]((evt: WorkflowReplayInfo) => {
    if (!stateStore.jobMetadataStore.getState.isReplaying) {
      stateStore.jobMetadataStore.updateState(jobMetadata =>
        jobMetadata.withInteractionHistory(evt.history)
      )
    }
  }))

  addSubscription(wsInput.subscribe((req: WorkflowReplayRequest, uidOpt) => {
    if (stateStore.jobMetadataStore.getState.currentReplayPos != req.replayPos) {
      val requireRestart =
        !stateStore.jobMetadataStore.getState.isReplaying || stateStore.jobMetadataStore.getState.currentReplayPos > req.replayPos
      stateStore.jobMetadataStore.updateState(state => {
        state.withCurrentReplayPos(req.replayPos).withIsReplaying(true)
      })
      client.replayExecution(req.replayPos, requireRestart)
    }
  }))

  //Receive skip tuple
  addSubscription(wsInput.subscribe((req: SkipTupleRequest, uidOpt) => {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }))

  // Receive Paused from Amber
  addSubscription(client.registerCallback[WorkflowPaused]((evt: WorkflowPaused) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(PAUSED))
  }))

  // Receive Pause
  addSubscription(wsInput.subscribe((req: WorkflowPauseRequest, uidOpt) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(PAUSING))
    client.sendAsync(PauseWorkflow())
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
    breakpointService.clearTriggeredBreakpoints()
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RESUMING))
    client.sendAsyncWithCallback[Unit](
      ResumeWorkflow(),
      _ => stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RUNNING))
    )
  }))

  // Receive Kill
  addSubscription(wsInput.subscribe((req: WorkflowKillRequest, uidOpt) => {
    client.shutdown()
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(COMPLETED))
  }))

}
