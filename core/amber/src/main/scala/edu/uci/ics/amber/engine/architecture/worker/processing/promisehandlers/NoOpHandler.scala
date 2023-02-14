package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import NoOpHandler.NoOp
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipConsoleLog, SkipFaultTolerance, SkipReply}

object NoOpHandler {
  final case class NoOp()
      extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply
      with SkipConsoleLog
}

trait NoOpHandler {
  this: DataProcessorRPCHandlerInitializer =>
  registerHandler { (msg: NoOp, sender) =>
    // do nothing
  }
}
