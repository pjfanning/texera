package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.collection.mutable
import scala.jdk.CollectionConverters.asScalaSet
import scala.util.control.Breaks.{break, breakable}

class AllReadyRegions(
    controlProcessor: ControlProcessor,
    regionsScheduleOrder: mutable.Buffer[PipelinedRegionIdentity]
) extends SchedulingPolicy(controlProcessor) {

  override def getNextSchedulingWork(plan: PipelinedRegionPlan): Set[PipelinedRegion] = {
    val nextToSchedule: mutable.HashSet[PipelinedRegion] = new mutable.HashSet[PipelinedRegion]()
    breakable {
      while (regionsScheduleOrder.nonEmpty) {
        val nextRegionId = regionsScheduleOrder.head
        val nextRegion = plan.getPipelinedRegion(nextRegionId)
        val upstreamRegions =
          asScalaSet(plan.pipelinedRegionsDAG.getAncestors(nextRegion)).map(_.id)
        if (upstreamRegions.forall(getExecution.completedRegions.contains)) {
          assert(!getExecution.scheduledRegions.contains(nextRegionId))
          nextToSchedule.add(nextRegion)
          regionsScheduleOrder.remove(0)
          getExecution.scheduledRegions.add(nextRegionId)
        } else {
          break
        }
      }
    }

    nextToSchedule.toSet
  }
}
