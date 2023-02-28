package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipConsoleLog, SkipFaultTolerance, SkipReply}

object BackpressureHandler {
  final case class Backpressure(enableBackpressure: Boolean)
      extends ControlCommand[Unit]
      with SkipReply
      with SkipConsoleLog
      with SkipFaultTolerance
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait BackpressureHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: Backpressure, _) =>
    if (msg.enableBackpressure) {
      dp.pauseManager.pause(BackpressurePause)
    } else {
      dp.pauseManager.resume(BackpressurePause)
    }
  }

}
