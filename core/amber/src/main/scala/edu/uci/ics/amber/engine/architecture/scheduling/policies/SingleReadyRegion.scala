package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.collection.mutable

class SingleReadyRegion(
    controlProcessor: ControlProcessor,
    regionsScheduleOrder: mutable.Buffer[PipelinedRegionIdentity]
) extends SchedulingPolicy(controlProcessor) {

  override def getNextSchedulingWork(plan: PipelinedRegionPlan): Set[PipelinedRegion] = {
    if (
      (getExecution.scheduledRegions.isEmpty ||
      getExecution.scheduledRegions.forall(
        getExecution.completedRegions.contains
      )) && regionsScheduleOrder.nonEmpty
    ) {
      val nextRegion = regionsScheduleOrder.head
      regionsScheduleOrder.remove(0)
      assert(!getExecution.scheduledRegions.contains(nextRegion))
      getExecution.scheduledRegions.add(nextRegion)
      return Set(plan.getPipelinedRegion(nextRegion))
    }
    Set()
  }
}
