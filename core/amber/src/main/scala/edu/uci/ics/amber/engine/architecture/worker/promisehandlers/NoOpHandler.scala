package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.NoOpHandler.NoOp
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{
  ControlCommand,
  SkipConsoleLog,
  SkipFaultTolerance,
  SkipReply
}

object NoOpHandler {
  final case class NoOp()
      extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply
      with SkipConsoleLog
}

trait NoOpHandler {
  this: DataProcessor =>
  registerHandler { (msg: NoOp, sender) =>
    // do nothing
  }
}
