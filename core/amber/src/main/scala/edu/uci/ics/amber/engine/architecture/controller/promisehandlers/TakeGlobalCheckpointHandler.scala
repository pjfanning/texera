package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{
  AmberConfig,
  CheckpointState,
  SerializedState,
  VirtualIdentityUtils
}
import edu.uci.ics.amber.engine.common.ambermessage.NoAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage

import java.net.URI
import java.util.UUID

object TakeGlobalCheckpointHandler {
  final case class TakeGlobalCheckpoint() extends ControlCommand[Unit]
}
trait TakeGlobalCheckpointHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeGlobalCheckpoint, sender) =>
    if (!AmberConfig.isFaultToleranceEnabled) {
      logger.info("Fault tolerance is not enabled. Unable to take a global checkpoint")
      Future.Unit
    } else {
      logger.info("Start to take checkpoint")
      val chkpt = new CheckpointState()
      // serialize CP state
      chkpt.save(SerializedState.CP_STATE_KEY, this.cp)
      logger.info("Serialized CP state")
      val checkpointId = s"Checkpoint_${UUID.randomUUID().toString}"
      val storageFolder = AmberConfig.faultToleranceLogRootFolder.get
      val uri = new URI(s"$storageFolder$checkpointId/")
      val physicalOpToTakeCheckpoint = cp.workflow.physicalPlan.operators.map(_.id)
      val collectorFutures = cp.inputGateway.getAllChannels
        .filter { c =>
          physicalOpToTakeCheckpoint.contains(VirtualIdentityUtils.getPhysicalOpId(c.channelId.to))
        }
        .map(_.collectMessagesUntilMarker(checkpointId))
      val workerCheckpointFuture = execute(
        PropagateEpochMarker(
          cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
          checkpointId,
          NoAlignment,
          cp.workflow.physicalPlan,
          physicalOpToTakeCheckpoint,
          TakeCheckpoint(uri)
        ),
        sender
      )
      val inflightMsgCollectionFuture = Future.collect(collectorFutures.toSeq).map { iterables =>
        chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, iterables.flatten)
        val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(uri))
        val writer = storage.getWriter(actorId.name)
        writer.writeRecord(chkpt)
        writer.flush()
      }
      Future.collect(Seq(workerCheckpointFuture, inflightMsgCollectionFuture)).unit
    }
  }
}
