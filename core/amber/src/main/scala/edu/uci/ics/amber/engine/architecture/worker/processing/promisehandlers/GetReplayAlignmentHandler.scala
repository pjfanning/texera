package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object GetReplayAlignmentHandler {
  final case class GetReplayAlignment() extends ControlCommand[(Long, Int, Int)]
}

trait GetReplayAlignmentHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: GetReplayAlignment, _) =>
    println("record total step at " + dp.totalValidStep + 1)
    var estimatedCheckpointTime = 0
    var estimatedStateLoadTime = 0
    dp.operator match {
      case support: CheckpointSupport =>
        estimatedCheckpointTime = support.getEstimatedCheckpointTime
        estimatedStateLoadTime = support.getEstimatedStateLoadTime
      case _ =>
    }
    (dp.totalValidStep + 1, estimatedCheckpointTime, estimatedStateLoadTime)
  }

}
