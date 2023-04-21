package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.collection.mutable

class SingleReadyRegion(workflow: Workflow,  execution:WorkflowExecution, regionsScheduleOrder: mutable.Buffer[PipelinedRegionIdentity]) extends SchedulingPolicy(workflow, execution) {

  override def getNextSchedulingWork(plan:PipelinedRegionPlan): Set[PipelinedRegion] = {
    if (
      (execution.scheduledRegions.isEmpty ||
      execution.scheduledRegions.forall(
        execution.completedRegions.contains
      )) && regionsScheduleOrder.nonEmpty
    ) {
      val nextRegion = regionsScheduleOrder.head
      regionsScheduleOrder.remove(0)
      assert(!execution.scheduledRegions.contains(nextRegion))
      execution.scheduledRegions.add(nextRegion)
      return Set(plan.getPipelinedRegion(nextRegion))
    }
    Set()
  }
}
