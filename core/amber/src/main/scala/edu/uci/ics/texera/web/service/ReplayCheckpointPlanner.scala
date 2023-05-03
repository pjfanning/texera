package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class ReplayCheckpointPlanner(history:ProcessingHistory, timeLimit:Long) {

  private val checkpointMapping = mutable.HashMap[Int, String]().withDefaultValue("global_checkpoint-1")
  private val plannedCheckpoints = getPartialPlan(getGlobalPlan(0, history.historyArray.length, timeLimit))

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

  def getPartialPlan(globalPlan:Set[Int]): Map[String, Map[ActorVirtualIdentity, Int]] ={
    if(globalPlan.isEmpty){
      return Map.empty
    }
    val segs = globalPlan.toSeq.sorted
    var currentIdx = 0
    var head: Int = segs(0)
    var next: Int = if(segs.length > 1){segs(1)}else{-1}
    val interactions = history.getInteractionIdxes.filter(x => x >= head)
    var currentIdx2 = 0
    val tmp = mutable.HashMap[String, Map[ActorVirtualIdentity, Int]]()
    while(head != -1){
      val chkptId = s"replay_checkpoint-${currentIdx+1}"
      var interactionIdx = interactions(currentIdx2)
      val firstInteraction = interactionIdx
      while(interactionIdx != -1 && interactionIdx >= head && (next == -1 || interactionIdx < next)){
        assert(history.getTimeGap(head, interactionIdx) <= timeLimit)
        checkpointMapping(interactionIdx) = chkptId
        currentIdx2 += 1
        if(currentIdx2 < interactions.length){
          interactionIdx = interactions(currentIdx2)
        }else{
          interactionIdx = -1
        }
      }
      val partialPlan = pickInRange(head, firstInteraction)
      tmp(chkptId) = partialPlan._1
      head = next
      currentIdx += 1
      next =  if(segs.length > currentIdx+1){segs(currentIdx+1)}else{-1}
    }
    tmp.toMap
  }


  def generateReplayPlan(target:Int):WorkflowReplayConfig = {
    null
  }


//  def getReplayPlan(end:Int, timeLimit:Long, mem:mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]): (Iterable[Map[ActorVirtualIdentity, Int]], Long) ={
//    if(end == 0){
//      return (Iterable.empty, 0)
//    }
//    if(mem.contains(end)){
//      return mem(end)
//    }
//    val interactions = history.getInteractionIdxes.take(end)
//    var last = interactions.length - 1
//    val t = interactions.last
//    var prevInteraction = t
//    var bestPlan = interactions.map(i => history.getSnapshot(i).getParticipants.map(x => x -> i).toMap).toIterable
//    var bestCost = bestPlan.map(history.getPlanCost).sum
//    var currentStart = t - 1
//    var timeInterval = history.getTimeGap(currentStart, t)
//    while(timeInterval < timeLimit && currentStart >= 0){
//      if(interactions.contains(currentStart)){
//        prevInteraction = currentStart
//        last = interactions.indexOf(prevInteraction)
//      }
//      val (plan, cost, _) = pickInRange(currentStart, prevInteraction)
//      val (others, total) = getReplayPlan(last, timeLimit, mem)
//      if(bestCost > total+cost){
//        bestCost = total+cost
//        bestPlan = others ++ Iterable(plan)
//      }
//      currentStart -= 1
//      timeInterval = history.getTimeGap(currentStart, t)
//    }
//    mem(end) = (bestPlan, bestCost)
//    (bestPlan, bestCost)
//  }


//  def getConvertedPlan(startFrom:Map[ActorVirtualIdentity, Long]): Map[ActorVirtualIdentity, mutable.ArrayBuffer[ReplayCheckpointConfig]] ={
//    val converted = mutable.HashMap[ActorVirtualIdentity, mutable.ArrayBuffer[ReplayCheckpointConfig]]()
//    chkptPlan._1.foreach{
//      plan =>
//        var availableToCheckpoint = true
//        plan.foreach{
//          case (identity, i) =>
//            val snapshot = history.getSnapshot(i)
//            val snapshotStats = snapshot.getStats(identity)
//            val alignment = snapshotStats.alignment
//            if(alignment <= startFrom.getOrElse(identity, 0L)){
//              availableToCheckpoint = false
//            }
//        }
//        if(availableToCheckpoint){
//          replayChkptId += 1
//          plan.foreach{
//            case (identity, i) =>
//              val buffer = converted.getOrElseUpdate(identity, new ArrayBuffer[ReplayCheckpointConfig]())
//              val snapshot = history.getSnapshot(i)
//              val snapshotStats = snapshot.getStats(identity)
//              val markerCollection = mutable.HashSet[ChannelEndpointID]()
//              plan.foreach{
//                case (upstream, chkptPos) =>
//                  val snapshot2 = history.getSnapshot(chkptPos)
//                  if(snapshot2.getParticipants.toSet.contains(identity)){
//                    snapshot2.getStats(identity).inputStatus.keys.toSet.foreach{
//                      channel:ChannelEndpointID => if(channel.endpointWorker == upstream){markerCollection.add(channel)}
//                    }
//                  }
//              }
//              markerCollection.remove(OutsideWorldChannelEndpointID) // outside world marker cannot be collected
//              val conf = ReplayCheckpointConfig(s"replay_checkpoint-$replayChkptId", markerCollection.toSet, snapshotStats.alignment, snapshot.id)
//              buffer.append(conf)
//          }
//        }
//    }
//    converted.toMap
//  }

}
