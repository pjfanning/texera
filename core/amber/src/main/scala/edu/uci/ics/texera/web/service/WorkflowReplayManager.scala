package edu.uci.ics.texera.web.service

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.{LogicalExecutionSnapshot, ProcessingHistory}
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{EstimateCheckpointCost, ReplayWorkflow, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, ReplayConfig}
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStateUpdate}
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT
import edu.uci.ics.texera.web.{SubscriptionManager, TexeraWebApplication}
import edu.uci.ics.texera.web.model.websocket.request.WorkflowReplayRequest
import edu.uci.ics.texera.web.storage.JobStateStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState

import java.io.{FileOutputStream, ObjectOutputStream}
import java.nio.file.Paths
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt


class WorkflowReplayManager(client:AmberClient, stateStore: JobStateStore) extends SubscriptionManager {
  private var currentIdx = 999
  private var replayId = 0

  var startTime:Long = 0
  var history = new ProcessingHistory()
  val planner = new ReplayCheckpointPlanner(history)
  val pendingReplay = new mutable.HashSet[ActorVirtualIdentity]()
  var replayStart = 0L

  private val checkpointInterval = 1000.seconds
  private val estimationInterval = 1.seconds
  private val interactionEveryNEstimation = 4
  private var interactionCounter = 0

  private var estimationHandler = Cancellable.alreadyCancelled

  private var checkpointHandler = Cancellable.alreadyCancelled

  def setupCheckpoint(): Cancellable = {
    TexeraWebApplication.scheduleRecurringCallThroughActorSystem(1.seconds, checkpointInterval) {
      client.execute(actor => {
        val time = System.currentTimeMillis() - startTime
        val id = CheckpointHolder.generateCheckpointId
        history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
        actor.controller ! TakeRuntimeGlobalCheckpoint(id, Map.empty)
      })
    }
  }

  def setupEstimation(): Cancellable ={
    TexeraWebApplication.scheduleRecurringCallThroughActorSystem(2.seconds, estimationInterval){
      client.execute(actor =>{
        interactionCounter += 1
        val time = System.currentTimeMillis() - startTime
        val id = CheckpointHolder.generateEstimationId(time)
        if(!history.hasSnapshotAtTime(time)){
          history.addSnapshot(time, new LogicalExecutionSnapshot(id, interactionCounter % interactionEveryNEstimation == 0, time), id)
        }
        actor.controller ! EstimateCheckpointCost(id)
      })
    }
  }

  addSubscription(client.registerCallback[EstimationCompleted](cmd =>{
    history.getSnapshot(cmd.id).addParticipant(cmd.actorId, cmd.checkpointStats)
  }))

  addSubscription(client.registerCallback[RuntimeCheckpointCompleted](cmd =>{
    val actor = cmd.actorId
    val checkpointStats = cmd.checkpointStats
    if(!CheckpointHolder.hasCheckpoint(actor, checkpointStats.step)){
      CheckpointHolder.addCheckpoint(actor, checkpointStats.step, null)
    }
    history.getSnapshot(cmd.id).addParticipant(actor, checkpointStats, true)
  }))

  addSubscription(client.registerCallback[ReplayCompleted](cmd =>{
    pendingReplay.remove(cmd.actorId)
    if(pendingReplay.isEmpty) {
      // replay completed
      println("replay completed! Replay planner can continue to next step")
      stateStore.jobMetadataStore.updateState(state => {
        state.withIsRecovering(false).withIsReplaying(false)
          .withReplayElapsed((System.currentTimeMillis() - replayStart)/1000d)
          .withCheckpointElapsed(0)
      })
    }
  }))

  addSubscription(client.registerCallback[WorkflowStateUpdate](evt => {
    evt.aggState match {
      case WorkflowAggregatedState.RUNNING =>
        if(startTime == 0){
          startTime = System.currentTimeMillis()
        }
        if(estimationHandler.isCancelled){
          estimationHandler = setupEstimation()
        }
        if(checkpointHandler.isCancelled){
          checkpointHandler = setupCheckpoint()
        }
      case WorkflowAggregatedState.PAUSED =>
        estimationHandler.cancel()
        checkpointHandler.cancel()
      case WorkflowAggregatedState.COMPLETED =>
        estimationHandler.cancel()
        checkpointHandler.cancel()
        stateStore.jobMetadataStore.updateState(jobMetadata =>
          jobMetadata.withInteractionHistory(history.getInteractionTimesAsSeconds).withCurrentReplayPos(-1)
        )
        val file = Paths.get("").resolve("latest-interation-history")
        val oos = new ObjectOutputStream(new FileOutputStream(file.toFile))
        oos.writeObject(history)
        oos.close()
      case other => //skip
    }
  }))

  def scheduleReplay(req:WorkflowReplayRequest): Unit = {
    println(s"replayTo: ${req.replayPos}")
    val estIdx = history.getInteractionIdxes(req.replayPos)
    replayStart = System.currentTimeMillis()
    val current =
      if (currentIdx < history.getSnapshots.size) {
        history.getSnapshot(estIdx)
      } else {
        null
      }
    val snapshot = history.getSnapshot(estIdx)
    val mem = mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]()
    val chkptPlan = planner.getReplayPlan(req.replayPos+1, 2000, mem)
    val converted = planner.getConvertedPlan(chkptPlan)
    val replayConf = WorkflowReplayConfig(snapshot.getParticipants.map {
      worker =>
        val replayTo = snapshot.getStats(worker).alignment
        val chkpt = CheckpointHolder.findLastCheckpointOf(worker, snapshot.getStats(worker).alignment)
        if (current == null || (chkpt.isDefined && chkpt.get > current.getStats(worker).alignment)) {
          worker -> ReplayConfig(chkpt, Some(replayTo), converted.getOrElse(worker, ArrayBuffer[ReplayCheckpointConfig]()).toArray)
        } else {
          worker -> ReplayConfig(None, Some(replayTo), converted.getOrElse(worker, ArrayBuffer[ReplayCheckpointConfig]()).toArray)
        }
    }.toMap - CLIENT) // client is always ready
    replayId += 1
    client.execute(actor => {
      replayConf.confs.keys.foreach(pendingReplay.add)
      actor.controller ! ReplayWorkflow(s"replay - $replayId", replayConf)
    })
  }
//
//  def mergeChoices(choices:Iterable[Int]): Map[Int, Set[ActorVirtualIdentity]] ={
//    if(choices.nonEmpty){choices.map(completeCheckpointToPartialRepr).reduce(_ ++ _)}else{Map()}
//  }
//





}
