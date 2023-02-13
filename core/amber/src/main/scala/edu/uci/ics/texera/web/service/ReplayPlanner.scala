package edu.uci.ics.texera.web.service

import com.google.common.collect.Sets
import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
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
  case class ReplayExecution(restart: Boolean, conf: WorkflowStateRestoreConfig, fromIdx:Int) extends PlannerStep

  def getExpectedReplayTime(time:Array[Int], from:Int, to:Int): Int ={
    time(to) - time(from)
  }

  def getLastCheckpoint(checkpoint:Set[Int], replayTo:Int):Int = {
    checkpoint.filter(_ <= replayTo).minBy(replayTo - _)
  }

  def getTotalReplayTime(time:Array[Int], checkpoint: Set[Int], loadCost:Array[Int], replayPoints: Array[Int]):Int = {
    var res = 0
    replayPoints.foreach{
      rp =>
        val lastChkpt = getLastCheckpoint(checkpoint, rp)
        val replayTime = loadCost(lastChkpt)+getExpectedReplayTime(time, lastChkpt, rp)
        res += replayTime
    }
    res
  }
}

class ReplayPlanner(interactionHistory: Seq[(Int,Map[ActorVirtualIdentity, (Long,Int,Int)])]) {
  CheckpointHolder.clear()
  private val time:Array[Int] = Array(0) ++ interactionHistory.map(_._1)
  private val alignments = Array(Map[ActorVirtualIdentity, Long]()) ++ interactionHistory.map(_._2).map(_.map(x => x._1 -> x._2._1).toMap)
  private val checkpointCost = Array(0) ++ interactionHistory.map(_._2).map(_.map(x => x._2._2).sum)
  private val loadCost = Array(0) ++ interactionHistory.map(_._2).map(_.map(x => x._2._3).sum)
  private var checkpointed = Set[Int](0)
  private val stepsQueue = mutable.Queue[PlannerStep]()
  private var currentIdx = 999
  private var targetIdx = 0

  def hasNext: Boolean = {
    val res = stepsQueue.nonEmpty
    if(!res){
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

  def addCheckpoint(controllerAlignment: Any): Int = {
    val interactionPointIdx = alignments.indexWhere(x =>
      x.contains(CONTROLLER) && x(CONTROLLER) == controllerAlignment
    )
    checkpointed = checkpointed ++ Set(interactionPointIdx)
    interactionPointIdx
  }

  private def createRestore(fromCheckpoint: Int, replayTo: Int): WorkflowStateRestoreConfig = {
    val controllerConf = StateRestoreConfig(
      mkOptionForActor(CONTROLLER, fromCheckpoint),
      mkOptionForActor(CONTROLLER, replayTo)
    )
    val workerConf = alignments(replayTo).keys
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
    alignments(value).get(actorVirtualIdentity)
  }

  def startPlanning(replayTo: Int): Unit = {
    if(currentIdx == replayTo){
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

    val checkpointCostThreshold = 5
    checkpointIndices = dynamicProgrammingPlanner(startingPoint, replayTo, checkpointCostThreshold)
    println(s"output plan = $checkpointIndices")
    println(s"best plan = ${bruteForcePlanner(startingPoint, replayTo, checkpointCostThreshold)}")
    println("---------------------------planner replay plan------------------------")
    var cur = startingPoint
    checkpointIndices.toSeq.sorted.foreach{
      toCheckpoint =>
        println(s"replay from $cur to $toCheckpoint with restart = $requireRestart")
        stepsQueue.enqueue(ReplayExecution(requireRestart, createRestore(cur, toCheckpoint), cur))
        println(s"take checkpoint at $toCheckpoint")
        stepsQueue.enqueue(CheckpointCurrentState())
        cur = toCheckpoint
        if(requireRestart){
          requireRestart = false
        }
    }
    if(cur != replayTo){
      println(s"replay from $cur to $replayTo with restart = $requireRestart")
      stepsQueue.enqueue(ReplayExecution(requireRestart, createRestore(cur, replayTo), cur))
    }
    println("---------------------------------------------------------------------")
  }

  def progressivePlanner(from:Int, to:Int, replayTimeThreshold:Int, checkpointCostThreshold: Int): Set[Int] ={
    var cur = from
    var accumulated = 0
    var cost = 0
    val res = mutable.ArrayBuffer[Int]()
    for(i <- from until to){
      if(accumulated >= replayTimeThreshold && cost+checkpointCost(i) < checkpointCostThreshold){
        res.append(i)
        cur = i
        cost += checkpointCost(i)
        accumulated = 0
      }
      accumulated += getExpectedReplayTime(time, i,i+1)
    }
    if(accumulated > replayTimeThreshold && cost+checkpointCost(to) < checkpointCostThreshold){
      cost += checkpointCost(to)
      res.append(to)
    }
    res.toSet
  }


  def greedyPlanner(from:Int, to:Int, checkpointCostThreshold: Int): Set[Int] ={
    var cost = 0
    var stop = false
    val replayPoints = (from+1 to to).toArray
    val remaining = mutable.HashSet(replayPoints:_*)
    val selected = mutable.HashSet[Int]()
    while(!stop){
      var bestChoice = -1
      var bestCost = getTotalReplayTime(time, checkpointed ++ selected, loadCost, replayPoints)
      for(i <- remaining) {
        val replayTime = getTotalReplayTime(time, checkpointed ++ selected ++ Set(i), loadCost, replayPoints)
        if(replayTime < bestCost && cost+checkpointCost(i) < checkpointCostThreshold){
          bestCost = replayTime
          bestChoice = i
        }
      }
      if(bestChoice != -1){
        selected.add(bestChoice)
        cost += checkpointCost(bestChoice)
      }else{
        stop = true
      }
    }
    selected.toSet
  }


  def bruteForcePlanner(from:Int, to:Int, checkpointCostThreshold: Int): Set[Int] ={
    var bestPlan:Set[Int] = Set()
    var bestCost = Int.MaxValue
    val replayPoints = (from+1 to to).toArray
    Sets.powerSet(Sets.newHashSet((from+1 to to).asJava)).asScala.foreach{
      choice =>
        val choiceScalaSet = choice.asScala
        val planCost = getTotalReplayTime(time, checkpointed ++ choiceScalaSet, loadCost, replayPoints)
        val chkptCost = choiceScalaSet.map(x => checkpointCost(x)).sum
        println(s"choice = ${choice} planCost = ${planCost} checkpointCost = $chkptCost")
        if(planCost <= bestCost && chkptCost < checkpointCostThreshold && choiceScalaSet.size >= bestPlan.size){
          bestCost = planCost
          bestPlan = choiceScalaSet.toSet
      }
    }
    bestPlan
  }


  def dynamicProgrammingPlanner(from:Int, to:Int, checkpointCostThreshold: Int): Set[Int] ={
    val res = mutable.HashMap[Int, Set[Int]]()
    val windowSize = to - from
    val dp = (from to to).map(i => (from to i).map(j => getExpectedReplayTime(time, from, j)).sum).toBuffer
    for(i <- 0 to windowSize){
      res(i) = Set()
    }
    for(right <- 1 to windowSize){
      for(k <- 1 to right){
        var replayTimeAfterK = 0
        for(j <- k to right){
          replayTimeAfterK += loadCost(k)+getExpectedReplayTime(time, k,j)
        }
        val subProblemReplayTime = dp(k-1)
        if(dp(right) >= subProblemReplayTime+replayTimeAfterK){
          val newSet = res(k-1)++Set(k)
          if(newSet.map(checkpointCost(_)).sum <= checkpointCostThreshold){
            dp(right) = subProblemReplayTime+replayTimeAfterK+checkpointCost(k)
            res(right) = newSet
          }
        }
      }
    }
    res(windowSize)
  }

}
