package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowStateUpdate,
  WorkflowStatusUpdate
}
import edu.uci.ics.amber.engine.architecture.controller.ControllerProcessor
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.StartWorkflowHandler.StartWorkflow
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING

import scala.collection.mutable

object StartWorkflowHandler {
  final case class StartWorkflow() extends ControlCommand[Unit]
}

/** start the workflow by starting the source workers
  * note that this SHOULD only be called once per workflow
  *
  * possible sender: client
  */
trait StartWorkflowHandler {
  this: ControllerProcessor =>

  registerHandler { (msg: StartWorkflow, sender) =>
    {
      scheduler
        .startWorkflow(availableNodes)
        .map(_ => {
          sendToClient(WorkflowStateUpdate(RUNNING))
          enableStatusUpdate()
          enableMonitoring()
          enableSkewHandling()
        })
    }
  }
}
