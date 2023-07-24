package edu.uci.ics.amber.engine.architecture.execution

import akka.actor.Address
import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.WorkerInfo
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity, LinkIdentity}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.{COMPLETED, PAUSED, READY, RUNNING, UNINITIALIZED}
import edu.uci.ics.texera.web.workflowruntimestate.{OperatorRuntimeStats, WorkflowAggregatedState}

import scala.collection.mutable

class WorkflowExecution(workflow: Workflow) extends java.io.Serializable {

  private val linkExecutions: Map[LinkIdentity, LinkExecution] =
    workflow.physicalPlan.linkStrategies.map { link =>
      link._1 -> new LinkExecution(link._2.totalReceiversCount)
    }
  private val operatorExecutions: Map[LayerIdentity, OperatorExecution] =
    workflow.getAllOperators.map { opConf =>
      opConf.id -> new OperatorExecution(opConf.numWorkers)
    }.toMap

  // Since one operator/link(i.e. links within an operator) can belong to multiple regions, we need to keep
  // track of those already built
  val builtOperators = new mutable.HashSet[LayerIdentity]()
  val openedOperators = new mutable.HashSet[LayerIdentity]()
  val initializedPythonOperators = new mutable.HashSet[LayerIdentity]()
  val activatedLink = new mutable.HashSet[LinkIdentity]()

  val constructingRegions = new mutable.HashSet[PipelinedRegionIdentity]()
  val startedRegions = new mutable.HashSet[PipelinedRegionIdentity]()

  // regions sent by the policy to be scheduled at least once
  val scheduledRegions = new mutable.HashSet[PipelinedRegionIdentity]()
  val completedRegions = new mutable.HashSet[PipelinedRegionIdentity]()
  // regions currently running
  val runningRegions = new mutable.HashSet[PipelinedRegionIdentity]()
  val completedLinksOfRegion =
    new mutable.HashMap[PipelinedRegionIdentity, mutable.Set[LinkIdentity]]

  def getAllWorkers: Iterable[ActorVirtualIdentity] =
    operatorExecutions.values
      .flatMap(operator => operator.identifiers.map(worker => operator.getWorkerInfo(worker)))
      .map(_.id)

  def getOperatorExecution(op: LayerIdentity): OperatorExecution = {
    operatorExecutions(op)
  }

  def getOperatorExecution(worker: ActorVirtualIdentity): OperatorExecution = {
    operatorExecutions.values.foreach { execution =>
      val result = execution.identifiers.find(x => x == worker)
      if (result.isDefined) {
        return execution
      }
    }
    throw new NoSuchElementException(s"cannot find operator with worker = $worker")
  }

  def getLinkExecution(link: LinkIdentity): LinkExecution = linkExecutions(link)

  def getAllOperatorExecutions: Iterable[(LayerIdentity, OperatorExecution)] = operatorExecutions

  def getAllWorkerInfoOfAddress(address: Address): Iterable[WorkerInfo] = {
    operatorExecutions.values
      .flatMap(x => {
        x.identifiers.map(x.getWorkerInfo)
      })
      .filter(info => info.ref.path.address == address)
  }

  def getWorkflowStatus: Map[String, OperatorRuntimeStats] = {
    operatorExecutions.map(op => (op._1.operator, op._2.getOperatorStatistics)).toMap
  }

  def isCompleted: Boolean =
    operatorExecutions.values.forall(op => op.getState == WorkflowAggregatedState.COMPLETED)

  def getState: WorkflowAggregatedState = {
    val opStates = operatorExecutions.values.map(_.getState)
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

}
