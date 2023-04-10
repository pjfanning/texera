package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import TakeCheckpointHandler.{CheckpointStats, TakeCheckpoint, TakeCursor}
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ReportCheckpointStatsHandler.ReportCheckpointStats
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.COMPLETED
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.ambermessage.FIFOMarker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.collection.mutable

object TakeCheckpointHandler {

  final case class CheckpointStats(markerId: Long,
                                   inputWatermarks: Map[(ActorVirtualIdentity,Boolean), Long],
                                   outputWatermarks: Map[(ActorVirtualIdentity,Boolean), Long],
                                   alignment: Long,
                                   saveStateCost: Long)

  final case class TakeCursor(marker: FIFOMarker, inputSeqNums: Map[(ActorVirtualIdentity,Boolean), Long])
    extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply

  final case class TakeCheckpoint(marker:FIFOMarker, chkpt:SavedCheckpoint, syncFuture: CompletableFuture[Long])
    extends ControlCommand[Unit]
    with SkipFaultTolerance
    with SkipReply
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler{(msg: TakeCursor, sender) =>
      var estimatedCheckpointCost = 0
      dp.operator match {
        case support: CheckpointSupport =>
          estimatedCheckpointCost = support.getEstimatedCheckpointTime
        case _ =>
      }
      val stats = CheckpointStats(
        msg.marker.id,
        msg.inputSeqNums,
        dp.outputPort.getFIFOState,
        dp.totalValidStep,
        estimatedCheckpointCost + dp.internalQueue.getDataQueueLength)
    dp.outputPort.broadcastMarker(msg.marker)
    send(ReportCheckpointStats(stats), CONTROLLER)
    Future.Unit
  }

  registerHandler { (msg: TakeCheckpoint, sender) =>
    dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
    msg.chkpt.save("inputHubState", dp.internalQueue)
    dp.operator match {
      case support: CheckpointSupport =>
        dp.outputIterator.setTupleOutput(
          support.serializeState(dp.outputIterator.outputIter, msg.chkpt)
        )
      case _ =>
    }
    msg.chkpt.save("controlState", dp)
    dp.outputPort.broadcastMarker(msg.marker)
    msg.syncFuture.complete(dp.totalValidStep)
    Future.Unit
  }


}
