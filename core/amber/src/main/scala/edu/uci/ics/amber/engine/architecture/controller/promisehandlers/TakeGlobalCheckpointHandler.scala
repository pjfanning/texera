package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.ChannelMarkerHandler.PropagateChannelMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.FinalizeCheckpointHandler.FinalizeCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PrepareCheckpointHandler.PrepareCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, SerializedState}
import edu.uci.ics.amber.engine.common.ambermessage.{NoAlignment, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity

import java.net.URI
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
    logger.info("Start to take checkpoint")
    val chkpt = new CheckpointState()
    if (!msg.estimationOnly) {
      // serialize CP state
      try {
        chkpt.save(SerializedState.CP_STATE_KEY, this.cp)
      } catch {
        case e: Throwable => logger.error("Failed to serialize controller state", e)
      }
      logger.info("Serialized CP state")
    }
    // get all output messages from cp.transferService
    chkpt.save(
      SerializedState.OUTPUT_MSG_KEY,
      this.cp.transferService.getAllUnAckedMessages.toArray
    )
    val physicalOpToTakeCheckpoint = cp.workflow.physicalPlan.operators.map(_.id)
    var totalSize = 0L
    // start to record input messages
    this.cp.inputRecordings(msg.checkpointId) = new mutable.ArrayBuffer[WorkflowFIFOMessage]()
    execute(
      PropagateChannelMarker(
        cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
        msg.checkpointId,
        NoAlignment,
        cp.workflow.physicalPlan,
        physicalOpToTakeCheckpoint,
        PrepareCheckpoint(msg.estimationOnly)
      ),
      sender
    ).flatMap { ret =>
      Future
        .collect(ret.map {
          case (workerId, chkptSize: Long) =>
            totalSize += chkptSize
            send(FinalizeCheckpoint(msg.checkpointId, msg.destination), workerId)
        })
        .map { _ =>
          chkpt.save(
            SerializedState.IN_FLIGHT_MSG_KEY,
            cp.inputRecordings.getOrElse(msg.checkpointId, new ArrayBuffer())
          )
          cp.inputRecordings.remove(msg.checkpointId)
          totalSize += chkpt.size()
          val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.destination))
          val writer = storage.getWriter(actorId.name)
          writer.writeRecord(chkpt)
          writer.flush()
          logger.info(s"global checkpoint finalized, total size = $totalSize")
          totalSize
        }
    }
  }
}
