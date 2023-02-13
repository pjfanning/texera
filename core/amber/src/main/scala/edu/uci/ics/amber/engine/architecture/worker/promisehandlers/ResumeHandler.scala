package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessor,
  PauseType
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeHandler.ResumeWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object ResumeHandler {
  final case class ResumeWorker() extends ControlCommand[WorkerState]
}

trait ResumeHandler {
  this: DataProcessor =>

  registerHandler { (msg: ResumeWorker, sender) =>
    if (stateManager.getCurrentState == PAUSED) {
      pauseManager.recordRequest(PauseType.UserPause, false)
      stateManager.transitTo(RUNNING)
      internalQueue.setDataQueueEnabled(true)
    }
    stateManager.getCurrentState
  }

}
