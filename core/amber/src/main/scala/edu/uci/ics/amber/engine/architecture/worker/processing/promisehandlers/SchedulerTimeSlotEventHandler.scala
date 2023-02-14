package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import SchedulerTimeSlotEventHandler.SchedulerTimeSlotEvent
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object SchedulerTimeSlotEventHandler {
  final case class SchedulerTimeSlotEvent(timeSlotExpired: Boolean) extends ControlCommand[Unit]
}

/** Time slot start/expiration message
  *
  * possible sender: controller(by scheduler)
  */
trait SchedulerTimeSlotEventHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: SchedulerTimeSlotEvent, _) =>
    if (msg.timeSlotExpired) {
      dp.pauseManager.recordRequest(PauseType.SchedulerTimeSlotExpiredPause, true)
    } else {
      dp.pauseManager.recordRequest(PauseType.SchedulerTimeSlotExpiredPause, false)
    }
  }

}
