package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorOccurredHandler.FatalErrorOccurred
import edu.uci.ics.amber.engine.common.amberexception.FatalError
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object FatalErrorOccurredHandler {
  final case class FatalErrorOccurred(e: FatalError, causedBy: ActorVirtualIdentity)
      extends ControlCommand[Unit]
}

/** Indicate a fatal error has occurred in the workflow
  *
  * possible sender: controller, worker
  */
trait FatalErrorOccurredHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: FatalErrorOccurred, sender) =>
    {
      // log the error to console
      logger.error("FatalErrorOccurred received", msg)

      //report to client
      sendToClient(msg)
    }
  }
}
