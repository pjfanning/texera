package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateChannelMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{
  AmberConfig,
  CheckpointState,
  SerializedState,
  VirtualIdentityUtils
}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  DelayedCallPayload,
  NoAlignment,
  WorkflowFIFOMessage
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage

import java.net.URI
import java.util.UUID

object TakeGlobalCheckpointHandler {
  final case class TakeGlobalCheckpoint(estimationOnly: Boolean)
      extends ControlCommand[Long] // return the total size
}
trait TakeGlobalCheckpointHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeGlobalCheckpoint, sender) =>
    if (!AmberConfig.isFaultToleranceEnabled) {
      logger.info("Fault tolerance is not enabled. Unable to take a global checkpoint")
      Future(0L)
    } else {
      logger.info("Start to take checkpoint")
      val chkpt = new CheckpointState()
      if (!msg.estimationOnly) {
        // serialize CP state
        try {
          chkpt.save(SerializedState.CP_STATE_KEY, this.cp)
        } catch {
          case e: Throwable => logger.error("???", e)
        }
        logger.info("Serialized CP state")
      }
      val checkpointId = s"Checkpoint_${UUID.randomUUID().toString}"
      val storageFolder = AmberConfig.faultToleranceLogRootFolder.get
      val uri = new URI(s"$storageFolder$checkpointId/")
      val physicalOpToTakeCheckpoint = cp.workflow.physicalPlan.operators.map(_.id)
      val collectorFutures = cp.inputGateway.getAllChannels
        .filter { c =>
          physicalOpToTakeCheckpoint.contains(VirtualIdentityUtils.getPhysicalOpId(c.channelId.to))
        }
        .map(_.collectMessagesUntilMarker(checkpointId))
      var totalSize = 0L
      val workerCheckpointFuture = execute(
        PropagateChannelMarker(
          cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
          checkpointId,
          NoAlignment,
          cp.workflow.physicalPlan,
          physicalOpToTakeCheckpoint,
          TakeCheckpoint(uri, msg.estimationOnly)
        ),
        sender
      ).map { ret =>
        ret.foreach {
          case (workerId, chkptSize: Long) =>
            totalSize += chkptSize
        }
      }
      val inflightMsgCollectionFuture =
        Future.collect(collectorFutures.toSeq).flatMap { iterables =>
          val promise = Promise[Unit]()
          val closure = () => {
            if (!msg.estimationOnly) {
              chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, iterables.flatten)
              val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(uri))
              val writer = storage.getWriter(actorId.name)
              writer.writeRecord(chkpt)
              writer.flush()
              totalSize += chkpt.size()
            }
            promise.setValue()
          }
          val channel = cp.inputGateway.getChannel(ChannelID.InternalDelayedClosureChannelID)
          channel.acceptMessage(
            WorkflowFIFOMessage(
              ChannelID.InternalDelayedClosureChannelID,
              channel.getCurrentSeq,
              DelayedCallPayload(closure)
            )
          )
          promise
        }
      Future.collect(Seq(workerCheckpointFuture, inflightMsgCollectionFuture)).map { _ =>
        logger.info(s"global checkpoint finalized, size = $totalSize")
        totalSize
      }
    }
  }
}
