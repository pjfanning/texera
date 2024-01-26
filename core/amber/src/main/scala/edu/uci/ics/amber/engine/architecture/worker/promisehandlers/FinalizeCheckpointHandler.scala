package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessorRPCHandlerInitializer,
  WorkflowWorker
}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.MainThreadDelegate
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.FinalizeCheckpointHandler.FinalizeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, SerializedState}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity

import java.net.URI
import java.util.concurrent.CompletableFuture
import scala.collection.mutable.ArrayBuffer

object FinalizeCheckpointHandler {
  final case class FinalizeCheckpoint(checkpointId: ChannelMarkerIdentity, writeTo: URI)
      extends ControlCommand[Long]
}

trait FinalizeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: FinalizeCheckpoint, sender) =>
    if (dp.channelMarkerManager.checkpoints.contains(msg.checkpointId)) {
      val waitFuture = new CompletableFuture[Unit]()
      val chkpt = dp.channelMarkerManager.checkpoints(msg.checkpointId)
      val closure = (worker: WorkflowWorker) => {
        logger.info(s"Main thread: start to serialize recorded messages.")
        chkpt.save(
          SerializedState.IN_FLIGHT_MSG_KEY,
          worker.inputRecordings.getOrElse(msg.checkpointId, new ArrayBuffer())
        )
        worker.inputRecordings.remove(msg.checkpointId)
        logger.info(s"Main thread: recorded messages serialized.")
        waitFuture.complete(())
        ()
      }
      dp.outputHandler(Left(MainThreadDelegate(closure)))
      waitFuture.get()
      logger.info(s"Start to write checkpoint to storage. Destination: ${msg.writeTo}")
      val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.writeTo))
      val writer = storage.getWriter(actorId.name.replace("Worker:", ""))
      writer.writeRecord(chkpt)
      writer.flush()
      logger.info(s"Checkpoint finalized, total size = ${chkpt.size()} bytes")
      chkpt.size()
    } else {
      logger.info(s"Checkpoint is estimation-only. report estimated size.")
      dp.operator match {
        case support: CheckpointSupport =>
          support.getEstimatedCheckpointCost
        case _ => 0L
      } // for estimation
    }
  }
}
