package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.worker.{ReplayCheckpointConfig, ReplayConfig}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelEndpointID,
  OutsideWorldChannelEndpointID
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.mutable

class ReplayCheckpointPlanner(history: ProcessingHistory, timeLimit: Long) {

  private val checkpointMapping =
    mutable.HashMap[Int, String]().withDefaultValue("global_checkpoint-1")
  private val plannedCheckpoints = getPartialPlan(
    getGlobalPlan(0, history.historyArray.length, timeLimit)
  )
  private val unscheduledCheckpoints = plannedCheckpoints.keySet.to[collection.mutable.Set]

  def pickInRange(start: Int, end: Int): (Map[ActorVirtualIdentity, Int], Long, Int) = {
    var currentPlan = history.getSnapshot(start).getParticipants.map(x => x -> start).toMap
    var currentCost = history.getPlanCost(currentPlan)
    var iteration = 0
    if (start == end) {
      return (currentPlan, currentCost, iteration)
    }
    while (true) {
      iteration += 1
      var nextCost = Long.MaxValue
      var nextPlan = currentPlan
      var currentDecision: Option[(ActorVirtualIdentity, Int)] = None
      (start + 1 to end).foreach { i =>
        val snapshot = history.getSnapshot(i)
        snapshot.getParticipants.foreach { op =>
          val plan = currentPlan.updated(op, i)
          val planCost = history.getPlanCost(plan)
          //println(s"$plan  => $planCost")
          if (planCost < nextCost) {
            nextCost = planCost
            nextPlan = plan
            currentDecision = Some((op, i))
          }
        }
      }
      if (nextCost < currentCost) {
        println(s"iter $iteration: $nextPlan")
        currentCost = nextCost
        currentPlan = nextPlan
      } else {
        return (currentPlan, currentCost, iteration)
      }
    }
    (currentPlan, currentCost, iteration)
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

  def getPartialPlan(globalPlan: Set[Int]): Map[String, Map[ActorVirtualIdentity, Int]] = {
    if (globalPlan.isEmpty) {
      return Map.empty
    }
    val segs = globalPlan.toSeq.sorted
    var currentIdx = 0
    var head: Int = segs(0)
    var next: Int = if (segs.length > 1) { segs(1) }
    else { -1 }
    val interactions = history.getInteractionIdxes.filter(x => x >= head)
    var currentIdx2 = 0
    val tmp = mutable.HashMap[String, Map[ActorVirtualIdentity, Int]]()
    println(s"global plan: $globalPlan")
    while (head != -1) {
      val chkptId = s"replay_checkpoint-${currentIdx + 1}"
      var interactionIdx = interactions(currentIdx2)
      val firstInteraction = interactionIdx
      while (
        interactionIdx != -1 && interactionIdx >= head && (next == -1 || interactionIdx < next)
      ) {
        assert(history.getTimeGap(head, interactionIdx) <= timeLimit)
        checkpointMapping(interactionIdx) = chkptId
        currentIdx2 += 1
        if (currentIdx2 < interactions.length) {
          interactionIdx = interactions(currentIdx2)
        } else {
          interactionIdx = -1
        }
      }
      val partialPlan = pickInRange(head, firstInteraction)
      tmp(chkptId) = partialPlan._1
      head = next
      currentIdx += 1
      next = if (segs.length > currentIdx + 1) { segs(currentIdx + 1) }
      else { -1 }
    }
    tmp.toMap
  }

  def getReplayConfig(checkpointId: String): Map[ActorVirtualIdentity, ReplayCheckpointConfig] = {
    plannedCheckpoints(checkpointId).map {
      case (identity, i) =>
        val snapshot = history.getSnapshot(i)
        val snapshotStats = snapshot.getStats(identity)
        val markerCollection = mutable.HashSet[ChannelEndpointID]()
        plannedCheckpoints(checkpointId).foreach {
          case (upstream, chkptPos) =>
            val snapshot2 = history.getSnapshot(chkptPos)
            if (snapshot2.getParticipants.toSet.contains(identity)) {
              snapshot2.getStats(identity).inputStatus.keys.toSet.foreach {
                channel: ChannelEndpointID =>
                  if (channel.endpointWorker == upstream) { markerCollection.add(channel) }
              }
            }
        }
        markerCollection.remove(
          OutsideWorldChannelEndpointID
        ) // outside world marker cannot be collected
        identity -> ReplayCheckpointConfig(
          checkpointId,
          markerCollection.toSet,
          snapshotStats.alignment,
          snapshot.id
        )
    }
  }

  def generateReplayPlan(targetTime: Long): WorkflowReplayConfig = {
    val interactionIdx = history.getInteractionIdx(targetTime)
    val prevInteractions =
      history.getInteractionIdxes.filter(x => x <= interactionIdx).reverse.to[mutable.Queue]
    val toCheckpoint = mutable.ArrayBuffer[String]()
    while (
      prevInteractions.nonEmpty && unscheduledCheckpoints.contains(
        checkpointMapping(prevInteractions.head)
      )
    ) {
      if (!toCheckpoint.contains(checkpointMapping(prevInteractions.head))) {
        toCheckpoint.append(checkpointMapping(prevInteractions.head))
      }
      prevInteractions.dequeue()
    }
    val converted =
      mutable.HashMap[ActorVirtualIdentity, mutable.ArrayBuffer[ReplayCheckpointConfig]]()
    toCheckpoint.foreach { chkpt =>
      unscheduledCheckpoints.remove(chkpt)
      getReplayConfig(chkpt).foreach {
        case (identity, config) =>
          converted
            .getOrElseUpdate(identity, mutable.ArrayBuffer[ReplayCheckpointConfig]())
            .append(config)
      }
    }
    val startingPoint = checkpointMapping(if (prevInteractions.nonEmpty) { prevInteractions.head }
    else { -1 })
    val targetSnapshot = history.getSnapshot(targetTime)
    WorkflowReplayConfig(targetSnapshot.getParticipants.map { worker =>
      val workerStats = targetSnapshot.getStats(worker)
      val replayTo = workerStats.alignment
      val checkpointOpt = CheckpointHolder.getCheckpointAlignment(worker, startingPoint)
      if (checkpointOpt.isDefined) {
        worker -> ReplayConfig(
          Some(checkpointOpt.get),
          Some(replayTo),
          converted.getOrElse(worker, mutable.ArrayBuffer[ReplayCheckpointConfig]()).toArray
        )
      } else {
        worker -> ReplayConfig(
          None,
          Some(replayTo),
          converted.getOrElse(worker, mutable.ArrayBuffer[ReplayCheckpointConfig]()).toArray
        )
      }
    }.toMap - CLIENT) // client is always ready
  }

}
