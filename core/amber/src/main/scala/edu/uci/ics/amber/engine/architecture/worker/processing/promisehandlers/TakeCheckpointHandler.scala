package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import TakeCheckpointHandler.{CheckpointStats, InitialCheckpointStats, StartCheckpoint}
import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.COMPLETED
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.ambermessage.{SnapshotMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}

import java.util.concurrent.{CompletableFuture, TimeUnit}

object TakeCheckpointHandler {

  final case class CheckpointStats(
                                    checkpointId: Long,
                                    initialCheckpointStats: InitialCheckpointStats,
                                    inputAlignmentTime: Long,
                                    saveUnprocessedInputTime: Long,
                                    totalSize: Long, isEstimation: Boolean)

  final case class InitialCheckpointStats(alignment: Long, processedTime: Long, checkpointStateTime: Long, saveProcessedInputTime: Long)

  final case class StartCheckpoint(marker: SnapshotMarker,
                                   chkpt:SavedCheckpoint,
                                   syncFuture: CompletableFuture[InitialCheckpointStats])
    extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
    // fill in checkpoint
    msg.chkpt.save("inputHubState", dp.internalQueue)
    dp.operator match {
      case support: CheckpointSupport =>
        if (!CheckpointHolder.hasMarkedCompletion(actorId, dp.totalValidStep)) {
          dp.outputIterator.setTupleOutput(
            support.serializeState(dp.outputIterator.outputIter, msg.chkpt)
          )
        }
      case _ =>
    }
    msg.cutoffs.foreach{
      case (id, watermark) =>
        if(id != CONTROLLER){
          dp.dataOutputPort.receiveWatermark(id, watermark)
        }else{
          dp.controlOutputPort.receiveWatermark(id, watermark)
        }
    }
    msg.chkpt.save("controlState", dp)
    // push to storage
    CheckpointHolder.addCheckpoint(
      actorId,
      dp.totalValidStep,
      msg.chkpt,
      dp.stateManager.getCurrentState == COMPLETED
    )
    logger.info(
      s"checkpoint stored for $actorId at alignment = ${dp.totalValidStep} size = ${msg.chkpt.size()} bytes"
    )
    // clear sent messages as we serialized them
    // TODO: enable the following if we only save data between last alignment point and current point.
//    dp.dataOutputPort.clearSentMessages()
//    dp.controlOutputPort.clearSentMessages()
    // completion
    msg.completion.complete(dp.totalValidStep)
    Future.Unit
  }


}
