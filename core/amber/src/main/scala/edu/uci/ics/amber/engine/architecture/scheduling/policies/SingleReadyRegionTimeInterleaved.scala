package edu.uci.ics.amber.engine.architecture.scheduling.policies

import edu.uci.ics.amber.engine.architecture.common.WorkflowActorService
import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.RegionsTimeSlotExpiredHandler.RegionsTimeSlotExpired
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

import scala.collection.mutable
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.jdk.CollectionConverters.asScalaSet
import scala.util.control.Breaks.{break, breakable}

class SingleReadyRegionTimeInterleaved(
    controlProcessor: ControlProcessor,
    regionsScheduleOrder: mutable.Buffer[PipelinedRegionIdentity]
) extends SchedulingPolicy(controlProcessor) {

  var currentlyExecutingRegions = new mutable.LinkedHashSet[PipelinedRegionIdentity]()

  override def checkRegionCompleted(
      pipelinedRegionPlan: PipelinedRegionPlan,
      region: PipelinedRegionIdentity
  ): Unit = {
    super.checkRegionCompleted(pipelinedRegionPlan, region)
    if (isRegionCompleted(pipelinedRegionPlan, region)) {
      currentlyExecutingRegions.remove(region)
    }
  }

  override def onWorkerCompletion(
      plan: PipelinedRegionPlan,
      workerId: ActorVirtualIdentity
  ): Set[PipelinedRegion] = {
    val regions = getRegions(plan, workerId)
    regions.foreach(r => checkRegionCompleted(plan, r.id))
    if (regions.exists(r => isRegionCompleted(plan, r.id))) {
      getNextSchedulingWork(plan)
    } else {
      Set()
    }
  }

  override def onLinkCompletion(
      plan: PipelinedRegionPlan,
      linkId: LinkIdentity
  ): Set[PipelinedRegion] = {
    val regions = getRegions(plan, linkId)
    regions.foreach(r =>
      getExecution.completedLinksOfRegion
        .getOrElseUpdate(r.id, new mutable.HashSet[LinkIdentity]())
        .add(linkId)
    )
    regions.foreach(r => checkRegionCompleted(plan, r.id))
    if (regions.exists(r => isRegionCompleted(plan, r.id))) {
      getNextSchedulingWork(plan)
    } else {
      Set()
    }
  }

  override def getNextSchedulingWork(plan: PipelinedRegionPlan): Set[PipelinedRegion] = {
    breakable {
      while (regionsScheduleOrder.nonEmpty) {
        val nextRegionId = regionsScheduleOrder.head
        val nextRegion = plan.getPipelinedRegion(nextRegionId)
        val upstreamRegions =
          asScalaSet(plan.pipelinedRegionsDAG.getAncestors(nextRegion)).map(_.id)
        if (upstreamRegions.forall(getExecution.completedRegions.contains)) {
          assert(!getExecution.scheduledRegions.contains(nextRegionId))
          currentlyExecutingRegions.add(nextRegionId)
          regionsScheduleOrder.remove(0)
          getExecution.scheduledRegions.add(nextRegionId)
        } else {
          break
        }
      }
    }
    if (currentlyExecutingRegions.nonEmpty) {
      val nextToSchedule = currentlyExecutingRegions.head
      if (!getExecution.runningRegions.contains(nextToSchedule)) {
        // if `nextToSchedule` is not running right now.
        currentlyExecutingRegions.remove(nextToSchedule) // remove first element
        currentlyExecutingRegions.add(nextToSchedule) // add to end of list
        return Set(plan.getPipelinedRegion(nextToSchedule))
      }
    }
    Set()

  }

  override def addToRunningRegions(
      regions: Set[PipelinedRegionIdentity],
      plan: PipelinedRegionPlan,
      actorService: WorkflowActorService
  ): Unit = {
    regions.foreach(r => getExecution.runningRegions.add(r))
    actorService.scheduleOnce(
      FiniteDuration.apply(Constants.timeSlotExpirationDurationInMs, MILLISECONDS),
      () =>
        actorService.self ! ControlInvocation(
          RegionsTimeSlotExpired(regions.map(plan.getPipelinedRegion))
        )
    )
  }
}
