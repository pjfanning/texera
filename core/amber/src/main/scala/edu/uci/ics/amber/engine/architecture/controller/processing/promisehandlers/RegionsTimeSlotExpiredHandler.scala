package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import RegionsTimeSlotExpiredHandler.RegionsTimeSlotExpired
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.scheduling.PipelinedRegion
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

object RegionsTimeSlotExpiredHandler {
  final case class RegionsTimeSlotExpired(regions: Set[PipelinedRegion])
      extends ControlCommand[Unit]
      with SkipReply
}

/** Indicate that the time slot for a reason is up and the execution of the regions needs to be paused
  *
  * possible sender: controller (scheduler)
  */
trait RegionsTimeSlotExpiredHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: RegionsTimeSlotExpired, sender) =>
    {
      val notCompletedRegions =
        msg.regions.diff(
          cp.schedulingPolicy
            .getCompletedRegions()
            .map(cp.pipelinedRegionPlan.getPipelinedRegion)
        )

      if (
        notCompletedRegions.subsetOf(
          cp.schedulingPolicy
            .getRunningRegions()
            .map(cp.pipelinedRegionPlan.getPipelinedRegion)
        )
      ) {
        cp.scheduler
          .onTimeSlotExpired(notCompletedRegions)
          .flatMap(_ => Future.Unit)
      } else {
        if (notCompletedRegions.nonEmpty) {
          // This shouldn't happen because the timer starts only after the regions have started
          // running. The only other possibility is that the region has completed which we have
          // checked above.
          throw new WorkflowRuntimeException(
            "The regions' time slot expired but they are not running yet."
          )
        }
        Future(())
      }
    }
  }
}
