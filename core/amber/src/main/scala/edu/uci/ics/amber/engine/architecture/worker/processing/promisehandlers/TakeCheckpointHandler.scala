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

  registerHandler { (msg: StartCheckpoint, sender) =>
    val checkpointTime = if(msg.marker.estimation){
      dp.operator match {
        case support: CheckpointSupport =>
          support.getEstimatedCheckpointTime
        case _ => 0L
      }
    }else{
      dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
      val startTime = System.currentTimeMillis()
      msg.chkpt.save("internalQueueState", dp.internalQueue)
      dp.operator match {
        case support: CheckpointSupport =>
          if (!CheckpointHolder.hasMarkedCompletion(actorId, dp.totalValidStep)) {
            dp.outputIterator.setTupleOutput(
              support.serializeState(dp.outputIterator.outputIter, msg.chkpt)
            )
          }
        case _ =>
      }
      msg.chkpt.save("controlState", dp)
      val endTime = System.currentTimeMillis()
      endTime - startTime
    }
    // sent marker to downstream
    dp.outputPort.broadcastMarker(msg.marker)
    if(dp.stateManager.getCurrentState == COMPLETED){
      CheckpointHolder.markCompletion(actorId, dp.totalValidStep)
    }
    msg.syncFuture.complete(InitialCheckpointStats(dp.totalValidStep, TimeUnit.NANOSECONDS.toMillis(dp.totalTimeSpent), checkpointTime, 0))
    Future.Unit
  }


}
