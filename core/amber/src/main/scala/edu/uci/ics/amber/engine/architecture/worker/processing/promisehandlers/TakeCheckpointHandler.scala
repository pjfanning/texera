package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer

object TakeCheckpointHandler {

//  final case class CheckpointStats(markerId: Long,
//                                   inputWatermarks: Map[ChannelEndpointID, Long],
//                                   outputWatermarks: Map[ChannelEndpointID, Long],
//                                   alignment: Long,
//                                   saveStateCost: Long)
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

//  registerHandler{(msg: TakeCursor, sender) =>
//      var estimatedCheckpointCost = 0
//      dp.operator match {
//        case support: CheckpointSupport =>
//          estimatedCheckpointCost = support.getEstimatedCheckpointTime
//        case _ =>
//      }
//      val stats = CheckpointStats(
//        msg.marker.id,
//        msg.inputSeqNums,
//        dp.outputPort.getFIFOState,
//        dp.determinantLogger.getStep,
//        estimatedCheckpointCost + dp.internalQueue.getDataQueueLength)
//    dp.outputPort.broadcastMarker(msg.marker)
//    send(ReportCheckpointStats(stats), CONTROLLER)
//    Future.Unit
//  }
//
//  registerHandler { (msg: TakeCheckpoint, sender) =>
//    dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
//    msg.chkpt.save("inputHubState", dp.internalQueue)
//    dp.operator match {
//      case support: CheckpointSupport =>
//        dp.outputIterator.setTupleOutput(
//          support.serializeState(dp.outputIterator.outputIter, msg.chkpt)
//        )
//      case _ =>
//    }
//    msg.chkpt.save("controlState", dp)
//    dp.outputPort.broadcastMarker(msg.marker)
//    msg.syncFuture.complete(dp.determinantLogger.getStep)
//    Future.Unit
//  }

}
