package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.deploysemantics.PhysicalOp
import edu.uci.ics.amber.engine.architecture.scheduling.{Region, RegionIdentity}
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  PhysicalOpIdentity
}
import edu.uci.ics.amber.engine.common.workflow.PhysicalLink
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState._
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}

import scala.collection.mutable

class ExecutionState(workflow: Workflow) {
  def appendRegions(regions: Set[RegionIdentity]) = {
    regionExecutionSequence.append(regions)
  }

  private val regionExecutionSequence: mutable.ListBuffer[Set[RegionIdentity]] =
    mutable.ListBuffer()
  private val regionExecutions: mutable.Map[RegionIdentity, RegionExecution] = mutable.HashMap()

  val builtChannels: mutable.Set[ChannelIdentity] = mutable.HashSet[ChannelIdentity]()

  def getRegionExecution(regionId: RegionIdentity): RegionExecution = {
    regionExecutions(regionId)
  }
  def getAllBuiltWorkers: Iterable[ActorVirtualIdentity] =
    getAllActiveOperatorExecutions
      .map(_._2)
      .flatMap(opExecution => opExecution.getBuiltWorkerIds)

  def getOperatorExecution(op: PhysicalOpIdentity): OperatorExecution = {
    // assume only one active op
    regionExecutions.values.map(_.getOperatorExecution(op)).filter(_.isDefined).head.get
  }

  def getLatestOperatorExecution(physicalOpId: PhysicalOpIdentity): Option[OperatorExecution] = {
    // assume only one active op
    regionExecutionSequence
      .map(regionIds =>
        regionIds
          .map(regionId => regionExecutions(regionId))
          .map(regionExecution => regionExecution.getOperatorExecution(physicalOpId))
      )
      .filter(_.count(_.isDefined) != 0)
      .map(executionOpts => {
        assert(executionOpts.count(_.isDefined) == 1)
        executionOpts.filter(_.isEmpty).head.get
      }).lastOption
  }

  def initRegionExecution(region: Region): RegionExecution = {
    regionExecutions += region.id -> new RegionExecution()
    regionExecutions(region.id)
  }
  def getOperatorExecution(worker: ActorVirtualIdentity): OperatorExecution = {

    getAllActiveOperatorExecutions.map(_._2).foreach { execution =>
      val result = execution.getBuiltWorkerIds.find(x => x == worker)
      if (result.isDefined) {
        return execution
      }
    }
    throw new NoSuchElementException(s"cannot find operator with worker = $worker")
  }

  def getLinkExecution(link: PhysicalLink): LinkExecution = {
    // assume only one active link
    regionExecutions.values.map(_.getLinkExecution(link)).filter(_.isDefined).head.get
  }
  def getAllActiveOperatorExecutions: Iterable[(PhysicalOpIdentity, OperatorExecution)] =
    regionExecutions.values.filter(_.isRunning).flatMap(_.getAllOperatorExecutions)

  def getAllWorkersOfRegion(region: Region): Set[ActorVirtualIdentity] = {
    region.getEffectiveOperators.flatMap(physicalOp =>
      getOperatorExecution(physicalOp.id).getBuiltWorkerIds.toList
    )
  }

  def getWorkflowStatus: Map[String, OperatorRuntimeStats] = {
    getAllActiveOperatorExecutions
      .map(op => (op._1.logicalOpId.id, op._2.getOperatorStatistics))
      .toMap
  }

  def isCompleted: Boolean = regionExecutions.values.forall(_.isCompleted)

  def getState: WorkflowAggregatedState = {
    val opStates = getAllActiveOperatorExecutions.map(_._2).map(_.getState)
    if (opStates.isEmpty) {
      return WorkflowAggregatedState.UNINITIALIZED
    }
    if (opStates.forall(_ == COMPLETED)) {
      return WorkflowAggregatedState.COMPLETED
    }
    if (opStates.exists(_ == RUNNING)) {
      return WorkflowAggregatedState.RUNNING
    }
    val unCompletedOpStates = opStates.filter(_ != COMPLETED)
    val runningOpStates = unCompletedOpStates.filter(_ != UNINITIALIZED)
    if (unCompletedOpStates.forall(_ == UNINITIALIZED)) {
      return WorkflowAggregatedState.UNINITIALIZED
    }
    if (runningOpStates.forall(_ == PAUSED)) {
      WorkflowAggregatedState.PAUSED
    } else if (runningOpStates.forall(_ == READY)) {
      WorkflowAggregatedState.READY
    } else {
      WorkflowAggregatedState.UNKNOWN
    }
  }

  def getAllWorkersForOperators(
      operators: Set[PhysicalOpIdentity]
  ): Set[ActorVirtualIdentity] = {
    operators.flatMap(physicalOpId => getOperatorExecution(physicalOpId).getBuiltWorkerIds)
  }

  def filterPythonPhysicalOpIds(
      fromOperatorsList: Set[PhysicalOpIdentity]
  ): Set[PhysicalOpIdentity] = {
    fromOperatorsList.filter(physicalOpId =>
      getOperatorExecution(physicalOpId).getBuiltWorkerIds.nonEmpty &&
        workflow.physicalPlan.getOperator(physicalOpId).isPythonOperator
    )
  }

  def getPythonWorkerToOperatorExec(
      pythonPhysicalOpIds: Set[PhysicalOpIdentity]
  ): Iterable[(ActorVirtualIdentity, PhysicalOp)] = {
    pythonPhysicalOpIds
      .map(opId => workflow.physicalPlan.getOperator(opId))
      .filter(physicalOp => physicalOp.isPythonOperator)
      .flatMap(physicalOp =>
        getOperatorExecution(physicalOp.id).getBuiltWorkerIds.map(worker => (worker, physicalOp))
      )
  }

}
