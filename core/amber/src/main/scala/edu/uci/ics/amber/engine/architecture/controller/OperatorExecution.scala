package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.breakpoint.globalbreakpoint.GlobalBreakpoint
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{WorkerInfo, WorkerWorkloadInfo}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState._
import edu.uci.ics.amber.engine.architecture.worker.statistics.{WorkerState, WorkerStatistics}
import edu.uci.ics.amber.engine.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  ExecutionIdentity,
  PhysicalOpIdentity,
  WorkflowIdentity
}
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

class OperatorExecution(
    workflowId: WorkflowIdentity,
    val executionId: ExecutionIdentity,
    physicalOpId: PhysicalOpIdentity,
    numWorkers: Int
) extends Serializable {

  //reshape-related:
  var workerToWorkloadInfo = new mutable.HashMap[ActorVirtualIdentity, WorkerWorkloadInfo]()

  // breakpoint-related:
  var attachedBreakpoints = new mutable.HashMap[String, GlobalBreakpoint[_]]()

  // workerExecutions of this operator
  private val workerExecutions = new mutable.HashMap[ActorVirtualIdentity, WorkerExecution]()

  def states: Array[WorkerState] = workerExecutions.values.map(_.state).toArray

  def statistics: Array[WorkerStatistics] = workerExecutions.values.map(_.statistics).toArray

  def initializeWorkerExecution(id: ActorVirtualIdentity): Unit = {
    workerExecutions.put(
      id,
      WorkerExecution(
        UNINITIALIZED,
        WorkerStatistics(UNINITIALIZED, 0, 0, 0, 0, 0)
      )
    )
  }

  def getWorkerExecution(id: ActorVirtualIdentity): WorkerExecution = {
    if (!workerExecutions.contains(id)) {
      initializeWorkerExecution(id)
    }
    workerExecutions(id)
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

  def getDataProcessingTime: Long = statistics.map(_.dataProcessingTime).sum

  def getControlProcessingTime: Long = statistics.map(_.controlProcessingTime).sum

  def getIdleTime: Long = statistics.map(_.idleTime).sum

  def getBuiltWorkerIds: Array[ActorVirtualIdentity] = workerExecutions.keys.toArray

  def assignBreakpoint(breakpoint: GlobalBreakpoint[_]): Array[ActorVirtualIdentity] = {
    getBuiltWorkerIds
  }

  def setAllWorkerState(state: WorkerState): Unit = {
    workerExecutions.values.foreach(workerExecution => workerExecution.state = state)
  }

  def getState: WorkflowAggregatedState = {
    val workerStates = workerExecutions.values.map(workerExecution => workerExecution.state)
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
    OperatorRuntimeStats(
      getState,
      getInputRowCount,
      getOutputRowCount,
      numWorkers,
      getDataProcessingTime,
      getControlProcessingTime,
      getIdleTime
    )
}
