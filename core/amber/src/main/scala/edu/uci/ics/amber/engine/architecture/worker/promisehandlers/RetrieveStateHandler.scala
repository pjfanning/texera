package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.RetrieveStateHandler.RetrieveState
import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object RetrieveStateHandler {
  final case class RetrieveState(retrieveFunc: DataProcessor => String)
      extends ControlCommand[String]
}

trait RetrieveStateHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: RetrieveState, sender) =>
    msg.retrieveFunc(dp)
  }

}
