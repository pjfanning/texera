package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{DataProcessor, DataProcessorRPCHandlerInitializer, PauseType}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object GetReplayAlignmentHandler {
  final case class GetReplayAlignment() extends ControlCommand[(Long, Int, Int)]
}

trait GetReplayAlignmentHandler {
  this: DataProcessor =>

  registerHandler { (msg: GetReplayAlignment, _) =>
    println("record total step at " + totalValidStep + 1)
    var estimatedCheckpointTime = 0
    var estimatedStateLoadTime = 0
    operator match {
      case support: CheckpointSupport =>
        estimatedCheckpointTime = support.getEstimatedCheckpointTime
        estimatedStateLoadTime = support.getEstimatedStateLoadTime
      case _ =>
    }
    (totalValidStep + 1, estimatedCheckpointTime, estimatedStateLoadTime)
  }

}
