package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.controlcommands.PauseWorkerV2
import edu.uci.ics.amber.engine.architecture.worker.{UserPause, WorkerAsyncRPCService}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, READY, RUNNING}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object PauseHandler {

  final case class PauseWorker() extends ControlCommand[WorkerState]
}

trait PauseHandler {
  this: WorkerAsyncRPCService =>

  registerHandler { (pause: PauseWorker, sender) =>
    if (stateManager.confirmState(RUNNING, READY)) {
      pauseManager.pause(UserPause)
      stateManager.transitTo(PAUSED)
    }
    stateManager.getCurrentState
  }
}
