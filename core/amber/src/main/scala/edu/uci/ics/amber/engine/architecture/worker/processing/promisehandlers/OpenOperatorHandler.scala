package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import OpenOperatorHandler.OpenOperator
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object OpenOperatorHandler {

  final case class OpenOperator() extends ControlCommand[Unit]
}

trait OpenOperatorHandler {
  this: DataProcessorRPCHandlerInitializer =>
  registerHandler { (openOperator: OpenOperator, sender) =>
    dp.operator.open()
  }
}
