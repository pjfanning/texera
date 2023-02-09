package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import akka.serialization.Serialization
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.CheckpointSupport
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}

import java.util.concurrent.CompletableFuture

object TakeCheckpointHandler{
  final case class TakeCheckpoint(chkpt:SavedCheckpoint, serialization:Serialization, completion:CompletableFuture[Long]) extends ControlCommand[Unit] with SkipFaultTolerance with SkipReply
}


trait TakeCheckpointHandler {
  this: DataProcessor =>

  registerHandler{
    (msg: TakeCheckpoint, sender) =>
      outputManager.adaptiveBatchingMonitor.pauseAdaptiveBatching()
      // fill in checkpoint
      msg.chkpt.save("inputHubState", SerializedState.fromObject(internalQueue, msg.serialization))
      operator match {
        case support: CheckpointSupport =>
          support.serializeState(currentOutputIterator, msg.chkpt, msg.serialization)
        case _ =>
      }
      msg.chkpt.save("controlState", SerializedState.fromObject(this, msg.serialization))
      msg.chkpt.save(
        "outputMassages",
        SerializedState.fromObject(logManager.getUnackedMessages(), msg.serialization)
      )
      // push to storage
      CheckpointHolder.addCheckpoint(actorId, totalValidStep, msg.chkpt)
      logger.info(s"checkpoint stored for $actorId at alignment = $totalValidStep size = ${msg.chkpt.size()} bytes")
      // completion
      msg.completion.complete(totalValidStep)
      Future.Unit
  }
}
