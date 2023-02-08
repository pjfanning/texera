package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer,
  PauseType
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object GetReplayAlignmentHandler {
  final case class GetReplayAlignment() extends ControlCommand[Long]
}

trait GetReplayAlignmentHandler {
  this: DataProcessor =>

  registerHandler { (msg: GetReplayAlignment, _) =>
    println("record total step at " + totalValidStep+1)
    totalValidStep+1
  }

}
