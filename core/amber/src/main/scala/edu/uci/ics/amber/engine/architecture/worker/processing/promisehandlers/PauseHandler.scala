package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, READY, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object PauseHandler {

  final case class PauseWorker() extends ControlCommand[WorkerState]
}

trait PauseHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (pause: PauseWorker, sender) =>
    if (dp.stateManager.confirmState(RUNNING, READY)) {
      dp.pauseManager.recordRequest(PauseType.UserPause, true)
      dp.stateManager.transitTo(PAUSED)
      dp.internalQueue.setDataQueueEnabled(false)
      dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
    }
    dp.stateManager.getCurrentState
  }
}
