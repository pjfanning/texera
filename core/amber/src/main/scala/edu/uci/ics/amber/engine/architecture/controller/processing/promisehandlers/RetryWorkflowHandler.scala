package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import ResumeHandler.ResumeWorkflow
import RetryWorkflowHandler.RetryWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers.ReplayCurrentTupleHandler.ReplayCurrentTuple
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

object RetryWorkflowHandler {
  final case class RetryWorkflow() extends ControlCommand[Unit]
}

/** retry the execution of the entire workflow
  *
  * possible sender: controller, client
  */
trait RetryWorkflowHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: RetryWorkflow, sender) =>
    {
      // if it is a PythonWorker, prepare for retry
      // retry message has no effect on completed workers
      Future
        .collect(
          cp.workflow.getAllOperators
            // find workers who received local operator exception
            .flatMap(operator =>
              cp.execution.getOperatorExecution(operator.id).caughtLocalExceptions.keys
            )
            // currently only support retry for PythonWorker, thus filter them
            .filter(worker => cp.workflow.getPythonWorkers.toSeq.contains(worker))
            .map(worker => send(ReplayCurrentTuple(), worker))
            .toSeq
        )
        .unit

      // resume all workers
      execute(ResumeWorkflow(), CONTROLLER)
    }
  }
}
