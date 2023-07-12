package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  BackpressurePause,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

object BackpressureHandler {
  final case class Backpressure(enableBackpressure: Boolean)
      extends ControlCommand[Unit]
      with SkipReply
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait BackpressureHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: Backpressure, _) =>
    if (msg.enableBackpressure) {
      logger.info(s"$actorId is paused by backpressure")
      dp.pauseManager.pause(BackpressurePause)
    } else {
      logger.info(s"$actorId is resumed by backpressure")
      dp.pauseManager.resume(BackpressurePause)
    }
  }

}
