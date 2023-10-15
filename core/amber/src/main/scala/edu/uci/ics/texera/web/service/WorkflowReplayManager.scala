package edu.uci.ics.texera.web.service

import akka.actor.Cancellable
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.{LogicalExecutionSnapshot, ProcessingHistory}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager.{EstimateCheckpointCost, ReplayWorkflow, TakeRuntimeGlobalCheckpoint}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{EstimationCompleted, ReplayCompleted, RuntimeCheckpointCompleted, WorkflowStateUpdate}
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}
import edu.uci.ics.texera.web.{SubscriptionManager, TexeraWebApplication}
import edu.uci.ics.texera.web.model.websocket.request.WorkflowReplayRequest
import edu.uci.ics.texera.web.storage.JobStateStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.COMPLETED
import io.reactivex.rxjava3.disposables.Disposable

import java.io.{FileOutputStream, ObjectOutputStream}
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random


class WorkflowReplayManager(client:AmberClient, stateStore: JobStateStore, periodicalCheckpointInterval:Int) extends SubscriptionManager {
  private var replayId = 0

  var startTime:Long = 0
  var pauseStart:Long = 0
  var history = new ProcessingHistory()
  var planner:ReplayCheckpointPlanner = _
  val pendingReplay = new mutable.HashSet[ActorVirtualIdentity]()
  val pendingCheckpoint = new mutable.HashMap[ActorVirtualIdentity, mutable.HashSet[String]]()
  var replayStart = 0L
  var checkpointCost = 0L

  private val estimationInterval = 1.seconds
  private var estimationHandler = Cancellable.alreadyCancelled

  private var uniqueId = 0L
  private var currentGapIdx = 0
  //private val interactionGaps = Array(3340,3676,2214,1547,3013,2179,3456,1057,1628,4722,2868,4569,1642,2907,1267,1738,3364,3048,3224,1033,4223,2072,2712,4636,2351,4342,3511,2909,2763,1207,1526,3911,3757,1008,2634,2848,1077,3026,1897,2754,4717,3491,1822,4999,1991,4922,4380,3121,4547,4503)
  def generateCheckpointId: String = {
    uniqueId += 1
    s"global_checkpoint-$uniqueId"
  }

  def generateEstimationId(time:Long):String = {
    uniqueId += 1
    s"estimation-$time-$uniqueId"
  }


  def setupEstimation(): Cancellable ={
//    TexeraWebApplication.scheduleRecurringCallThroughActorSystem(2.seconds, estimationInterval) {
//      doEstimation(false)
//    }
    currentGapIdx = 0
    TexeraWebApplication.scheduleRecurringCallThroughActorSystem(2.seconds,estimationInterval){
      if(stateStore.jobMetadataStore.getState.state != COMPLETED){
        client.executeAsync(actor => {
        val time = System.currentTimeMillis() - startTime
        val id = generateCheckpointId
        history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
        actor.controller ! EstimateCheckpointCost(id)
      })
      }
    }
  }

  def doEstimation(interaction:Boolean): Unit ={
    client.executeAsync(actor =>{
      var time = System.currentTimeMillis() - startTime
      if(history.hasSnapshotAtTime(time)) {
        time +=1
      }
      val id = generateEstimationId(time) + (if(interaction){"-interaction"}else{""})
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
      CheckpointHolder.addCheckpoint(actor, checkpointStats.step, cmd.checkpointId, null, checkpointStats.saveStateCost)
    }
    checkpointCost += cmd.checkpointStats.saveStateCost
    val snapshot = history.getSnapshot(cmd.logicalSnapshotId)
    snapshot.addParticipant(actor, checkpointStats, true)
    stateStore.jobMetadataStore.updateState(state => {
      state.withNeedRefreshReplayState(state.needRefreshReplayState+1)
    })
    println("current pending checkpoints =",pendingCheckpoint.map(_._2.size).sum)
    if(pendingCheckpoint.map(_._2.size).sum < 10){
      println(s"details: ${pendingCheckpoint.mkString(",")}")
    }
    if(pendingCheckpoint.contains(actor)){
      pendingCheckpoint(actor).remove(cmd.checkpointId)
      if(pendingCheckpoint(actor).isEmpty){
        pendingCheckpoint.remove(actor)
      }
      checkReplayCompleted()
    }
  }))


  def checkReplayCompleted(): Unit ={
    if(pendingReplay.isEmpty && pendingCheckpoint.isEmpty) {
      // replay completed
      println("replay completed! Replay planner can continue to next step")
      stateStore.jobMetadataStore.updateState(state => {
        state.withIsRecovering(false).withIsReplaying(false)
          .withReplayElapsed((System.currentTimeMillis() - replayStart) / 1000d)
          .withCheckpointElapsed(checkpointCost / 1000d)
      })
    }
  }

  addSubscription(client.registerCallback[ReplayCompleted](cmd =>{
    pendingReplay.remove(cmd.actorId)
    checkReplayCompleted()
  }))

  addSubscription(client.registerCallback[WorkflowStateUpdate](evt => {
    evt.aggState match {
      case WorkflowAggregatedState.RUNNING =>
        if(startTime == 0){
          startTime = System.currentTimeMillis()
//          TexeraWebApplication.scheduleCallThroughActorSystem(1.seconds) {
//            client.executeAsync(actor => {
//              val time = System.currentTimeMillis() - startTime
//              val id = generateCheckpointId
//              history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
//              actor.controller ! TakeRuntimeGlobalCheckpoint(id, Map.empty)
//            })
//          }
          if(periodicalCheckpointInterval > 1){
            val interval = FiniteDuration(periodicalCheckpointInterval, TimeUnit.SECONDS)
            TexeraWebApplication.scheduleRecurringCallThroughActorSystem(interval, interval) {
              client.executeAsync(actor => {
                val time = System.currentTimeMillis() - startTime
                val id = generateCheckpointId
                history.addSnapshot(time, new LogicalExecutionSnapshot(id, false, time), id)
                actor.controller ! TakeRuntimeGlobalCheckpoint(id, Map.empty)
              })
            }
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
//        if(replayStart == 0L){
//          doEstimation(true)
//        }
      case WorkflowAggregatedState.COMPLETED =>
        estimationHandler.cancel()
        stateStore.jobMetadataStore.updateState(state => {
          state.withNeedRefreshReplayState(state.needRefreshReplayState+1)
        })
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
    val replayConf = if(req.replayPos == -1){
      planner.doPrepPhase(req.plannerStrategy)
    }else{
      planner.getReplayPlan(req.replayPos)
    }
    replayId += 1
    client.executeAsync(actor => {
      replayConf.confs.keys.foreach(pendingReplay.add)
      pendingReplay.remove(CONTROLLER)
      replayConf.confs.foreach{
        case (k,v) =>{
          if(v.checkpointConfig.nonEmpty){
            pendingCheckpoint(k) = new mutable.HashSet[String]()
            v.checkpointConfig.foreach(x => pendingCheckpoint(k).add(x.checkpointId))
          }
        }
      }
      //actor.controller ! PoisonPill
      actor.controller ! ReplayWorkflow(s"replay - $replayId", replayConf)
    })
  }

}
