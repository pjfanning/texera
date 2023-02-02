package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{PauseType, WorkerAsyncRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object GetReplayAlignmentHandler {
  final case class GetReplayAlignment() extends ControlCommand[Long]
}


trait GetReplayAlignmentHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: GetReplayAlignment, _) =>
    println("record total step at "+dataProcessor.totalValidStep)
    dataProcessor.totalValidStep+1
  }

}

