package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowStateUpdate, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerAsyncRPCHandlerInitializer, ControllerProcessor}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeHandler.ResumeWorker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

object ResumeHandler {
  final case class ResumeWorkflow() extends ControlCommand[Unit]
}

/** resume the entire workflow
  *
  * possible sender: controller, client
  */
trait ResumeHandler {
  this: ControllerProcessor =>

  registerHandler { (msg: ResumeWorkflow, sender) =>
    {

      // send all workers resume
      // resume message has no effect on non-paused workers
      Future
        .collect(execution.getAllWorkers.map { worker =>
          send(ResumeWorker(), worker).map { ret =>
            execution.getOperatorExecution(worker).getWorkerInfo(worker).state = ret
          }
        }.toSeq)
        .map { _ =>
          // update frontend status
          sendToClient(WorkflowStateUpdate(RUNNING))
          sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))
          enableStatusUpdate() //re-enabled it since it is disabled in pause
          enableMonitoring()
          enableSkewHandling()
        }
    }
  }
}
