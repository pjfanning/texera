package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import TakeCheckpointHandler.{CheckpointStats, TakeCheckpoint, TakeCursor}
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ReportCheckpointStatsHandler.ReportCheckpointStats
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.COMPLETED
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.collection.mutable

object TakeCheckpointHandler {

  final case class CheckpointStats( isEstimation: Boolean,
                                    markerId: Long,
                                    inputWatermarks: Map[(ActorVirtualIdentity,Boolean), Long],
                                   outputWatermarks: Map[(ActorVirtualIdentity,Boolean), Long],
                                   totalSize: Long,
                                   alignment: Long,
                                   processedTime: Long,
                                   saveStateTime: Long,
                                   saveOutputTime: Long)

  final case class TakeCursor(markerId: Long, inputSeqNums: Map[(ActorVirtualIdentity,Boolean), Long])
    extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply

  final case class TakeCheckpoint(inputSeqNums: Map[(ActorVirtualIdentity,Boolean), Long],
                                  outputCutOffs: Map[(ActorVirtualIdentity,Boolean), Long], syncFuture: CompletableFuture[Long])
    extends ControlCommand[Unit]
    with SkipFaultTolerance
    with SkipReply
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  private val receivedMarkers = mutable.HashSet[Long]()

  registerHandler{(msg: TakeCursor, sender) =>
    if(!receivedMarkers.contains(msg.markerId)) {
      receivedMarkers.add(msg.markerId)
      var estimatedCheckpointTime = 0
      var estimatedStateLoadTime = 0
      dp.operator match {
        case support: CheckpointSupport =>
          estimatedCheckpointTime = support.getEstimatedCheckpointTime
          estimatedStateLoadTime = support.getEstimatedStateLoadTime
        case _ =>
      }
      val stats = CheckpointStats(
        isEstimation = true,
        msg.markerId,
        msg.inputSeqNums,
        dp.outputPort.getFIFOState,
        0,
        dp.totalValidStep,
        TimeUnit.NANOSECONDS.toMillis(dp.totalTimeSpent),
        estimatedCheckpointTime, 0)
      send(ReportCheckpointStats(stats), CONTROLLER)
    }
    Future.Unit
  }

  registerHandler { (msg: TakeCheckpoint, sender) =>
    val startTime = System.currentTimeMillis()
    val chkpt = new SavedCheckpoint()
    chkpt.attachSerialization(SerializationExtension(dp.actorContext.system))
    logger.info("start to take local checkpoint")
    dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
    // fill in checkpoint
    chkpt.save("fifoState", msg.inputSeqNums)
    chkpt.save("inputHubState", dp.internalQueue)
    dp.operator match {
      case support: CheckpointSupport =>
        if (!CheckpointHolder.hasMarkedCompletion(actorId, dp.totalValidStep)) {
          dp.outputIterator.setTupleOutput(
            support.serializeState(dp.outputIterator.outputIter, chkpt)
          )
        }
      case _ =>
    }
    msg.outputCutOffs.foreach{
      case (id, watermark) =>
        dp.outputPort.receiveWatermark(id, watermark)
    }
    chkpt.save("controlState", dp)
    if(dp.stateManager.getCurrentState == COMPLETED){
      CheckpointHolder.markCompletion(actorId, dp.totalValidStep)
    }
    // push to storage
    CheckpointHolder.addCheckpoint(
      actorId,
      dp.totalValidStep,
      chkpt
    )
    logger.info(
      s"checkpoint stored for $actorId at alignment = ${dp.totalValidStep} size = ${chkpt.size()} bytes"
    )
    // clear sent messages as we serialized them
    // TODO: enable the following if we only save data between last alignment point and current point.
//    dp.dataOutputPort.clearSentMessages()
//    dp.controlOutputPort.clearSentMessages()
    logger.info(
      s"local checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000f}s"
    )
    msg.syncFuture.complete(dp.totalValidStep)
    Future.Unit
  }


}
