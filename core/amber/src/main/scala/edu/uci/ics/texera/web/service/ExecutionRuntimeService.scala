package edu.uci.ics.texera.web.service

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowPaused
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.RetrieveWorkflowStateHandler.RetrieveWorkflowState
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.FaultToleranceConfig
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}
import edu.uci.ics.texera.web.model.websocket.request.{
  SkipTupleRequest,
  WorkflowInteractionRequest,
  WorkflowKillRequest,
  WorkflowPauseRequest,
  WorkflowResumeRequest
}
import edu.uci.ics.texera.web.storage.ExecutionStateStore
import edu.uci.ics.texera.web.storage.ExecutionStateStore.updateWorkflowState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._

import java.util.UUID

class ExecutionRuntimeService(
    client: AmberClient,
    stateStore: ExecutionStateStore,
    wsInput: WebsocketInput,
    breakpointService: ExecutionBreakpointService,
    reconfigurationService: ExecutionReconfigurationService,
    logConf: Option[FaultToleranceConfig]
) extends SubscriptionManager
    with LazyLogging {

  //Receive skip tuple
  addSubscription(wsInput.subscribe((req: SkipTupleRequest, uidOpt) => {
    throw new RuntimeException("skipping tuple is temporarily disabled")
  }))

  // Receive Paused from Amber
  addSubscription(client.registerCallback[WorkflowPaused]((evt: WorkflowPaused) => {
    stateStore.metadataStore.updateState(metadataStore =>
      updateWorkflowState(PAUSED, metadataStore)
    )
  }))

  // Receive Pause
  addSubscription(wsInput.subscribe((req: WorkflowPauseRequest, uidOpt) => {
    stateStore.metadataStore.updateState(metadataStore =>
      updateWorkflowState(PAUSING, metadataStore)
    )
    client.sendAsync(PauseWorkflow())
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
    breakpointService.clearTriggeredBreakpoints()
    reconfigurationService.performReconfigurationOnResume()
    stateStore.metadataStore.updateState(metadataStore =>
      updateWorkflowState(RESUMING, metadataStore)
    )
    client.sendAsyncWithCallback[Unit](
      ResumeWorkflow(),
      _ =>
        stateStore.metadataStore.updateState(metadataStore =>
          updateWorkflowState(RUNNING, metadataStore)
        )
    )
  }))

  // Receive Kill
  addSubscription(wsInput.subscribe((req: WorkflowKillRequest, uidOpt) => {
    client.shutdown()
    stateStore.statsStore.updateState(stats => stats.withEndTimeStamp(System.currentTimeMillis()))
    stateStore.metadataStore.updateState(metadataStore =>
      updateWorkflowState(KILLED, metadataStore)
    )
  }))

  // Receive Interaction
  addSubscription(wsInput.subscribe((req: WorkflowInteractionRequest, uidOpt) => {
    if (logConf.isEmpty) {
      logger.info(
        "Fault tolerance log folder is not established. Unable to take a global checkpoint."
      )
    } else if (req.toCheckpoint) {
      val checkpointId = ChannelMarkerIdentity(s"Checkpoint_${UUID.randomUUID().toString}")
      val uri = logConf.get.writeTo.resolve(checkpointId.toString)
      client.sendAsync(TakeGlobalCheckpoint(estimationOnly = false, checkpointId, uri))
    } else {
      client.sendAsync(RetrieveWorkflowState())
    }
  }))

}
