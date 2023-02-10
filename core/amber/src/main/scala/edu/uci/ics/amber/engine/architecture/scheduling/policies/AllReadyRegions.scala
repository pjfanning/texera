package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.scheduling.PipelinedRegion

import scala.collection.mutable
import scala.jdk.CollectionConverters.{asScalaSet, asScalaSetConverter}
import scala.util.control.Breaks.{break, breakable}

class AllReadyRegions(workflow: Workflow) extends SchedulingPolicy(workflow) {

  override def getNextSchedulingWork(): Set[PipelinedRegion] = {
    val nextToSchedule: mutable.HashSet[PipelinedRegion] = new mutable.HashSet[PipelinedRegion]()
    breakable {
      while (execution.regionsScheduleOrder.nonEmpty) {
        val nextRegionId = execution.regionsScheduleOrder.head
        val nextRegion = workflow.physicalPlan.getPipelinedRegion(nextRegionId)
        val upstreamRegions =
          asScalaSet(workflow.physicalPlan.pipelinedRegionsDAG.getAncestors(nextRegion)).map(_.id)
        if (upstreamRegions.forall(execution.completedRegions.contains)) {
          assert(!execution.scheduledRegions.contains(nextRegionId))
          nextToSchedule.add(nextRegion)
          execution.regionsScheduleOrder.remove(0)
          execution.scheduledRegions.add(nextRegionId)
        } else {
          break
        }
      }
    }

    nextToSchedule.toSet
  }
}
