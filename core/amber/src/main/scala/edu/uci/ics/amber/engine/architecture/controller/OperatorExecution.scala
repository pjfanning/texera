package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.GlobalBreakpoint
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{WorkerExecution, WorkerWorkloadInfo}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState._
import edu.uci.ics.amber.engine.architecture.worker.statistics.{WorkerState, WorkerStatistics}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

case class OperatorExecution() extends Serializable {

  // workers of this operator
  private val workerExecutions =
    new util.concurrent.ConcurrentHashMap[ActorVirtualIdentity, WorkerExecution]()

  var attachedBreakpoints = new mutable.HashMap[String, GlobalBreakpoint[_]]()
  var workerToWorkloadInfo = new mutable.HashMap[ActorVirtualIdentity, WorkerWorkloadInfo]()

  def states: Array[WorkerState] = workerExecutions.values.asScala.map(_.state).toArray

  def statistics: Array[WorkerStatistics] = workerExecutions.values.asScala.map(_.stats).toArray

  def initWorkerExecution(workerId: ActorVirtualIdentity): Unit = {
    workerExecutions.put(
      workerId,
      new WorkerExecution(
        UNINITIALIZED,
        WorkerStatistics(UNINITIALIZED, 0, 0)
      )
    )
  }
  def getWorkerExecution(workerId: ActorVirtualIdentity): WorkerExecution = {
    if (!workerExecutions.containsKey(workerId)) {
      initWorkerExecution(workerId)
    }
    workerExecutions.get(workerId)
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

  def getBuiltWorkerIds: Set[ActorVirtualIdentity] = workerExecutions.keySet().asScala.toSet

  def assignBreakpoint(breakpoint: GlobalBreakpoint[_]): Set[ActorVirtualIdentity] = {
    getBuiltWorkerIds
  }

  def setAllWorkerState(state: WorkerState): Unit = {
    workerExecutions.values.asScala.foreach(workerExecution => workerExecution.setState(state))
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
