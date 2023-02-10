package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.scheduling.PipelinedRegion

class SingleReadyRegion(workflow: Workflow) extends SchedulingPolicy(workflow) {

  override def getNextSchedulingWork(): Set[PipelinedRegion] = {
    if (
      (execution.scheduledRegions.isEmpty ||
      execution.scheduledRegions.forall(
        execution.completedRegions.contains
      )) && execution.regionsScheduleOrder.nonEmpty
    ) {
      val nextRegion = execution.regionsScheduleOrder.head
      execution.regionsScheduleOrder.remove(0)
      assert(!execution.scheduledRegions.contains(nextRegion))
      execution.scheduledRegions.add(nextRegion)
      return Set(workflow.physicalPlan.getPipelinedRegion(nextRegion))
    }
    Set()
  }
}
