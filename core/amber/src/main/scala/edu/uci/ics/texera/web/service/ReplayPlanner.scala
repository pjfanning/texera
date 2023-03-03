package edu.uci.ics.texera.web.service

import com.google.common.collect.Sets
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.common.InteractionHistory
import edu.uci.ics.amber.engine.architecture.controller.WorkflowStateRestoreConfig
import edu.uci.ics.amber.engine.architecture.worker.StateRestoreConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.web.service.ReplayPlanner._
import org.nd4j.linalg.activations.impl.ActivationRectifiedTanh

import scala.collection.mutable
import scala.jdk.CollectionConverters.{asJavaIterableConverter, asScalaSetConverter}

object ReplayPlanner {

  sealed trait PlannerStep
  case class CheckpointCurrentState(cutoffMap:Map[ActorVirtualIdentity, Map[ActorVirtualIdentity, Long]]) extends PlannerStep
  case class ReplayExecution(restart: Boolean, conf: WorkflowStateRestoreConfig, fromIdx: Int)
      extends PlannerStep

  def getLastGlobalCheckpoint(checkpoint: Map[Int, Set[ActorVirtualIdentity]], replayTo:Int): Int ={
    checkpoint.keys.filter(_ <= replayTo).minBy(replayTo - _)
  }


  def getLastCheckpoint(checkpoint: Map[Int, Set[ActorVirtualIdentity]], replayTo:Int, candidates:Set[ActorVirtualIdentity]): Map[Int, Set[ActorVirtualIdentity]] = {
    val result = mutable.HashMap[Int, mutable.HashSet[ActorVirtualIdentity]]()
    val added = mutable.HashSet[ActorVirtualIdentity]()
    checkpoint.keys.filter(_ <= replayTo).toArray.sorted.reverse.foreach{
      key =>
        checkpoint(key).foreach{
          worker =>
            if(!added.contains(worker) && candidates.contains(worker)){
              added.add(worker)
             if(result.contains(key)){
               result(key).add(worker)
             }else{
               result(key) = mutable.HashSet(worker)
             }
            }
        }
    }
    result.mapValues(_.toSet).toMap
  }

}

class ReplayPlanner(interactionHistory: InteractionHistory) {
  CheckpointHolder.clear()
  private var checkpointed = Map(0 -> interactionHistory.getInteractions.last.getParticipants.toSet)
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
      case ReplayPlanner.CheckpointCurrentState(_) => //skip
      case ReplayExecution(restart, conf, fromIdx) =>
        currentIdx = fromIdx
    }
    res
  }

  def getCheckpointIndex(controllerAlignment: Long): Int = {
    val interactionPointIdx =
      interactionHistory.findInteractionIdx(CONTROLLER, controllerAlignment)
    interactionPointIdx - 1
  }

  private def createRestore(fromCheckpoint: Int, replayTo: Int): WorkflowStateRestoreConfig = {
    val controllerConf = StateRestoreConfig(
      mkOptionForActor(CONTROLLER, fromCheckpoint),
      mkOptionForActor(CONTROLLER, replayTo)
    )
    val workerConf = interactionHistory
      .getInteraction(replayTo)
      .getParticipants
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

  def startPlanning(replayTo: Int, strategy: String, replayTimeLimit: Int): Unit = {
    if (currentIdx == replayTo) {
      return
    }
    stepsQueue.clear()
    targetIdx = replayTo
    val replayTimeThreshold = replayTimeLimit*1000

    if(strategy.contains("Partial")){
      strategy match{
        case "Partial - naive" =>
        case "Partial - optimized" =>
        case other =>
          throw new RuntimeException("strategy does not match either naive or optimized method")
      }
    }else{
      val lastChkpt = getLastGlobalCheckpoint(checkpointed, replayTo)
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
      checkpointIndices = strategy match{
        case "Complete - all" =>
          (startingPoint+1 to replayTo).toSet
        case "Complete - naive"=>
          // iterative checkpoint
           iterativePlanner(startingPoint, replayTo, replayTimeThreshold)
        case "Complete - optimized" =>
          dynamicProgrammingPlanner(startingPoint, replayTo, replayTimeThreshold)
        case other =>
          // no checkpoint case
          Set()
      }

      println(interactionHistory)
      println(s"output plan = $checkpointIndices")
      println("---------------------------planner replay plan------------------------")
      var cur = startingPoint
      checkpointIndices.toSeq.sorted.foreach { toCheckpoint =>
        println(s"replay from $cur to $toCheckpoint with restart = $requireRestart")
        stepsQueue.enqueue(ReplayExecution(requireRestart, createRestore(cur, toCheckpoint), cur))
        println(s"take checkpoint at $toCheckpoint")
        stepsQueue.enqueue(CheckpointCurrentState(interactionHistory.computeGlobalCheckpointCutoff(toCheckpoint)))
        checkpointed = checkpointed ++ completeCheckpointToPartialRepr(toCheckpoint)
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
  }


  def completeCheckpointToPartialRepr(idx:Int):Map[Int,Set[ActorVirtualIdentity]] = {
    Map(idx -> interactionHistory.getInteraction(idx).getParticipants.toSet)
  }

  def iterativePlanner(from: Int, to:Int, replayTimeThreshold:Int):Set[Int] = {
    var cur = from
    var accumulated = 0L
    val res = mutable.ArrayBuffer[Int]()
    for (i <- from until to) {
      if (
        accumulated > replayTimeThreshold
      ) {
        res.append(i)
        cur = i
        accumulated = 0L
      }
      accumulated += interactionHistory.getGlobalReplayTime(i,i+1)
    }
    if (accumulated > replayTimeThreshold) {
      res.append(to)
    }
    res.toSet
  }

  def bruteForcePlanner(from: Int, to: Int, replayTimeThreshold: Int): Set[Int] = {
    var bestPlan: Set[Int] = (from + 1 to to).toSet
    var bestCost = Long.MaxValue
    var bestUnsatisfied = Int.MaxValue
    val replayPoints = (from + 1 to to).toArray
    Sets.powerSet(Sets.newHashSet((from + 1 to to).asJava)).asScala.foreach { choice =>
      val choiceScalaArray = choice.asScala.toArray
      val validation = interactionHistory
        .validateReplayTime(checkpointed ++ mergeChoices(choiceScalaArray), replayPoints, replayTimeThreshold)
      if (validation <= bestUnsatisfied) {
        val planCost =
          interactionHistory.getCheckpointCost(choiceScalaArray, checkpointed)
        println(s"${choiceScalaArray.mkString("Array(", ", ", ")")} = $validation, $planCost")
        if (validation < bestUnsatisfied || planCost < bestCost) {
          bestUnsatisfied = validation
          bestCost = planCost
          bestPlan = choiceScalaArray.toSet
        }
      }else{
        println(s"${choiceScalaArray.mkString("Array(", ", ", ")")} = $validation")
      }
    }
    bestPlan
  }


  def mergeChoices(choices:Iterable[Int]): Map[Int, Set[ActorVirtualIdentity]] ={
    if(choices.nonEmpty){choices.map(completeCheckpointToPartialRepr).reduce(_ ++ _)}else{Map()}
  }

  def dynamicProgrammingPlanner(from: Int, to: Int, replayTimeThreshold: Int): Set[Int] = {
    val res = mutable.HashMap[(Int, Int), Set[Int]]()
    for (i <- from to to) {
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
          val candidateMap = mergeChoices(candidate)
          val validation = interactionHistory.validateReplayTime(
            candidateMap ++ checkpointed,
            replayPoints,
            replayTimeThreshold
          )
          val subMap = mergeChoices(res(i, j))
          val unsatisfied = interactionHistory.validateReplayTime(
            subMap ++ checkpointed,
            replayPoints,
            replayTimeThreshold
          )
          if (validation <= unsatisfied) {
            val candidateCost = interactionHistory.getCheckpointCost(candidate, checkpointed)
            // println(j, i, candidate, candidateCost)
            if (
              validation < unsatisfied || candidateCost < interactionHistory.getCheckpointCost(
                res((i, j)),
                checkpointed
              )
            ) {
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
      val unsatisfied = interactionHistory.validateReplayTime(
        mergeChoices(res((to, j))) ++ checkpointed,
        replayPoints,
        replayTimeThreshold
      )
      if (unsatisfied <= bestUnsatisfied) {
        val cost = interactionHistory.getCheckpointCost(res((to, j)), checkpointed)
        if (unsatisfied < bestUnsatisfied || cost < finalMinCost) {
          bestUnsatisfied = unsatisfied
          finalMinCost = cost
          finalResult = res((to, j))
        }
      }
    }
    finalResult
  }


  def partialIterativePlanner(dest:Int, replayTimeThreshold:Int):Map[Int, Set[ActorVirtualIdentity]] = {
    val targets = interactionHistory.getInteraction(dest).getParticipants.toSet
    val lastCheckpoint = getLastCheckpoint(checkpointed, dest, targets)
    val lastCheckpointMapping = mutable.HashMap[ActorVirtualIdentity, Int]()
    lastCheckpoint.foreach{
      case (i, identities) =>
        identities.foreach(x => lastCheckpointMapping(x) = i)
    }
    val result = mutable.HashMap[Int, mutable.HashSet[ActorVirtualIdentity]]()
    targets.foreach{
      id =>
        var last = lastCheckpointMapping(id)
        for(i <-  lastCheckpointMapping(id) to dest){
          if(interactionHistory.getWorkerReplayTime(id, last, i) > replayTimeThreshold){
            result.getOrElseUpdate(i,mutable.HashSet[ActorVirtualIdentity]()).add(id)
            last = i
          }
        }
    }
    result.mapValues(_.toSet).toMap
  }

}
