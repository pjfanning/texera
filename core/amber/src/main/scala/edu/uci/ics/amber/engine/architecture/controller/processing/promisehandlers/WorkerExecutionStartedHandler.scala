package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowStatusUpdate
import WorkerExecutionStartedHandler.WorkerStateUpdated
import edu.uci.ics.amber.engine.architecture.controller.processing.{
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object WorkerExecutionStartedHandler {
  final case class WorkerStateUpdated(state: WorkerState) extends ControlCommand[Unit]
}

/** indicate the state change of a worker
  *
  * possible sender: worker
  */
trait WorkerExecutionStartedHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: WorkerStateUpdated, sender) =>
    {
      // set the state
      cp.execution.getOperatorExecution(sender).getWorkerInfo(sender).state = msg.state
      sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
    }
  }
}
