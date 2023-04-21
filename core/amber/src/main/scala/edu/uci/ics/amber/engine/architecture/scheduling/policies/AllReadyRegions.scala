package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.collection.mutable
import scala.jdk.CollectionConverters.{asScalaSet, asScalaSetConverter}
import scala.util.control.Breaks.{break, breakable}

class AllReadyRegions(workflow: Workflow, execution:WorkflowExecution, regionsScheduleOrder: mutable.Buffer[PipelinedRegionIdentity]) extends SchedulingPolicy(workflow, execution) {

  override def getNextSchedulingWork(plan:PipelinedRegionPlan): Set[PipelinedRegion] = {
    val nextToSchedule: mutable.HashSet[PipelinedRegion] = new mutable.HashSet[PipelinedRegion]()
    breakable {
      while (regionsScheduleOrder.nonEmpty) {
        val nextRegionId = regionsScheduleOrder.head
        val nextRegion = plan.getPipelinedRegion(nextRegionId)
        val upstreamRegions =
          asScalaSet(plan.pipelinedRegionsDAG.getAncestors(nextRegion)).map(_.id)
        if (upstreamRegions.forall(execution.completedRegions.contains)) {
          assert(!execution.scheduledRegions.contains(nextRegionId))
          nextToSchedule.add(nextRegion)
          regionsScheduleOrder.remove(0)
          execution.scheduledRegions.add(nextRegionId)
        } else {
          break
        }
      }
    }

    nextToSchedule.toSet
  }
}
