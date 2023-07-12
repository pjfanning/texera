package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.WorkflowStateUpdate
import StartWorkflowHandler.StartWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

object StartWorkflowHandler {
  final case class StartWorkflow() extends ControlCommand[Unit]
}

/** start the workflow by starting the source workers
  * note that this SHOULD only be called once per workflow
  *
  * possible sender: client
  */
trait StartWorkflowHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: StartWorkflow, sender) =>
    {
      cp.scheduler
        .startWorkflow()
        .map(_ => {
          sendToClient(WorkflowStateUpdate(RUNNING))
          enableStatusUpdate()
          enableMonitoring()
          enableSkewHandling()
        })
    }
  }
}
