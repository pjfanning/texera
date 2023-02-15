package edu.uci.ics.amber.engine.architecture.scheduling.policies

import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.RegionsTimeSlotExpiredHandler.RegionsTimeSlotExpired
import edu.uci.ics.amber.engine.architecture.scheduling.{PipelinedRegion, PipelinedRegionIdentity}
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}

import scala.collection.mutable
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.jdk.CollectionConverters.asScalaSet
import scala.util.control.Breaks.{break, breakable}

class SingleReadyRegionTimeInterleaved(
    workflow: Workflow,
    ctx: ActorContext
) extends SchedulingPolicy(workflow) {

  // CANNOT BE INCLUDED IN CHECKPOINT

  var currentlyExecutingRegions = new mutable.LinkedHashSet[PipelinedRegionIdentity]()

  override def checkRegionCompleted(region: PipelinedRegionIdentity): Unit = {
    super.checkRegionCompleted(region)
    if (isRegionCompleted(region)) {
      currentlyExecutingRegions.remove(region)
    }
  }

  override def onWorkerCompletion(workerId: ActorVirtualIdentity): Set[PipelinedRegion] = {
    val regions = getRegions(workerId)
    regions.foreach(r => checkRegionCompleted(r))
    if (regions.exists(r => isRegionCompleted(r))) {
      getNextSchedulingWork()
    } else {
      Set()
    }
  }

  override def onLinkCompletion(linkId: LinkIdentity): Set[PipelinedRegion] = {
    val regions = getRegions(linkId)
    regions.foreach(r => completedLinksOfRegion.addBinding(r, linkId))
    regions.foreach(r => checkRegionCompleted(r))
    if (regions.exists(r => isRegionCompleted(r))) {
      getNextSchedulingWork()
    } else {
      Set()
    }
  }

  override def getNextSchedulingWork(): Set[PipelinedRegion] = {
    breakable {
      while (execution.regionsScheduleOrder.nonEmpty) {
        val nextRegionId = execution.regionsScheduleOrder.head
        val nextRegion = workflow.physicalPlan.getPipelinedRegion(nextRegionId)
        val upstreamRegions =
          asScalaSet(workflow.physicalPlan.pipelinedRegionsDAG.getAncestors(nextRegion)).map(_.id)
        if (upstreamRegions.forall(execution.completedRegions.contains)) {
          assert(!execution.scheduledRegions.contains(nextRegionId))
          currentlyExecutingRegions.add(nextRegionId)
          execution.regionsScheduleOrder.remove(0)
          execution.scheduledRegions.add(nextRegionId)
        } else {
          break
        }
      }
    }
    if (currentlyExecutingRegions.nonEmpty) {
      val nextToSchedule = currentlyExecutingRegions.head
      if (!execution.runningRegions.contains(nextToSchedule)) {
        // if `nextToSchedule` is not running right now.
        currentlyExecutingRegions.remove(nextToSchedule) // remove first element
        currentlyExecutingRegions.add(nextToSchedule) // add to end of list
        return Set(workflow.physicalPlan.getPipelinedRegion(nextToSchedule))
      }
    }
    Set()

  }

  override def addToRunningRegions(regions: Set[PipelinedRegionIdentity]): Unit = {
    regions.foreach(r => execution.runningRegions.add(r))
    ctx.system.scheduler.scheduleOnce(
      FiniteDuration.apply(Constants.timeSlotExpirationDurationInMs, MILLISECONDS),
      ctx.self,
      ControlInvocation(
        RegionsTimeSlotExpired(regions.map(workflow.physicalPlan.getPipelinedRegion))
      )
    )(ctx.dispatcher)
  }
}
