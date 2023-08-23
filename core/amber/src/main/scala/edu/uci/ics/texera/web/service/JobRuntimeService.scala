package edu.uci.ics.texera.web.service

import com.twitter.util.{Await, Duration}
import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.util.Timing.startTime
import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.WorkflowRecoveryStatus
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{EstimateCheckpointCost, ReplayWorkflow, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.texera.Utils
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}
import edu.uci.ics.texera.web.model.websocket.event.{TexeraWebSocketEvent, WorkflowAdditionalOperatorInfoEvent, WorkflowCheckpointedEvent, WorkflowExecutionErrorEvent, WorkflowInteractionHistoryEvent, WorkflowReplayCompletedEvent, WorkflowStateEvent}
import edu.uci.ics.texera.web.model.websocket.request.{RemoveBreakpointRequest, SkipTupleRequest, WorkflowAdditionalOperatorInfoRequest, WorkflowCheckpointRequest, WorkflowKillRequest, WorkflowPauseRequest, WorkflowReplayRequest, WorkflowResumeRequest}
import edu.uci.ics.texera.web.storage.{JobStateStore, WorkflowStateStore}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._

import scala.collection.mutable

class JobRuntimeService(
    client: AmberClient,
    stateStore: JobStateStore,
    wsInput: WebsocketInput,
    breakpointService: JobBreakpointService,
    reconfigurationService: JobReconfigurationService,
    periodicalCheckpointInterval: Int
) extends SubscriptionManager
    with LazyLogging {

  var planner: WorkflowReplayManager = new WorkflowReplayManager(client, stateStore, periodicalCheckpointInterval)

  override def unsubscribeAll(): Unit = {
    super.unsubscribeAll()
    planner.unsubscribeAll()
  }

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
      if(newState.replayElapsed != oldState.replayElapsed || newState.checkpointElapsed != oldState.checkpointElapsed){
        outputEvts.append(WorkflowReplayCompletedEvent(newState.replayElapsed, newState.checkpointElapsed))
      }
      if (newState.needRefreshReplayState != oldState.needRefreshReplayState) {
        val interactions = planner.history.getInteractionIdxes
        outputEvts.append(WorkflowInteractionHistoryEvent(planner.history.historyArray.map(_.toInt), planner.history.historyArray.indices.map(interactions.contains), planner.history.getSnapshotStatus))
      }
      if (newState.operatorInfoStr != oldState.operatorInfoStr) {
        outputEvts.append(WorkflowAdditionalOperatorInfoEvent(newState.operatorInfoStr))
      }
      outputEvts
    })
  )

  addSubscription(wsInput.subscribe((req: WorkflowReplayRequest, uidOpt) => {
    planner.scheduleReplay(req)
  }))


  //Receive skip tuple
  addSubscription(wsInput.subscribe((req: SkipTupleRequest, uidOpt) => {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }))

  // Receive Pause
  addSubscription(wsInput.subscribe((req: WorkflowPauseRequest, uidOpt) => {
    stateStore.jobMetadataStore.updateState(jobInfo => jobInfo.withState(PAUSING))
    client.sendAsync(PauseWorkflow())
    if(periodicalCheckpointInterval == 0){
      client.executeAsync(actor => {
        val time = System.currentTimeMillis() - planner.startTime
        val id = planner.generateCheckpointId
        planner.history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
        actor.controller ! TakeRuntimeGlobalCheckpoint(id, Map.empty)
      })
    }
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
    if (stateStore.jobMetadataStore.getState.isReplaying) {
//      client
//        .interruptReplay()
//        .onSuccess(ret => {
//          stateStore.jobMetadataStore.updateState(state => state.withIsReplaying(false))
//          doResume()
//        })
    } else {
      doResume()
    }
  }))

  def doResume(): Unit = {
    breakpointService.clearTriggeredBreakpoints()
    reconfigurationService.performReconfigurationOnResume()
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
