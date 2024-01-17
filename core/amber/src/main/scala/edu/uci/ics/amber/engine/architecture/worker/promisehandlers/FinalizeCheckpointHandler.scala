package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{DataProcessorRPCHandlerInitializer, WorkflowWorker}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.MainThreadDelegate
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AssignLocalBreakpointHandler.AssignLocalBreakpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.FinalizeCheckpointHandler.FinalizeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, SerializedState}
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage

import java.net.URI
import java.util.concurrent.CompletableFuture
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FinalizeCheckpointHandler {
  final case class FinalizeCheckpoint(checkpointId:String, writeTo: URI) extends ControlCommand[Long]
}

trait FinalizeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: FinalizeCheckpoint, sender) =>
    if(dp.epochManager.checkpoints.contains(msg.checkpointId)){
      val waitFuture = new CompletableFuture[Unit]()
      val chkpt = dp.epochManager.checkpoints(msg.checkpointId)
      val closure = (worker: WorkflowWorker) => {
        chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, worker.inputRecordings.getOrElse(msg.checkpointId, new ArrayBuffer()))
        worker.inputRecordings.remove(msg.checkpointId)
        waitFuture.complete()
        ()
      }
      dp.outputHandler(Left(MainThreadDelegate(closure)))
      waitFuture.get()
      val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.writeTo))
      val writer = storage.getWriter(actorId.name.replace("Worker:", ""))
      writer.writeRecord(chkpt)
      writer.flush()
      logger.info(s"Checkpoint finalized")
      chkpt.size()
    }else{
      0L
    }
  }
}
