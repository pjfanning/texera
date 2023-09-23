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

  private val replayPlans = mutable.HashMap[Int, Map[ActorVirtualIdentity, Int]]()


  def collectAllUpstreams(vid:ActorVirtualIdentity, upstreamMap:Map[ActorVirtualIdentity, Set[ActorVirtualIdentity]], allUpstreamFromVid:mutable.HashSet[(ActorVirtualIdentity, Int)]):Unit ={
    if(upstreamMap.contains(vid)){
      upstreamMap(vid).foreach {
        x =>
          allUpstreamFromVid.add(x, -1)
          collectAllUpstreams(x, upstreamMap, allUpstreamFromVid)
      }
    }
  }

  def findFrontier(start:Int, end:Int): Map[ActorVirtualIdentity, Int] ={
    val result = mutable.HashMap[ActorVirtualIdentity, Int]()
    val all = history.getSnapshot(end).getToplogicalOrder
    for(op <- all){
      var minCost = Long.MaxValue
      var idx = -1
      for(i <- start to end){
        if(history.getSnapshot(i).getParticipants.toSet.contains(op)){
          val cost = history.getOperatorCost(op, i, result.toMap)
          if(cost < minCost){
            minCost = cost
            idx = i
          }
        }
      }
      result(op) = idx
    }
    result(CONTROLLER) = end
    result.toMap
  }

  def findFrontierExhaustive(start: Int, end: Int): Map[ActorVirtualIdentity, Int] = {
    val all = history.getSnapshot(end).getParticipants

    def generateCombinations(ops: List[ActorVirtualIdentity]): List[Map[ActorVirtualIdentity, Int]] = ops match {
      case Nil => List(Map())
      case op :: rest =>
        for {
          idx <- (start to end).toList
          if history.getSnapshot(idx).getParticipants.toSet.contains(op)
          restCombination <- generateCombinations(rest)
        } yield {
          restCombination + (op -> idx)
        }
    }

    val allCombinations = generateCombinations(all.toList)
    val optimalAssignment = allCombinations.minBy(history.getPlanCost)

    optimalAssignment
  }

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

//  def findBestPlan(tau:Long): Map[String, Map[ActorVirtualIdentity, Int]] = {
//    val mergedPlan: mutable.HashMap[ActorVirtualIdentity, mutable.ArrayBuffer[Int]] = mutable.HashMap()
//    val plans = mutable.HashMap[String, Map[ActorVirtualIdentity, Int]]()
//    history.getInteractionIdxes.reverse.foreach{
//      idx =>
//        replayPlans(idx) = mutable.HashMap()
//        val end = idx
//        var start = end
//        while(start > 0 && history.getTimeGap(start-1,end) <= tau){
//          start -= 1
//        }
//        val snapshot = history.getSnapshot(end)
//        val topo = snapshot.getToplogicalOrder
//        val plan = mutable.HashMap[ActorVirtualIdentity, Int]()
//        plan(CONTROLLER) = idx
//        replayPlans(idx)(CONTROLLER) = idx
//        topo.foreach{
//          op =>
//            if(!mergedPlan.contains(op)){
//              mergedPlan(op) = mutable.ArrayBuffer()
//            }
//            if(!mergedPlan(op).exists(x => x <= idx)){
//              if(snapshot.getUpstreams(op).nonEmpty){
//                start = snapshot.getUpstreams(op).map(upstream => mergedPlan(upstream).min).min
//              }
//              var p = start
//              var minCost = Long.MaxValue
//              for(i <- start to end){
//                val prevSnapshot = history.getSnapshot(i)
//                if(prevSnapshot.getParticipants.toSet.contains(op)){
//                  val currentCost = history.getSnapshot(i).getCheckpointCost(op)
//                  if(currentCost < minCost){
//                    minCost = currentCost
//                    p = i
//                  }
//                }
//              }
//              mergedPlan(op).append(p)
//              plan(op) = p
//              replayPlans(idx)(op) = p
//            }else{
//              replayPlans(idx)(op) = mergedPlan(op).find(x => x <= idx).get
//            }
//        }
//        if(plan.nonEmpty){
//          plans(s"replay-checkpoint-${idx}") = plan.toMap
//        }
//    }
//    plans.toMap
//  }

  def findBestPlan(start: Int, end: Int, timeLimit: Long, globalOnly:Boolean, searchAll:Boolean): Map[String, Map[ActorVirtualIdentity, Int]] = {
    val n = end - start
    val dp = Array.fill(n)(Map.empty[String, (Map[ActorVirtualIdentity, Int], (Int, Int))])
    var count = 0
    val interactions = history.getInteractionIdxes.toSet
    for (i <- 0 until n) {
      var res = Long.MaxValue
      var need_to_chkpt = false
      val endPos = start + i
      if (interactions.contains(endPos)) {
        for (k <- 0 to i) {
          if (history.getTimeGap(start+k, endPos) <= timeLimit) {
            var upper = start + k
            if(!globalOnly){
              while (upper > 1 && history.getTimeGap(upper - 1, endPos) <= timeLimit) {
                upper -= 1
              }
            }
            val chkptPos = if(!searchAll){
              findFrontier(upper, start + k)
            }else{
              findFrontierExhaustive(upper, start + k)
            }
            if (need_to_chkpt) {
              count+=1
              val sub_res: Map[String, (Map[ActorVirtualIdentity, Int], (Int, Int))] = if (k > 0) {
                dp(k - 1) ++ Map(s"replay-checkpoint-$count" -> (chkptPos, (k, i)))
              } else {
                Map(s"replay-checkpoint-$count" -> (chkptPos, (k, i)))
              }
              val min_cost = sub_res.values.map(x => history.getPlanCost(x._1)).sum
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
    dp(n - 1).map{
      x =>
        val st = x._2._2
        for(p <- st._1 to st._2){
          replayPlans(p) = x._2._1
        }
        x._1 -> x._2._1
    }
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
              snapshot2.getStats(identity).inputStatus.keys.toSet.foreach {
                channel: ChannelEndpointID => if (channel.endpointWorker == upstream) {
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
    val checkpointPlan = findBestPlan(0,history.historyArray.length,timeLimit,plannerStrategy=="global", false)
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
