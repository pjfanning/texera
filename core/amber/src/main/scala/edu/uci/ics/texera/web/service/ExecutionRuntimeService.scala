package edu.uci.ics.texera.web.service

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowPaused
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ChannelMarkerHandler.PropagateChannelMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.FaultToleranceConfig
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.common.ambermessage.{NoAlignment, RequireAlignment}
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity
import edu.uci.ics.texera.web.{SubscriptionManager, WebsocketInput}
import edu.uci.ics.texera.web.model.websocket.request.{SkipTupleRequest, WorkflowInteractionRequest, WorkflowKillRequest, WorkflowPauseRequest, WorkflowResumeRequest}
import edu.uci.ics.texera.web.storage.ExecutionStateStore
import edu.uci.ics.texera.web.storage.ExecutionStateStore.updateWorkflowState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

import java.net.URI
import java.time.Instant
import java.util.UUID

class ExecutionRuntimeService(
    physicalPlan:PhysicalPlan,
    client: AmberClient,
    stateStore: ExecutionStateStore,
    wsInput: WebsocketInput,
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
    val time = System.currentTimeMillis()
    client.sendAsync(PauseWorkflow()).map{
      ret =>
        val span = System.currentTimeMillis() - time
        println(s"FCM: workflow paused in $span ms")
    }
  }))

  // Receive Resume
  addSubscription(wsInput.subscribe((req: WorkflowResumeRequest, uidOpt) => {
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
    val time = System.currentTimeMillis()
    if(req.mode == 1){
      val channelMarkerId = ChannelMarkerIdentity(s"ECM-pause-${Instant.now()}")
      client.sendAsync(PropagateChannelMarker(
        physicalPlan.getSourceOperatorIds,
        channelMarkerId,
        RequireAlignment,
        physicalPlan,
        physicalPlan.operators.map(_.id),
        PauseWorker())).map {
        ret =>
          val span = System.currentTimeMillis() - time
          println(s"ECM: workflow paused in $span ms")
          client.sendAsync(PauseWorkflow())
      }
    }else if(req.mode == 2){
      val channelMarkerId = ChannelMarkerIdentity(s"Hybrid-pause-${Instant.now()}")
      client.sendAsync(PropagateChannelMarker(
        physicalPlan.operators.map(_.id),
        channelMarkerId,
        NoAlignment,
        physicalPlan,
        physicalPlan.operators.map(_.id),
        PauseWorker())).map {
        ret =>
          val span = System.currentTimeMillis() - time
          println(s"Hybrid: workflow paused in $span ms")
          client.sendAsync(PauseWorkflow())
      }
    }else{
      val uuid = UUID.randomUUID()
      val channelMarkerId = ChannelMarkerIdentity(s"estimate-cost-${uuid}")
      client.sendAsync(TakeGlobalCheckpoint(estimationOnly = true, channelMarkerId, new URI(s"ram:///recovery-logs/${uuid}"))).map {
        ret =>
          println(s"inflight message total size = $ret bytes")
      }
    }
  }))

}
