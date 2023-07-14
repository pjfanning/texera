package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCService
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.EndMarker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{READY, RUNNING}
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.{StartWorkerRequest, StartWorkerResponse, WorkerCallServiceGrpc}
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerCallServiceGrpc.WorkerCallService
import edu.uci.ics.amber.engine.common.ISourceOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.util.SOURCE_STARTER_ACTOR

import scala.concurrent.Future

object StartHandler {
  final case class StartWorker() extends ControlCommand[WorkerState]
}

trait StartHandler extends WorkerCallServiceGrpc.WorkerCallService{
  this: WorkerAsyncRPCService =>

  override def startWorker(request: StartWorkerRequest): Future[StartWorkerResponse] = {
    if (operator.isInstanceOf[ISourceOperatorExecutor]) {
      stateManager.assertState(READY)
      stateManager.transitTo(RUNNING)
      internalQueue.appendElement(EndMarker(SOURCE_STARTER_ACTOR))
      stateManager.getCurrentState
    } else {
      throw new WorkflowRuntimeException(
        s"non-source worker $actorId received unexpected StartWorker!"
      )
    }
  }

}
