package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{
  WorkflowStateUpdate,
  WorkflowStatusUpdate
}
import ResumeHandler.ResumeWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ResumeHandler.ResumeWorker
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
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: ResumeWorkflow, sender) =>
    {

      workflowStartTimeStamp += System.currentTimeMillis() - workflowPauseStartTime
      // send all workers resume
      // resume message has no effect on non-paused workers
      Future
        .collect(cp.execution.getAllWorkers.map { worker =>
          send(ResumeWorker(), worker).map { ret =>
            cp.execution.getOperatorExecution(worker).getWorkerInfo(worker).state = ret
          }
        }.toSeq)
        .map { _ =>
          // update frontend status
          sendToClient(WorkflowStateUpdate(RUNNING))
          sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
          enableStatusUpdate() //re-enabled it since it is disabled in pause
          enableMonitoring()
          enableSkewHandling()
        }
    }
  }
}
