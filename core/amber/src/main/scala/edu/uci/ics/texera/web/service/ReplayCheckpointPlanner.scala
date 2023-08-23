package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, ReplayConfig}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, OutsideWorldChannelEndpointID}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER}

import scala.collection.mutable

class ReplayCheckpointPlanner(history:ProcessingHistory, timeLimit:Long) {

  private val replayPlans = mutable.HashMap[Int, mutable.HashMap[ActorVirtualIdentity, Int]]()

  def getGlobalPlan(start: Int, end: Int, timeLimit: Long): Set[Int] = {
    val n = end - start
    val dp = Array.fill(n)(Set.empty[Int])
    val interactions = history.getInteractionIdxes.toSet
    for (i <- 0 until n) {
      var res = Long.MaxValue
      var need_to_chkpt = false
      val endPos = start + i
      if (interactions.contains(endPos)) {
        for (k <- 0 to i) {
          val chkptPos = start + k
          if (history.getTimeGap(chkptPos, endPos) <= timeLimit) {
            if (need_to_chkpt) {
              val sub_res = if (k > 0) dp(k - 1) + k else Set(k)
              val min_cost = sub_res.map(x => history.getPlanCost(x)).sum
              if (res >= min_cost) {
                res = min_cost
                dp(i) = sub_res
              }
            }
          } else {
            need_to_chkpt = true
          }
        }
      } else if (i > 0) {
        dp(i) = dp(i - 1)
      }
    }
    dp(n - 1)
  }

  def toPartialPlan(globalPlan: Set[Int]): Map[String, Map[ActorVirtualIdentity, Int]] = {
    if (globalPlan.isEmpty) {
      return Map.empty
    }
    var numChkpt = 0
    globalPlan.map {
      idx =>
        numChkpt += 1
        s"replay-checkpoint-$numChkpt" -> history.getSnapshot(idx).getParticipants.map(x => x -> idx).toMap
    }.toMap
  }


  def collectAllUpstreams(vid:ActorVirtualIdentity, upstreamMap:Map[ActorVirtualIdentity, Set[ActorVirtualIdentity]], allUpstreamFromVid:mutable.HashSet[(ActorVirtualIdentity, Int)]):Unit ={
    if(upstreamMap.contains(vid)){
      upstreamMap(vid).foreach {
        x =>
          allUpstreamFromVid.add(x, -1)
          collectAllUpstreams(x, upstreamMap, allUpstreamFromVid)
      }
    }
  }


  def findBestPlan(tau:Long): Map[String, Map[ActorVirtualIdentity, Int]] = {
    val mergedPlan: mutable.HashMap[ActorVirtualIdentity, mutable.ArrayBuffer[Int]] = mutable.HashMap()
    val plans = mutable.HashMap[String, Map[ActorVirtualIdentity, Int]]()
    history.getInteractionIdxes.reverse.foreach{
      idx =>
        replayPlans(idx) = mutable.HashMap()
        val end = idx
        var start = end
        while(start > 0 && history.getTimeGap(start-1,end) <= tau){
          start -= 1
        }
        val snapshot = history.getSnapshot(end)
        val topo = snapshot.getToplogicalOrder
        val plan = mutable.HashMap[ActorVirtualIdentity, Int]()
        plan(CONTROLLER) = idx
        replayPlans(idx)(CONTROLLER) = idx
        topo.foreach{
          op =>
            if(!mergedPlan.contains(op)){
              mergedPlan(op) = mutable.ArrayBuffer()
            }
            if(!mergedPlan(op).exists(x => x <= idx)){
              if(snapshot.getUpstreams(op).nonEmpty){
                start = snapshot.getUpstreams(op).map(upstream => mergedPlan(upstream).min).min
              }
              var p = start
              var minCost = Long.MaxValue
              for(i <- start to end){
                val prevSnapshot = history.getSnapshot(i)
                if(prevSnapshot.getParticipants.toSet.contains(op)){
                  val currentCost = history.getSnapshot(i).getCheckpointCost(op)
                  if(currentCost < minCost){
                    minCost = currentCost
                    p = i
                  }
                }
              }
              mergedPlan(op).append(p)
              plan(op) = p
              replayPlans(idx)(op) = p
            }else{
              replayPlans(idx)(op) = mergedPlan(op).find(x => x <= idx).get
            }
        }
        if(plan.nonEmpty){
          plans(s"replay-checkpoint-${idx}") = plan.toMap
        }
    }
    plans.toMap
  }


  def getReplayConfig(checkpointId: String, checkpointPlan: Map[ActorVirtualIdentity, Int]): Map[ActorVirtualIdentity, ReplayCheckpointConfig] = {
    checkpointPlan.map {
      case (identity, i) =>
        val snapshot = history.getSnapshot(i)
        val snapshotStats = snapshot.getStats(identity)
        val markerCollection = mutable.HashSet[ChannelEndpointID]()
        checkpointPlan.foreach {
          case (upstream, chkptPos) =>
            val snapshot2 = history.getSnapshot(chkptPos)
            if (snapshot2.getParticipants.toSet.contains(identity)) {
              val stats = snapshot2.getStats(identity)
              stats.inputStatus.keys.toSet.foreach {
                channel: ChannelEndpointID =>
                  val upstreamStats = stats.inputStatus.get(channel)
                  val currentStats = snapshotStats.inputStatus.get(channel)
                  if (channel.endpointWorker == upstream && currentStats.actualReceived < upstreamStats.toReceive) {
                  markerCollection.add(channel)
                }
              }
            }
        }
        markerCollection.remove(OutsideWorldChannelEndpointID) // outside world marker cannot be collected
        identity -> ReplayCheckpointConfig(checkpointId, markerCollection.toSet, snapshotStats.alignment, snapshot.id)
    }
  }


  def doPrepPhase(plannerStrategy:String): WorkflowReplayConfig = {
    val interactionIdx = history.getInteractionIdxes.last
    val checkpointPlan = if(plannerStrategy!="global"){
      findBestPlan(timeLimit)
    }else{
      toPartialPlan(getGlobalPlan(0,interactionIdx, timeLimit))
    }
    val confs = checkpointPlan.map {
      case (name, mapping) =>
        getReplayConfig(name, mapping).toSeq
    }
    val converted = confs.flatten.groupBy(_._1).mapValues(_.map(_._2))
    val targetSnapshot = history.getSnapshot(interactionIdx)
    WorkflowReplayConfig(targetSnapshot.getParticipants.map {
      worker =>
        val workerStats = targetSnapshot.getStats(worker)
        val replayTo = workerStats.alignment
        val checkpointOpt = CheckpointHolder.getCheckpointAlignment(worker, "global_checkpoint-1")
        if (checkpointOpt.isDefined) {
          worker -> ReplayConfig(Some(checkpointOpt.get), Some(replayTo), converted.getOrElse(worker, mutable.ArrayBuffer[ReplayCheckpointConfig]()).toArray)
        } else {
          worker -> ReplayConfig(None, Some(replayTo), converted.getOrElse(worker, mutable.ArrayBuffer[ReplayCheckpointConfig]()).toArray)
        }
    }.toMap - CLIENT) // client is always ready
  }


  def getReplayPlan(targetTime: Long): WorkflowReplayConfig = {
    val interactionIdx = history.getInteractionIdx(targetTime)
    val plan = replayPlans(interactionIdx)
    val snapshot = history.getSnapshot(interactionIdx)
    snapshot.getParticipants.filter(x => !plan.contains(x)).foreach {
      worker =>
        println(s"$worker is not included in the plan $interactionIdx! isCheckpointed = ${snapshot.isCheckpointed(worker)} stats = ${snapshot.getStats(worker)} ")
    }
    WorkflowReplayConfig(snapshot.getParticipants.filter(plan.contains).map {
      worker =>
        val checkpointIdx = plan(worker)
        if (checkpointIdx == -1) {
          worker -> ReplayConfig(None, Some(snapshot.getStats(worker).alignment), Array[ReplayCheckpointConfig]())
        } else {
          val checkpointSnapshot = history.getSnapshot(checkpointIdx)
          val loadAlignment = checkpointSnapshot.getStats(worker).alignment
          worker -> ReplayConfig(Some(loadAlignment), Some(snapshot.getStats(worker).alignment), Array[ReplayCheckpointConfig]())
        }
    }.toMap - CLIENT) // client is always ready
  }

}
