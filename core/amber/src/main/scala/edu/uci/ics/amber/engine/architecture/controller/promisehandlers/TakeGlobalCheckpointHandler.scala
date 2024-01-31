package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ChannelMarkerHandler.PropagateChannelMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.FinalizeCheckpointHandler.FinalizeCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PrepareCheckpointHandler.PrepareCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, SerializedState}
import edu.uci.ics.amber.engine.common.ambermessage.NoAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity

import java.net.URI

object TakeGlobalCheckpointHandler {
  final case class TakeGlobalCheckpoint(
      estimationOnly: Boolean,
      checkpointId: ChannelMarkerIdentity,
      destination: URI
  ) extends ControlCommand[Long] // return the total size
}
trait TakeGlobalCheckpointHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeGlobalCheckpoint, sender) =>
    @transient val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.destination))
    var estimationOnly = msg.estimationOnly
    if (storage.containsFolder(msg.checkpointId.toString)) {
      logger.info("skip checkpoint since its already taken")
      estimationOnly = true
    }
      val uri = msg.destination.resolve(msg.checkpointId.toString)
      var totalSize = 0L
      val physicalOpToTakeCheckpoint = cp.workflow.physicalPlan.operators.map(_.id)
      execute(
        PropagateChannelMarker(
          cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
          msg.checkpointId,
          NoAlignment,
          cp.workflow.physicalPlan,
          physicalOpToTakeCheckpoint,
          PrepareCheckpoint(estimationOnly)
        ),
        sender
      ).flatMap { ret =>
        Future
          .collect(ret.map {
            case (workerId, _) =>
              send(FinalizeCheckpoint(msg.checkpointId, uri), workerId).map { size =>
                totalSize += size
              }
          })
          .map { _ =>
            logger.info("Start to take checkpoint")
            val chkpt = new CheckpointState()
            totalSize += chkpt.size()
            if (!estimationOnly) {
              // serialize CP state
              try {
                chkpt.save(SerializedState.CP_STATE_KEY, this.cp)
              } catch {
                case e: Throwable => logger.error("Failed to serialize controller state", e)
              }
              logger.info(s"Serialized CP state, current workflow state = ${cp.executionState.getState}")
              // get all output messages from cp.transferService
              chkpt.save(
                SerializedState.OUTPUT_MSG_KEY,
                this.cp.transferService.getAllUnAckedMessages.toArray
              )
              val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(uri))
              val writer = storage.getWriter(actorId.name)
              writer.writeRecord(chkpt)
              writer.flush()
            }
            logger.info(s"global checkpoint finalized, total size = $totalSize")
            totalSize
          }
      }
  }
}
