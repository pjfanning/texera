package edu.uci.ics.texera.web.service

import com.google.common.collect.Sets
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.InteractionHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowStateRestoreConfig
import edu.uci.ics.amber.engine.architecture.worker.StateRestoreConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.web.service.ReplayPlanner._

import scala.collection.mutable
import scala.jdk.CollectionConverters.{asJavaIterableConverter, asScalaSetConverter}

object ReplayPlanner {

  sealed trait PlannerStep
  case class CheckpointCurrentState() extends PlannerStep
  case class ReplayExecution(restart: Boolean, conf: WorkflowStateRestoreConfig, fromIdx: Int)
      extends PlannerStep

  def getLastCheckpoint(checkpoint: Iterable[Int], replayTo: Int): Int = {
    checkpoint.filter(_ <= replayTo).minBy(replayTo - _)
  }

}

class ReplayPlanner(interactionHistory: InteractionHistory) {
  CheckpointHolder.clear()
  private var checkpointed = Set[Int](0)
  private val stepsQueue = mutable.Queue[PlannerStep]()
  private var currentIdx = 999
  private var targetIdx = 0

  def hasNext: Boolean = {
    val res = stepsQueue.nonEmpty
    if (!res) {
      currentIdx = targetIdx
    }
    res
  }

  def next(): PlannerStep = {
    val res = stepsQueue.dequeue()
    res match {
      case ReplayPlanner.CheckpointCurrentState() =>
      //skip
      case ReplayExecution(restart, conf, fromIdx) =>
        currentIdx = fromIdx
    }
    res
  }

  def addCheckpoint(controllerAlignment: Long): Int = {
    val interactionPointIdx =
      interactionHistory.findInteractionIdx(CONTROLLER, controllerAlignment)
    checkpointed = checkpointed ++ Set(interactionPointIdx)
    interactionPointIdx
  }

  private def createRestore(fromCheckpoint: Int, replayTo: Int): WorkflowStateRestoreConfig = {
    val controllerConf = StateRestoreConfig(
      mkOptionForActor(CONTROLLER, fromCheckpoint),
      mkOptionForActor(CONTROLLER, replayTo)
    )
    val workerConf = interactionHistory.getInteraction(replayTo).getParticipants
      .filter(_ != CONTROLLER)
      .map { identity =>
        identity -> StateRestoreConfig(
          mkOptionForActor(identity, fromCheckpoint),
          mkOptionForActor(identity, replayTo)
        )
      }
      .toMap
    WorkflowStateRestoreConfig(controllerConf, workerConf)
  }

  private def mkOptionForActor(
      actorVirtualIdentity: ActorVirtualIdentity,
      value: Int
  ): Option[Long] = {
    interactionHistory.getInteraction(value).getAlignment(actorVirtualIdentity)
  }

  def startPlanning(replayTo: Int): Unit = {
    if (currentIdx == replayTo) {
      return
    }
    stepsQueue.clear()
    targetIdx = replayTo
    val lastChkpt = getLastCheckpoint(checkpointed, replayTo)
    var requireRestart = false
    var checkpointIndices = Set[Int]()
    var startingPoint = currentIdx
    if (replayTo >= currentIdx && lastChkpt <= currentIdx) {
      println(s"planner output: continue replay to $replayTo")
    } else {
      println(s"planner output: restore state from $lastChkpt then replay to $replayTo")
      requireRestart = true
      startingPoint = lastChkpt
    }

    val replayTimeThreshold = 5000
    checkpointIndices = dynamicProgrammingPlanner(startingPoint, replayTo, replayTimeThreshold)
    println(interactionHistory)
    val best = bruteForcePlanner(startingPoint, replayTo, replayTimeThreshold)
    println(s"output plan = $checkpointIndices")
    println(s"best plan = $best")
    println("---------------------------planner replay plan------------------------")
    var cur = startingPoint
    checkpointIndices.toSeq.sorted.foreach { toCheckpoint =>
      println(s"replay from $cur to $toCheckpoint with restart = $requireRestart")
      stepsQueue.enqueue(ReplayExecution(requireRestart, createRestore(cur, toCheckpoint), cur))
      println(s"take checkpoint at $toCheckpoint")
      stepsQueue.enqueue(CheckpointCurrentState())
      cur = toCheckpoint
      if (requireRestart) {
        requireRestart = false
      }
    }
    if (cur != replayTo || requireRestart) {
      println(s"replay from $cur to $replayTo with restart = $requireRestart")
      stepsQueue.enqueue(ReplayExecution(requireRestart, createRestore(cur, replayTo), cur))
    }
    println("---------------------------------------------------------------------")
  }

  def getCheckpointMap(checkpointed:Iterable[Int]): Map[ActorVirtualIdentity, Set[Long]] ={
    checkpointed.flatMap(x => interactionHistory.getInteraction(x).getAlignmentMap.toSeq).groupBy(_._1).mapValues(_.map(_._2).toSet)
  }

  def bruteForcePlanner(from: Int, to: Int, replayTimeThreshold: Int): Set[Int] = {
    var bestPlan: Set[Int] = (from + 1 to to).toSet
    var bestCost = Long.MaxValue
    var bestUnsatisfied = Int.MaxValue
    val replayPoints = (from + 1 to to).toArray
    Sets.powerSet(Sets.newHashSet((from + 1 to to).asJava)).asScala.foreach { choice =>
      val choiceScalaArray = choice.asScala.toArray
      val validation = interactionHistory.validateReplayTime(checkpointed ++ choiceScalaArray, replayPoints, replayTimeThreshold)
      if(validation <= bestUnsatisfied){
        val planCost = interactionHistory.getCheckpointCost(choiceScalaArray, getCheckpointMap(checkpointed))
        // println(s"choice = ${choice} planCost = ${planCost}")
        if (validation < bestUnsatisfied || planCost < bestCost) {
          bestUnsatisfied = validation
          bestCost = planCost
          bestPlan = choiceScalaArray.toSet
        }
      }
    }
    bestPlan
  }

  def dynamicProgrammingPlanner(from: Int, to: Int, replayTimeThreshold: Int): Set[Int] = {
    val res = mutable.HashMap[(Int, Int), Set[Int]]()
    val checkpointedMap = getCheckpointMap(checkpointed)
    for(i <- from to to) {
      for (j <- from to i) {
        res.put((i, j), (from + 1 to j).toSet)
      }
      val replayPoints = (from + 1 to i).toArray
      for (j <- from to i) {
        for (k <- from until j) {
          val candidate: Set[Int] = if (j != from) {
            res(j - 1, k).union(Set(j))
          } else {
            Set()
          }
          val validation = interactionHistory.validateReplayTime(candidate ++ checkpointed, replayPoints, replayTimeThreshold)
          val unsatisfied = interactionHistory.validateReplayTime(res(i,j)++ checkpointed, replayPoints, replayTimeThreshold)
          if(validation <= unsatisfied){
            val candidateCost = interactionHistory.getCheckpointCost(candidate, checkpointedMap)
            // println(j, i, candidate, candidateCost)
            if (validation < unsatisfied || candidateCost < interactionHistory.getCheckpointCost(res((i,j)), checkpointedMap)) {
              res.put((i, j), candidate)
            }
          }
        }
      }
    }
    var finalMinCost = Long.MaxValue
    var finalResult: Set[Int] = (from + 1 to to).toSet
    var bestUnsatisfied = Int.MaxValue
    val replayPoints = (from + 1 to to).toArray
    for (j <- from to to) {
      val unsatisfied = interactionHistory.validateReplayTime(res((to,j)) ++ checkpointed, replayPoints, replayTimeThreshold)
      if(unsatisfied <= bestUnsatisfied){
        val cost = interactionHistory.getCheckpointCost(res((to,j)), checkpointedMap)
        if(unsatisfied < bestUnsatisfied || cost < finalMinCost){
          bestUnsatisfied = unsatisfied
          finalMinCost = cost
          finalResult = res((to,j))
        }
      }
    }
    finalResult
  }


}
