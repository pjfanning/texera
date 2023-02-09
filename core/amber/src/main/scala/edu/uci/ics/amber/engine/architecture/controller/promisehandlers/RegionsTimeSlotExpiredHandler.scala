package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.{
  Controller,
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.RegionsTimeSlotExpiredHandler.RegionsTimeSlotExpired
import edu.uci.ics.amber.engine.architecture.scheduling.PipelinedRegion
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

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
  this: ControllerProcessor =>

  registerHandler { (msg: RegionsTimeSlotExpired, sender) =>
    {
      val notCompletedRegions =
        msg.regions.diff(scheduler.schedulingPolicy.getCompletedRegions())

      if (notCompletedRegions.subsetOf(scheduler.schedulingPolicy.getRunningRegions())) {
        scheduler.onTimeSlotExpired(notCompletedRegions, availableNodes).flatMap(_ => Future.Unit)
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
