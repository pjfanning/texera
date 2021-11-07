package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.BreakpointTriggered
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LocalOperatorExceptionOccurredHandler.LocalOperatorExceptionOccurred
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.common.amberexception.LocalOperatorException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

object LocalOperatorExceptionOccurredHandler {
  final case class LocalOperatorExceptionOccurred(
      e: LocalOperatorException,
      causedBy: ActorVirtualIdentity
  ) extends ControlCommand[Unit]
}

/** indicate an exception thrown from the operator logic on a worker
  * we catch exception when calling:
  * 1. operator.processTuple
  * 2. operator.hasNext
  * 3. operator.Next
  * The triggeredTuple of this message will always be the current input tuple
  * note that this message will be sent for each faulted input tuple, so the frontend
  * need to update incrementally since there can be multiple faulted tuple
  * from different workers at the same time.
  *
  * possible sender: worker
  */
trait LocalOperatorExceptionOccurredHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: LocalOperatorExceptionOccurred, sender) =>
    {

      // get the operator where the worker caught the local operator exception
      val operator = workflow.getOperator(sender)
      operator.caughtLocalExceptions.put(sender, msg.e)

      // then pause the workflow
      execute(PauseWorkflow(), CONTROLLER)

      // report the faulted tuple to the frontend with the exception
      sendToClient(
        BreakpointTriggered(
          msg.e,
          workflow.getOperator(sender).id.operator
        )
      )
    }
  }
}
