package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.rpc.{AsyncRPCContext, EmptyRequest}
import edu.uci.ics.amber.engine.architecture.rpc.WorkerStateResponse
import edu.uci.ics.amber.engine.architecture.worker.{DataProcessorRPCHandlerInitializer, UserPause}
import edu.uci.ics.amber.engine.architecture.worker.WorkerState.{PAUSED, READY, RUNNING}

trait PauseHandler {
  this: DataProcessorRPCHandlerInitializer =>

  override def pauseWorker(
      request: EmptyRequest,
      ctx: AsyncRPCContext
  ): Future[WorkerStateResponse] = {
    if (dp.stateManager.confirmState(RUNNING, READY)) {
      dp.pauseManager.pause(UserPause)
      dp.stateManager.transitTo(PAUSED)
    }
    WorkerStateResponse(dp.stateManager.getCurrentState)
  }

}
