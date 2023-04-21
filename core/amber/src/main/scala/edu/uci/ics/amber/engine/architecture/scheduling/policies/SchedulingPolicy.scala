package edu.uci.ics.amber.engine.architecture.scheduling.policies

import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.common.WorkflowActorService
import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity, OperatorIdentity}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.mutable
import scala.collection.JavaConverters._

object SchedulingPolicy {
  def createPolicy(
      policyName: String,
      workflow: Workflow,
      execution: WorkflowExecution,
      pipelinedRegionPlan: PipelinedRegionPlan
  ): SchedulingPolicy = {
    val scheduleOrder = pipelinedRegionPlan.getRegionScheduleOrder()
    if (policyName.equals("single-ready-region")) {
      new SingleReadyRegion(workflow, execution, scheduleOrder)
    } else if (policyName.equals("all-ready-regions")) {
      new AllReadyRegions(workflow, execution, scheduleOrder)
    } else if (policyName.equals("single-ready-region-time-interleaved")) {
      new SingleReadyRegionTimeInterleaved(workflow, execution, scheduleOrder)
    } else {
      throw new WorkflowRuntimeException(s"Unknown scheduling policy name")
    }
  }
}

abstract class SchedulingPolicy(workflow: Workflow, execution: WorkflowExecution) {

  protected def isRegionCompleted(plan:PipelinedRegionPlan, regionId: PipelinedRegionIdentity): Boolean = {
    val region = plan.getPipelinedRegion(regionId)
    workflow
      .getBlockingOutLinksOfRegion(region)
      .subsetOf(
        execution.completedLinksOfRegion.getOrElse(regionId, new mutable.HashSet[LinkIdentity]())
      ) &&
    region
      .getOperators()
      .forall(opId =>
        execution.getOperatorExecution(opId).getState == WorkflowAggregatedState.COMPLETED
      )
  }

  protected def checkRegionCompleted(pipelinedRegionPlan: PipelinedRegionPlan,region: PipelinedRegionIdentity): Unit = {
    if (isRegionCompleted(pipelinedRegionPlan, region)) {
      execution.runningRegions.remove(region)
      execution.completedRegions.add(region)
    }
  }

  protected def getRegions(plan:PipelinedRegionPlan, workerId: ActorVirtualIdentity): Set[PipelinedRegion] = {
    val opId = workflow.getOperator(workerId).id
    execution.runningRegions
      .filter(r => plan.getPipelinedRegion(r).getOperators().contains(opId))
      .map(plan.getPipelinedRegion)
      .toSet
  }

  /**
    * A link's region is the region of the source operator of the link.
    */
  protected def getRegions(plan:PipelinedRegionPlan, link: LinkIdentity): Set[PipelinedRegion] = {
    execution.runningRegions
      .filter(r => plan.getPipelinedRegion(r).getOperators().contains(link.from))
      .map(plan.getPipelinedRegion)
      .toSet
  }

  // gets the ready regions that is not currently running
  protected def getNextSchedulingWork(plan:PipelinedRegionPlan): Set[PipelinedRegion]

  def startWorkflow(plan:PipelinedRegionPlan): Set[PipelinedRegion] = {
    val regions = getNextSchedulingWork(plan)
    if (regions.isEmpty) {
      throw new WorkflowRuntimeException(
        s"No first region is being scheduled"
      )
    }
    regions
  }

  def onWorkerCompletion(plan:PipelinedRegionPlan, workerId: ActorVirtualIdentity): Set[PipelinedRegion] = {
    val regions = getRegions(plan, workerId)
    regions.foreach(r => checkRegionCompleted(plan, r.id))
    getNextSchedulingWork(plan)
  }

  def onLinkCompletion(plan:PipelinedRegionPlan, link: LinkIdentity): Set[PipelinedRegion] = {
    val regions = getRegions(plan, link)
    regions.foreach(r =>
      execution.completedLinksOfRegion
        .getOrElseUpdate(r.id, new mutable.HashSet[LinkIdentity]())
        .add(link)
    )
    regions.foreach(r => checkRegionCompleted(plan, r.id))
    getNextSchedulingWork(plan)
  }

  def onTimeSlotExpired(plan:PipelinedRegionPlan): Set[PipelinedRegion] = {
    getNextSchedulingWork(plan)
  }

  def addToRunningRegions(regions: Set[PipelinedRegionIdentity], plan:PipelinedRegionPlan,  actorService: WorkflowActorService): Unit = {
    execution.runningRegions ++= regions
  }

  def removeFromRunningRegion(regions: Set[PipelinedRegionIdentity]): Unit = {
    execution.runningRegions --= regions
  }

  def getRunningRegions(): Set[PipelinedRegionIdentity] = execution.runningRegions.toSet

  def getCompletedRegions(): Set[PipelinedRegionIdentity] = execution.completedRegions.toSet

}
