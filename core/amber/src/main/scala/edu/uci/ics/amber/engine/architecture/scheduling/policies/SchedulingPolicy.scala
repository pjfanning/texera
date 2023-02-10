package edu.uci.ics.amber.engine.architecture.scheduling.policies

import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity, OperatorIdentity}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.collection.mutable
import scala.collection.JavaConverters._

object SchedulingPolicy {
  def createPolicy(
      policyName: String,
      workflow: Workflow,
      ctx: ActorContext
  ): SchedulingPolicy = {
    if (policyName.equals("single-ready-region")) {
      new SingleReadyRegion(workflow)
    } else if (policyName.equals("all-ready-regions")) {
      new AllReadyRegions(workflow)
    } else if (policyName.equals("single-ready-region-time-interleaved")) {
      new SingleReadyRegionTimeInterleaved(workflow, ctx)
    } else {
      throw new WorkflowRuntimeException(s"Unknown scheduling policy name")
    }
  }
}

abstract class SchedulingPolicy(workflow: Workflow) {

  @transient
  protected var execution: WorkflowExecution = _

  def attachToExecution(execution: WorkflowExecution): Unit = {
    this.execution = execution
  }

  protected def isRegionCompleted(regionId: PipelinedRegionIdentity): Boolean = {
    val region = workflow.physicalPlan.getPipelinedRegion(regionId)
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

  protected def checkRegionCompleted(region: PipelinedRegionIdentity): Unit = {
    if (isRegionCompleted(region)) {
      execution.runningRegions.remove(region)
      execution.completedRegions.add(region)
    }
  }

  protected def getRegion(workerId: ActorVirtualIdentity): Option[PipelinedRegion] = {
    val opId = workflow.getOperator(workerId).id
    execution.runningRegions.find(r => workflow.physicalPlan.getPipelinedRegion(r).getOperators().contains(opId)).map(workflow.physicalPlan.getPipelinedRegion)
  }

  /**
    * A link's region is the region of the source operator of the link.
    */
  protected def getRegion(link: LinkIdentity): Option[PipelinedRegion] = {
    execution.runningRegions.find(r => workflow.physicalPlan.getPipelinedRegion(r).getOperators().contains(link.from)).map(workflow.physicalPlan.getPipelinedRegion)
  }

  // gets the ready regions that is not currently running
  protected def getNextSchedulingWork(): Set[PipelinedRegion]

  def startWorkflow(): Set[PipelinedRegion] = {
    val regions = getNextSchedulingWork()
    if (regions.isEmpty) {
      throw new WorkflowRuntimeException(
        s"No first region is being scheduled"
      )
    }
    regions
  }

  def onWorkerCompletion(workerId: ActorVirtualIdentity): Set[PipelinedRegion] = {
    val region = getRegion(workerId)
    if (region.isEmpty) {
      throw new WorkflowRuntimeException(
        s"WorkflowScheduler: Worker ${workerId} completed from a non-running region"
      )
    } else {
      checkRegionCompleted(region.get.id)
    }
    getNextSchedulingWork()
  }

  def onLinkCompletion(link: LinkIdentity): Set[PipelinedRegion] = {
    val region = getRegion(link)
    if (region.isEmpty) {
      throw new WorkflowRuntimeException(
        s"WorkflowScheduler: Link ${link.toString} completed from a non-running region"
      )
    } else {
      val completedLinks =
        execution.completedLinksOfRegion.getOrElseUpdate(
          region.get.id,
          new mutable.HashSet[LinkIdentity]()
        )
      completedLinks.add(link)
      execution.completedLinksOfRegion(region.get.id) = completedLinks
      checkRegionCompleted(region.get.id)
    }
    getNextSchedulingWork()
  }

  def onTimeSlotExpired(): Set[PipelinedRegion] = {
    getNextSchedulingWork()
  }

  def addToRunningRegions(regions: Set[PipelinedRegionIdentity]): Unit = {
    execution.runningRegions ++= regions
  }

  def removeFromRunningRegion(regions: Set[PipelinedRegionIdentity]): Unit = {
    execution.runningRegions --= regions
  }

  def getRunningRegions(): Set[PipelinedRegionIdentity] = execution.runningRegions.toSet

  def getCompletedRegions(): Set[PipelinedRegionIdentity] = execution.completedRegions.toSet

}
