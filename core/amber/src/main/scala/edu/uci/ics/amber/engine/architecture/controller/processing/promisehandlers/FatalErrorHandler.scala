package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.Controller
import FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object FatalErrorHandler {
  final case class FatalError(e: Throwable) extends ControlCommand[Unit]
}

/** Indicate a fatal error has occurred in the workflow
  *
  * possible sender: controller, worker
  */
trait FatalErrorHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: FatalError, sender) =>
    {
      // log the error to console
      logger.error("FatalError received", msg)

      //report to client
      sendToClient(msg)
    }
  }
}
