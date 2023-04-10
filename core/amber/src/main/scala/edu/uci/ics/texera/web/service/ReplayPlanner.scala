package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.ReplayConfig
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ReplayPlanner(history: ProcessingHistory) {
  private var currentIdx = 999

  def completeCheckpointToPartialRepr(idx:Int):Map[Int,Set[ActorVirtualIdentity]] = {
    Map(idx -> history.getSnapshot(idx).getParticipants.toSet)
  }

  def scheduleReplayWithCheckpoint(interactionIdx:Int, timeLimit: Int, amberClient: AmberClient): Unit ={
    val snapshotIdx = history.getInteractionIdxes(interactionIdx)
    val mem = mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]()
    val (plan, cost) = getReplayPlan(snapshotIdx, timeLimit, mem)
    // find last checkpoint as the starting point of the replay.
    plan.foreach{
      p =>
        // make replay config for each time point
    }
    // execute new plan
  }

  def scheduleReplay(interactionIdx:Int, amberClient: AmberClient): Unit ={
    val snapshotIdx = history.getInteractionIdxes(interactionIdx)
    val current =
      if(currentIdx < history.getSnapshots.size){
        history.getSnapshot(currentIdx)
      }else{
        null
      }
    val snapshot = history.getSnapshot(snapshotIdx)
    val replayConf = WorkflowReplayConfig(snapshot.getParticipants.map{
      worker =>
        val replayTo = snapshot.getStats(worker).alignment
        val chkpt = CheckpointHolder.findLastCheckpointOf(worker, snapshot.getStats(worker).alignment)
        if(current == null || (chkpt.isDefined && chkpt.get > current.getStats(worker).alignment)){
          worker -> ReplayConfig(chkpt, Some(replayTo), Array.empty)
        }else{
          worker -> ReplayConfig(None, Some(replayTo), Array.empty)
        }
    }.toMap)
    amberClient.replayExecution(replayConf)
  }


//  def scheduleAsyncReplayCheckpoint(end:Int, timeLimit: Int): Unit ={
//    // setup a new amber client for replay
//    val amberClient = new AmberClient(null, null, null, null)
//    scheduleReplayCheckpoint(end, timeLimit, amberClient)
//    // kill this client after finish
//    amberClient.registerCallback[WorkflowRecoveryStatus]((evt: WorkflowRecoveryStatus) => {
//      amberClient.shutdown()
//    })
//  }

  def mergeChoices(choices:Iterable[Int]): Map[Int, Set[ActorVirtualIdentity]] ={
    if(choices.nonEmpty){choices.map(completeCheckpointToPartialRepr).reduce(_ ++ _)}else{Map()}
  }

  def pickInRange(start:Int, end:Int): (Map[ActorVirtualIdentity, Int], Long, Int) ={
    var currentPlan = history.getSnapshot(end).getParticipants.map(x => x -> end).toMap
    var currentCost = history.getPlanCost(currentPlan)
    var iteration = 0
    if(start == end){
      return (currentPlan, currentCost, iteration)
    }
    while(true){
      iteration += 1
      var nextCost = Long.MaxValue
      var nextPlan = currentPlan
      var currentDecision:Option[(ActorVirtualIdentity, Int)] = None
      (start until end).foreach{
        i =>
          val snapshot = history.getSnapshot(i)
          snapshot.getParticipants.foreach{
            op =>
              val plan = currentPlan.updated(op, i)
              val planCost = history.getPlanCost(plan)
              if (planCost < nextCost){
                nextCost = planCost
                nextPlan = plan
                currentDecision = Some((op, i))
              }
          }
      }
      if(nextCost < currentCost){
        currentCost = nextCost
        currentPlan = nextPlan
      }else{
        return (currentPlan, currentCost, iteration)
      }
    }
    (currentPlan, currentCost, iteration)
  }



  def getReplayPlan(end:Int, timeLimit:Int, mem:mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]): (Iterable[Map[ActorVirtualIdentity, Int]], Long) ={
    if(end == 0){
      return (Iterable.empty, 0)
    }
    if(mem.contains(end)){
      return mem(end)
    }
    val interactions = history.getInteractionIdxes.take(end)
    var last = interactions.length - 1
    val t = interactions.last
    var prevInteraction = t
    var bestPlan = interactions.map(i => history.getSnapshot(i).getParticipants.map(x => x -> i).toMap).toIterable
    var bestCost = bestPlan.map(history.getPlanCost).sum
    var currentStart = t - 1
    var timeInterval = history.getTimeGap(currentStart, t)
    while(timeInterval < timeLimit && currentStart >= 0){
      if(interactions.contains(currentStart)){
        prevInteraction = currentStart
        last = interactions.indexOf(prevInteraction)
      }
      val (plan, cost, _) = pickInRange(currentStart, prevInteraction)
      val (others, total) = getReplayPlan(last, timeLimit, mem)
      if(bestCost > total+cost){
        bestCost = total+cost
        bestPlan = others ++ Iterable(plan)
      }
      currentStart -= 1
      timeInterval = history.getTimeGap(currentStart, t)
    }
    mem(end) = (bestPlan, bestCost)
    (bestPlan, bestCost)
  }





}
