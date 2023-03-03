package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import GetReplayAlignmentHandler.{GetReplayAlignment, ReplayAlignmentInfo}
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object GetReplayAlignmentHandler {

  final case class ReplayAlignmentInfo(
                                        inputWatermarks: Map[ActorVirtualIdentity, Long],
                                        alignment:Long,
                                        estimatedCheckpointTime:Int,
                                        estimatedLoadTime: Int,
                                        processedTime: Long,
                                        outputWatermarks: Map[ActorVirtualIdentity, Long])
  final case class GetReplayAlignment() extends ControlCommand[ReplayAlignmentInfo]
}

trait GetReplayAlignmentHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: GetReplayAlignment, _) =>
    println(s"record total step at ${dp.totalValidStep + 1}")
    var estimatedCheckpointTime = 0
    var estimatedStateLoadTime = 0
    dp.operator match {
      case support: CheckpointSupport =>
        estimatedCheckpointTime = support.getEstimatedCheckpointTime
        estimatedStateLoadTime = support.getEstimatedStateLoadTime
      case _ =>
    }
    ReplayAlignmentInfo(
      dp.dataInputPort.getFIFOState ++ dp.controlInputPort.getFIFOState,
      dp.totalValidStep + 1,
      estimatedCheckpointTime,
      estimatedStateLoadTime,
      dp.totalTimeSpent,
      dp.dataOutputPort.getFIFOState ++ dp.controlOutputPort.getFIFOState)
  }

}
