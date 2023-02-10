package edu.uci.ics.texera.web.service

import edu.uci.ics.amber.engine.architecture.checkpoint.CheckpointHolder
import edu.uci.ics.amber.engine.architecture.controller.WorkflowStateRestoreConfig
import edu.uci.ics.amber.engine.architecture.worker.StateRestoreConfig
import edu.uci.ics.amber.engine.common.ambermessage.ContinueReplay
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.web.service.ReplayPlanner.{PlannerStep, ReplayExecution}

import scala.collection.mutable
import scala.util.Try

object ReplayPlanner {

  sealed trait PlannerStep
  case class CheckpointCurrentState() extends PlannerStep
  case class ReplayExecution(restart: Boolean, conf: WorkflowStateRestoreConfig) extends PlannerStep
}

class ReplayPlanner(interactionHistory: Array[Map[ActorVirtualIdentity, Long]]) {
  CheckpointHolder.clear()
  private val checkpointed = mutable.HashSet[Int](-1)
  private val stepsQueue = mutable.Queue[PlannerStep]()
  private var currentIdx = -2

  def hasNext: Boolean = {
    stepsQueue.nonEmpty
  }

  def next(): PlannerStep = {
    stepsQueue.dequeue()
  }

  def addCheckpoint(controllerAlignment: Any): Int = {
    val interactionPointIdx = interactionHistory.indexWhere(x =>
      x.contains(CONTROLLER) && x(CONTROLLER) == controllerAlignment
    )
    checkpointed.add(interactionPointIdx)
    interactionPointIdx
  }

  private def createRestore(fromCheckpoint: Int, replayTo: Int): WorkflowStateRestoreConfig = {
    val controllerConf = StateRestoreConfig(
      mkOptionForActor(CONTROLLER, fromCheckpoint),
      mkOptionForActor(CONTROLLER, replayTo)
    )
    val workerConf = interactionHistory(replayTo).keys
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
    if (value == -1) {
      None
    } else {
      interactionHistory(value).get(actorVirtualIdentity)
    }
  }

  def startPlanning(replayTo: Int): Unit = {
    stepsQueue.clear()
    val lastChkpt = checkpointed.filter(_ <= replayTo).minBy(replayTo - _)
    if (replayTo >= currentIdx && lastChkpt <= currentIdx) {
      println(s"planner output: continue replay to $replayTo")
      stepsQueue.enqueue(ReplayExecution(false, createRestore(-1, replayTo)))
    } else {
      println(s"planner output: restore state from $lastChkpt then replay to $replayTo")
      stepsQueue.enqueue(ReplayExecution(true, createRestore(lastChkpt, replayTo)))
    }
    currentIdx = replayTo
//    for(i <- lastChkpt+1 to replayTo ) {
//      stepsQueue.enqueue(ReplayExecution(Left(createContinue(i))))
//
//    }
  }

}
