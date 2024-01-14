package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowPaused,
  WorkflowStatusUpdate
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object PauseHandler {

  final case class PauseWorkflow() extends ControlCommand[Unit]
}

/** pause the entire workflow
  *
  * possible sender: client, controller
  */
trait PauseHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: PauseWorkflow, sender) =>
    {
      cp.controllerTimerService.disableStatusUpdate() // to be enabled in resume
      cp.controllerTimerService.disableMonitoring()
      cp.controllerTimerService.disableSkewHandling()
      Future
        .collect(
          cp.executionState.getAllBuiltWorkers
            .map(workerId => send(PauseWorker(), workerId).map(ret => (workerId, ret)))
            .toSeq
        )
        .map { ret =>
          ret.foreach {
            case (worker, value) =>
              val info = cp.executionState.getOperatorExecution(worker).getWorkerInfo(worker)
              info.state = value
          }
          execute(ControllerInitiateQueryStatistics(), sender).map { ret =>
            // update frontend workflow status
            sendToClient(WorkflowStatusUpdate(cp.executionState.getWorkflowStatus))
            sendToClient(WorkflowPaused())
            logger.info(s"workflow paused")
          }
        }
        .unit
    }
  }
}
