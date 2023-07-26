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

  def getGlobalPlan(start:Int, end:Int, timeLimit:Long): Set[Int] ={
    val n = end - start
    val dp = Array.fill(n)(Set.empty[Int])
    val interactions = history.getInteractionIdxes.toSet
    for (i <- 0 until n) {
      var res = Long.MaxValue
      var need_to_chkpt = false
      val endPos = start + i
      if(interactions.contains(endPos)){
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
      }else if(i > 0){
        dp(i) = dp(i-1)
      }
    }
    dp(n - 1)
  }

  def toPartialPlan(globalPlan:Set[Int]): Map[String, Map[ActorVirtualIdentity, Int]] ={
    if(globalPlan.isEmpty){
      return Map.empty
    }
    var numChkpt = 0
    globalPlan.map{
      idx =>
        numChkpt += 1
        s"replay-checkpoint-$numChkpt" -> history.getSnapshot(idx).getParticipants.map(x => x -> idx).toMap
    }.toMap
  }

  def generatePartialPlan(targetTime:Long): Map[String, Map[ActorVirtualIdentity, Int]] ={
    val plannedCheckpoint = mutable.HashSet[(ActorVirtualIdentity, Int)]()
    var numChkpt = 0
    history.getInteractionIdxes.reverse.map{
      x =>
        val replayPlan = mutable.HashMap[ActorVirtualIdentity, Int]()
        history.getSnapshot(x).getParticipants.foreach{
          operator =>
            if(operator != CONTROLLER && operator != CLIENT){
              if (operator.name.contains("ee76-main-0") && x == 19) {
                println("hello")
              }
              val p = findBestPlan(operator, x, targetTime)
              if(operator.name.contains("ee76-main-0") && x==19) {
                println(p)
              }
              p.foreach{
                case (id, i) =>
                  if(replayPlan.contains(id)){
                    replayPlan(id) = Math.min(replayPlan(id), i)
                  }else{
                    replayPlan(id) = i
                  }
              }
            }
        }
        replayPlan ++= Set((CONTROLLER, x))
        replayPlans(x) = replayPlan
        var additional = (replayPlan.toSeq.toSet diff plannedCheckpoint).filter(_._2 > 0)
        plannedCheckpoint ++= additional
        additional.toMap
    }.map{ x =>
      numChkpt += 1
      s"replay-checkpoint-$numChkpt" -> x
    }.toMap
  }


  def findBestPlan(target:ActorVirtualIdentity, snapshot:Int, budget:Long): Set[(ActorVirtualIdentity, Int)] ={
      val plans:mutable.ArrayBuffer[Set[(ActorVirtualIdentity, Int)]] = mutable.ArrayBuffer()
      for(i <- 0 to snapshot){
        val curBudget = budget - history.getTimeGap(i, snapshot)
        var currentPlan:Set[(ActorVirtualIdentity, Int)] = Set((target, i))
        if(curBudget >= 0){
          history.getSnapshot(i).getUpstreams(target).foreach {
            vid =>
              if (vid != CONTROLLER && vid != CLIENT) {
                currentPlan ++= findBestPlan(vid, i, curBudget)
              }
          }
          plans.append(currentPlan)
      }
    }
    plans.minBy(history.getPlanCost)
  }


  def getReplayConfig(checkpointId:String, checkpointPlan:Map[ActorVirtualIdentity, Int]): Map[ActorVirtualIdentity, ReplayCheckpointConfig] ={
    checkpointPlan.map{
      case (identity, i) =>
        val snapshot = history.getSnapshot(i)
        val snapshotStats = snapshot.getStats(identity)
        val markerCollection = mutable.HashSet[ChannelEndpointID]()
        checkpointPlan.foreach{
          case (upstream, chkptPos) =>
            val snapshot2 = history.getSnapshot(chkptPos)
            if(snapshot2.getParticipants.toSet.contains(identity)){
              snapshot2.getStats(identity).inputStatus.keys.toSet.foreach{
                channel:ChannelEndpointID => if(channel.endpointWorker == upstream){markerCollection.add(channel)}
              }
            }
        }
        markerCollection.remove(OutsideWorldChannelEndpointID) // outside world marker cannot be collected
        identity -> ReplayCheckpointConfig(checkpointId, markerCollection.toSet, snapshotStats.alignment, snapshot.id)
    }
  }


  def doPrepPhase(): WorkflowReplayConfig ={
    val interactionIdx = history.getInteractionIdxes.last
    val checkpointPlan = generatePartialPlan(timeLimit)
    val confs = checkpointPlan.map{
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


  def getReplayPlan(targetTime:Long):WorkflowReplayConfig = {
    val interactionIdx = history.getInteractionIdx(targetTime)
    val plan = replayPlans(interactionIdx)
    val snapshot = history.getSnapshot(interactionIdx)
    snapshot.getParticipants.filter(x => !plan.contains(x)).foreach{
      worker =>
        println(s"$worker is not included in the plan $interactionIdx! isCheckpointed = ${snapshot.isCheckpointed(worker)} stats = ${snapshot.getStats(worker)} ")
    }
    WorkflowReplayConfig(snapshot.getParticipants.filter(plan.contains).map {
      worker =>
        val checkpointIdx = plan(worker)
        val checkpointSnapshot =  history.getSnapshot(checkpointIdx)
        val loadAlignment = checkpointSnapshot.getStats(worker).alignment
          worker -> ReplayConfig(Some(loadAlignment), Some(snapshot.getStats(worker).alignment), Array[ReplayCheckpointConfig]())
    }.toMap - CLIENT) // client is always ready
  }

}
