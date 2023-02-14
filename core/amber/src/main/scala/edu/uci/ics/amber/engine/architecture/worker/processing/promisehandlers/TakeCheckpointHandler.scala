package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import akka.serialization.Serialization
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}

import java.util.concurrent.CompletableFuture

object TakeCheckpointHandler {
  final case class TakeCheckpoint(
      chkpt: SavedCheckpoint,
      serialization: Serialization,
      completion: CompletableFuture[Long]
  ) extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    dp.outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
    // fill in checkpoint
    msg.chkpt.save("inputHubState", SerializedState.fromObject(dp.internalQueue, msg.serialization))
    dp.operator match {
      case support: CheckpointSupport =>
        support.serializeState(dp.currentOutputIterator, msg.chkpt, msg.serialization)
      case _ =>
    }
    msg.chkpt.save("controlState", SerializedState.fromObject(this, msg.serialization))
    msg.chkpt.save(
      "outputMassages",
      SerializedState.fromObject(dp.logManager.getUnackedMessages(), msg.serialization)
    )
    // push to storage
    CheckpointHolder.addCheckpoint(actorId, dp.totalValidStep, msg.chkpt)
    logger.info(
      s"checkpoint stored for $actorId at alignment = ${dp.totalValidStep} size = ${msg.chkpt.size()} bytes"
    )
    // completion
    msg.completion.complete(dp.totalValidStep)
    Future.Unit
  }
}
