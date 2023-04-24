package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.worker.ReplayCheckpointConfig
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, OutsideWorldChannelEndpointID}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ReplayCheckpointPlanner(history:ProcessingHistory) {

  var replayChkptId = 0

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



  def getReplayPlan(end:Int, timeLimit:Long, mem:mutable.HashMap[Int, (Iterable[Map[ActorVirtualIdentity, Int]], Long)]): (Iterable[Map[ActorVirtualIdentity, Int]], Long) ={
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


  def getConvertedPlan(chkptPlan:(Iterable[Map[ActorVirtualIdentity, Int]], Long)): Map[ActorVirtualIdentity, mutable.ArrayBuffer[ReplayCheckpointConfig]] ={
    val converted = mutable.HashMap[ActorVirtualIdentity, mutable.ArrayBuffer[ReplayCheckpointConfig]]()
    chkptPlan._1.foreach{
      plan =>
        plan.foreach{
          case (identity, i) =>
            val buffer = converted.getOrElseUpdate(identity, new ArrayBuffer[ReplayCheckpointConfig]())
            val snapshotStats = history.getSnapshot(i).getStats(identity)
            val markerCollection = mutable.HashSet[ChannelEndpointID]()
            plan.values.toSet.foreach{
              pos:Int =>
                val snapshot2 = history.getSnapshot(pos)
                snapshot2.getStats(identity).inputStatus.keys.foreach(markerCollection.add)
            }
            markerCollection.remove(OutsideWorldChannelEndpointID) // outside world marker cannot be collected
            val conf = ReplayCheckpointConfig(s"replay checkpoint - $replayChkptId", markerCollection.toSet, snapshotStats.alignment)
            buffer.append(conf)
        }
    }
    converted.toMap
  }

}
