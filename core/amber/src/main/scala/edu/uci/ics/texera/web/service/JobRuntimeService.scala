package edu.uci.ics.texera.web.service

import com.twitter.util.{Await, Duration}
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  AdditionalOperatorInfo,
  WorkflowPaused,
  WorkflowRecoveryStatus,
  WorkflowReplayInfo
}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowStateRestoreConfig
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.EvaluatePythonExpressionHandler.EvaluatePythonExpression
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}
import edu.uci.ics.texera.web.model.websocket.event.{
  TexeraWebSocketEvent,
  WorkflowAdditionalOperatorInfoEvent,
  WorkflowCheckpointedEvent,
  WorkflowExecutionErrorEvent,
  WorkflowInteractionHistoryEvent,
  WorkflowStateEvent
}
import edu.uci.ics.texera.web.model.websocket.request.{
  RemoveBreakpointRequest,
  SkipTupleRequest,
  WorkflowAdditionalOperatorInfoRequest,
  WorkflowCheckpointRequest,
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

  var planner: ReplayPlanner = _

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
      if (newState.operatorInfoStr != oldState.operatorInfoStr) {
        outputEvts.append(WorkflowAdditionalOperatorInfoEvent(newState.operatorInfoStr))
      }
      if (newState.checkpointedStates != oldState.checkpointedStates) {
        outputEvts.append(WorkflowCheckpointedEvent(newState.checkpointedStates))
      }
      outputEvts
    })
  )

  addSubscription(client.registerCallback[WorkflowReplayInfo]((evt: WorkflowReplayInfo) => {
    if (planner == null) {
      planner = new ReplayPlanner(evt.history)
      stateStore.jobMetadataStore.updateState(jobMetadata =>
        jobMetadata.withInteractionHistory(evt.history.map(_._1)).withCurrentReplayPos(-1)
      )
    }
  }))

  addSubscription(wsInput.subscribe((req: WorkflowReplayRequest, uidOpt) => {
    val reqPos = req.replayPos
    if (stateStore.jobMetadataStore.getState.currentReplayPos != reqPos) {
      stateStore.jobMetadataStore.updateState(state => {
        state.withCurrentReplayPos(reqPos).withIsReplaying(true)
        state.withIsRecovering(true)
      })
      planner.startPlanning(reqPos + 1)
      plannerNextStep()
    }
  }))

  def plannerNextStep(): Unit = {
    if (planner.hasNext) {
      planner.next() match {
        case ReplayPlanner.CheckpointCurrentState() =>
          client
            .takeGlobalCheckpoint()
            .onSuccess(idx => {
              if (idx != -1) {
                val res = planner.addCheckpoint(idx)
                if (res != -1) {
                  stateStore.jobMetadataStore.updateState(state => state.addCheckpointedStates(res))
                }
              }
              plannerNextStep()
            })
        case r @ ReplayPlanner.ReplayExecution(_, _, _) =>
          client.replayExecution(r)
      }
    } else {
      stateStore.jobMetadataStore.updateState(state => {
        state.withIsRecovering(false).withIsReplaying(false)
      })
    }
  }

  addSubscription(wsInput.subscribe((req: WorkflowCheckpointRequest, uidOpt) => {
    client
      .takeGlobalCheckpoint()
      .onSuccess(idx => {
        if (idx != -1) {
          val res = planner.addCheckpoint(idx)
          if (res != -1) {
            stateStore.jobMetadataStore.updateState(state => state.addCheckpointedStates(res))
          }
        }
      })
  }))

  addSubscription(
    client
      .registerCallback[WorkflowRecoveryStatus]((evt: WorkflowRecoveryStatus) => {
        if (!evt.isRecovering) {
          plannerNextStep()
        }
      })
  )

  //Receive skip tuple
  addSubscription(wsInput.subscribe((req: SkipTupleRequest, uidOpt) => {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }))

  addSubscription(wsInput.subscribe((req: WorkflowAdditionalOperatorInfoRequest, uidOpt) => {
    client.getOperatorInfo()
  }))

  // Receive Paused from Amber
  addSubscription(client.registerCallback[WorkflowPaused]((evt: WorkflowPaused) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(PAUSED))
  }))

  addSubscription(client.registerCallback[AdditionalOperatorInfo]((evt: AdditionalOperatorInfo) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withOperatorInfoStr(evt.data))
  }))

  // Receive Pause
  addSubscription(wsInput.subscribe((req: WorkflowPauseRequest, uidOpt) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(PAUSING))
    client.sendAsync(PauseWorkflow())
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
    if (stateStore.jobMetadataStore.getState.isReplaying) {
      client
        .interruptReplay()
        .onSuccess(ret => {
          stateStore.jobMetadataStore.updateState(state => state.withIsReplaying(false))
          doResume()
        })
    } else {
      doResume()
    }
  }))

  def doResume(): Unit = {
    breakpointService.clearTriggeredBreakpoints()
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RESUMING))
    client.sendAsyncWithCallback[Unit](
      ResumeWorkflow(),
      _ => stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(RUNNING))
    )
  }

  // Receive Kill
  addSubscription(wsInput.subscribe((req: WorkflowKillRequest, uidOpt) => {
    client.shutdown()
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(COMPLETED))
  }))

}
