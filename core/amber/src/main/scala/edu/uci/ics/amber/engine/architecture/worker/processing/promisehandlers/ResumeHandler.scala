package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import ResumeHandler.ResumeWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object ResumeHandler {
  final case class ResumeWorker() extends ControlCommand[WorkerState]
}

trait ResumeHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: ResumeWorker, sender) =>
    if (dp.stateManager.getCurrentState == PAUSED) {
      dp.pauseManager.recordRequest(PauseType.UserPause, false)
      dp.stateManager.transitTo(RUNNING)
      dp.internalQueue.setDataQueueEnabled(true)
    }
    dp.stateManager.getCurrentState
  }

}
