package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.{ControllerAsyncRPCHandlerInitializer, ControllerState}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LinkWorkflowHandler.LinkWorkflow
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AddOutputPolicyHandler.AddOutputPolicy
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{CommandCompleted, ControlCommand}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager.Ready
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object LinkWorkflowHandler {
  final case class LinkWorkflow() extends ControlCommand[CommandCompleted]
}


trait LinkWorkflowHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: LinkWorkflow, sender) => {
    Future
      .collect(controller.workflow.getAllLinks.flatMap { link =>
        link.getPolicies.flatMap {
          case (from, policy, tos) =>
            // send messages to sender worker
            Seq(send(AddOutputPolicy(policy), from))
        }
      }.toSeq)
      .map { ret =>
        controller.workflow.getAllOperators.foreach(_.setAllWorkerState(Ready))
        if (controller.eventListener.workflowStatusUpdateListener != null) {
          controller.eventListener.workflowStatusUpdateListener
            .apply(WorkflowStatusUpdate(controller.workflow.getWorkflowStatus))
        }
        // for testing, report ready state to parent
        controller.context.parent ! ControllerState.Ready
        controller.context.become(controller.running)
        controller.unstashAll()
        CommandCompleted()
      }
  }}
}
