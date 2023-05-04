package edu.uci.ics.texera.web.service

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot.ProcessingStats
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
import io.reactivex.rxjava3.disposables.Disposable

import java.io.{FileOutputStream, ObjectOutputStream}
import java.nio.file.Paths
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.DurationInt


class WorkflowReplayManager(client:AmberClient, stateStore: JobStateStore) extends SubscriptionManager {
  private var replayId = 0

  var startTime:Long = 0
  var pauseStart:Long = 0
  var history = new ProcessingHistory()
  var planner:ReplayCheckpointPlanner = _
  val pendingReplay = new mutable.HashSet[ActorVirtualIdentity]()
  var replayStart = 0L
  var checkpointCost = 0L

  private val estimationInterval = 1.seconds
  private var estimationHandler = Cancellable.alreadyCancelled

  def setupEstimation(): Cancellable ={
    TexeraWebApplication.scheduleRecurringCallThroughActorSystem(2.seconds, estimationInterval){
      doEstimation(false)
    }
  }

  def doEstimation(interaction:Boolean): Unit ={
    client.executeAsync(actor =>{
      var time = System.currentTimeMillis() - startTime
      if(history.hasSnapshotAtTime(time)) {
        time +=1
      }
      val id = CheckpointHolder.generateEstimationId(time) + (if(interaction){"-interaction"}else{""})
      history.addSnapshot(time, new LogicalExecutionSnapshot(id, interaction, time), id)
      actor.controller ! EstimateCheckpointCost(id)
    })
  }

  addSubscription(new Disposable {
    override def dispose(): Unit = estimationHandler.cancel()
    override def isDisposed: Boolean = estimationHandler.isCancelled
  })

  addSubscription(client.registerCallback[EstimationCompleted](cmd =>{
    val snapshot = history.getSnapshot(cmd.id)
    println(cmd.actorId)
    println(cmd.checkpointStats.inputWatermarks)
    println(cmd.checkpointStats.outputWatermarks)
    snapshot.addParticipant(cmd.actorId, cmd.checkpointStats)
  }))

  addSubscription(client.registerCallback[RuntimeCheckpointCompleted](cmd =>{
    val actor = cmd.actorId
    val checkpointStats = cmd.checkpointStats
    if(!CheckpointHolder.hasCheckpoint(actor, checkpointStats.step)){
      CheckpointHolder.addCheckpoint(actor, checkpointStats.step, cmd.checkpointId, null)
    }
    checkpointCost += cmd.checkpointStats.alignmentCost + cmd.checkpointStats.saveStateCost
    val snapshot = history.getSnapshot(cmd.logicalSnapshotId)
    snapshot.addParticipant(actor, checkpointStats, true)
    val time = history.getSnapshotTime(cmd.logicalSnapshotId)
    stateStore.jobMetadataStore.updateState(state => {
      state.withCheckpointedStates(state.checkpointedStates + (time.toInt -> snapshot.isAllCheckpointed))
    })
  }))

  addSubscription(client.registerCallback[ReplayCompleted](cmd =>{
    pendingReplay.remove(cmd.actorId)
    if(pendingReplay.isEmpty) {
      // replay completed
      println("replay completed! Replay planner can continue to next step")
      stateStore.jobMetadataStore.updateState(state => {
        state.withIsRecovering(false).withIsReplaying(false)
          .withReplayElapsed((System.currentTimeMillis() - replayStart)/1000d)
          .withCheckpointElapsed(checkpointCost / 1000d)
      })
    }
  }))

  addSubscription(client.registerCallback[WorkflowStateUpdate](evt => {
    evt.aggState match {
      case WorkflowAggregatedState.RUNNING =>
        if(startTime == 0){
          startTime = System.currentTimeMillis()
          TexeraWebApplication.scheduleCallThroughActorSystem(1.seconds) {
            client.executeAsync(actor => {
              val time = System.currentTimeMillis() - startTime
              val id = CheckpointHolder.generateCheckpointId
              history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
              actor.controller ! TakeRuntimeGlobalCheckpoint(id, Map.empty)
            })
          }
        }else if(pauseStart != 0){
          startTime += System.currentTimeMillis() - pauseStart
          pauseStart = 0
        }
        if(estimationHandler.isCancelled && replayStart == 0L){
          estimationHandler = setupEstimation()
        }
      case WorkflowAggregatedState.PAUSED =>
        pauseStart = System.currentTimeMillis()
        estimationHandler.cancel()
        if(replayStart == 0L){
          doEstimation(true)
        }
      case WorkflowAggregatedState.COMPLETED =>
        estimationHandler.cancel()
        stateStore.jobMetadataStore.updateState(jobMetadata =>
          jobMetadata.withInteractionHistory(history.getInteractionTimes.map(_.toInt)).withCurrentReplayPos(-1)
        )
        val file = Paths.get("").resolve("latest-interation-history")
        val oos = new ObjectOutputStream(new FileOutputStream(file.toFile))
        oos.writeObject(history)
        oos.close()
      case other => //skip
    }
  }))

  def scheduleReplay(req:WorkflowReplayRequest): Unit = {
    checkpointCost = 0L
    replayStart = System.currentTimeMillis()
    if(planner == null){
      planner = new ReplayCheckpointPlanner(history, req.replayTimeLimit)
    }
    val replayConf = planner.generateReplayPlan(req.replayPos)
    replayId += 1
    client.executeAsync(actor => {
      replayConf.confs.keys.foreach(pendingReplay.add)
      actor.controller ! ReplayWorkflow(s"replay - $replayId", replayConf)
    })
  }

}
