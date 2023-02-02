package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.CheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetMessageInQueue, NetworkMessage}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.CompletableFuture
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object CheckpointHandler {
  final case class TakeCheckpoint(sync:CompletableFuture[Unit]) extends ControlCommand[Unit]
}

trait CheckpointHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, _) =>

    // create checkpoint
    val chkpt = new SavedCheckpoint()
    chkpt.saveThread(dataProcessor)
    chkpt.saveMessages(
      Await.result((networkSenderActorRef.ref ? GetMessageInQueue), 5.seconds)
        .asInstanceOf[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]])

    // push to storage
    CheckpointHolder.checkpoints
      .getOrElseUpdate(actorId, new mutable.HashMap[Long, SavedCheckpoint]())(dataProcessor.totalSteps) = chkpt

    //notify main thread
    msg.sync.complete()
    Future.Unit
  }

}

