package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object BackpressureHandler {
  final case class Backpressure(enableBackpressure: Boolean) extends ControlCommand[Unit]
}

/** Get queue and other resource usage of this worker
  *
  * possible sender: controller(by ControllerInitiateMonitoring)
  */
trait BackpressureHandler {
  this: DataProcessor =>

  registerHandler { (msg: Backpressure, _) =>
    if (msg.enableBackpressure) {
      pauseManager.recordRequest(PauseType.BackpressurePause, true)
    } else {
      pauseManager.recordRequest(PauseType.BackpressurePause, false)
      if (!pauseManager.isPaused()) {
      }
    }
  }

}
