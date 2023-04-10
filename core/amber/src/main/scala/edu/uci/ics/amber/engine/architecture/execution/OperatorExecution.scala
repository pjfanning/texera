package edu.uci.ics.amber.engine.architecture.execution

import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.GlobalBreakpoint
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{WorkerInfo, WorkerWorkloadInfo}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{
  COMPLETED,
  PAUSED,
  READY,
  RUNNING,
  UNINITIALIZED
}
import edu.uci.ics.amber.engine.architecture.worker.statistics.{WorkerState, WorkerStatistics}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}

import scala.collection.mutable

class OperatorExecution(numWorkers: Int, val opExecClass: Class[_]) {

  /*
   * Variables related to runtime information
   */

  // workers of this operator
  private val workers: mutable.HashMap[ActorVirtualIdentity, WorkerInfo] =
    mutable.HashMap[ActorVirtualIdentity, WorkerInfo]()

  var attachedBreakpoints = new mutable.HashMap[String, GlobalBreakpoint[_]]()
  var caughtLocalExceptions = new mutable.HashMap[ActorVirtualIdentity, Throwable]()
  var workerToWorkloadInfo = new mutable.HashMap[ActorVirtualIdentity, WorkerWorkloadInfo]()

  def isBuilt: Boolean = workers.nonEmpty

  def states: Array[WorkerState] = workers.values.map(_.state).toArray

  def statistics: Array[WorkerStatistics] = workers.values.map(_.stats).toArray

  def getWorkerInfo(id: ActorVirtualIdentity): WorkerInfo = {
    workers.getOrElseUpdate(
      id,
      WorkerInfo(id, UNINITIALIZED, WorkerStatistics(UNINITIALIZED, 0, 0), 1, // 1 = controller
        null)
    )
  }

  def getWorkerWorkloadInfo(id: ActorVirtualIdentity): WorkerWorkloadInfo = {
    if (!workerToWorkloadInfo.contains(id)) {
      workerToWorkloadInfo(id) = WorkerWorkloadInfo(0L, 0L)
    }
    workerToWorkloadInfo(id)
  }

  def getAllWorkerStates: Iterable[WorkerState] = states

  def getInputRowCount: Long = statistics.map(_.inputTupleCount).sum

  def getOutputRowCount: Long = statistics.map(_.outputTupleCount).sum

  def identifiers: Array[ActorVirtualIdentity] = workers.values.map(_.id).toArray

  def assignBreakpoint(breakpoint: GlobalBreakpoint[_]): Array[ActorVirtualIdentity] = {
    identifiers
  }

  def setAllWorkerState(state: WorkerState): Unit = {
    (0 until numWorkers).foreach(states.update(_, state))
  }

  def getState: WorkflowAggregatedState = {
    val workerStates = getAllWorkerStates
    if (workerStates.isEmpty) {
      return WorkflowAggregatedState.UNINITIALIZED
    }
    if (workerStates.forall(_ == COMPLETED)) {
      return WorkflowAggregatedState.COMPLETED
    }
    if (workerStates.exists(_ == RUNNING)) {
      return WorkflowAggregatedState.RUNNING
    }
    val unCompletedWorkerStates = workerStates.filter(_ != COMPLETED)
    if (unCompletedWorkerStates.forall(_ == UNINITIALIZED)) {
      WorkflowAggregatedState.UNINITIALIZED
    } else if (unCompletedWorkerStates.forall(_ == PAUSED)) {
      WorkflowAggregatedState.PAUSED
    } else if (unCompletedWorkerStates.forall(_ == READY)) {
      WorkflowAggregatedState.READY
    } else {
      WorkflowAggregatedState.UNKNOWN
    }
  }

  def getOperatorStatistics: OperatorRuntimeStats =
    OperatorRuntimeStats(getState, getInputRowCount, getOutputRowCount)
}
