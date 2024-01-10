package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, SerializedState}
import edu.uci.ics.amber.engine.common.ambermessage.NoAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage

import java.net.URI
import java.time.Instant
import java.util.UUID


object TakeGlobalCheckpointHandler{
  final case class TakeGlobalCheckpoint() extends ControlCommand[Unit]
}
trait TakeGlobalCheckpointHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeGlobalCheckpoint, sender) =>
    logger.info("Start to take checkpoint")
    val chkpt = new CheckpointState()
    // serialize CP state
    chkpt.save(SerializedState.CP_STATE_KEY, this.cp)
    logger.info("Serialized CP state")
    val checkpointId = "Checkpoint_" + Instant.now().toString
    val uri = new URI(s"file:///checkpoints/Checkpoint_${UUID.randomUUID().toString}/")
    val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(uri))
    val writer = storage.getWriter(actorId.name)
    writer.writeRecord(chkpt)
    writer.flush()
    execute(
      PropagateEpochMarker(
        cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
        checkpointId,
        NoAlignment,
        cp.workflow.physicalPlan,
        cp.workflow.physicalPlan.operators.map(_.id),
        TakeCheckpoint(checkpointId, uri)
      ),
      sender
    ).unit
  }
}
