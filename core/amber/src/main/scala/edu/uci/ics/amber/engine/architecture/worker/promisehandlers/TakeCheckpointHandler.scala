package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import akka.serialization.Serialization
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
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
      msg.chkpt.save("operatorState", operator.serializeState(currentOutputIterator, msg.serialization))
      msg.chkpt.save("controlState", SerializedState.fromObject(this, msg.serialization))
      msg.chkpt.save(
        "outputMassages",
        SerializedState.fromObject(logManager.getUnackedMessages(), msg.serialization)
      )
      // push to storage
      CheckpointHolder.addCheckpoint(actorId, totalValidStep, msg.chkpt)
      // completion
      msg.completion.complete(totalValidStep)
      Future.Unit
  }
}
