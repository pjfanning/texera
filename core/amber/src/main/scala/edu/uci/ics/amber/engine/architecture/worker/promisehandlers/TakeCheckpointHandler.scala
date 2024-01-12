package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.{DelayedCallPayload, InternalDelayedClosureChannelID, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, SerializedState}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage

import java.net.URI

object TakeCheckpointHandler {
  final case class TakeCheckpoint(writeTo: URI, estimationOnly:Boolean) extends ControlCommand[Long] // return checkpoint size
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    logger.info("Start to take checkpoint")
    val chkpt = new CheckpointState()
    var totalSize = 0L
    if(!msg.estimationOnly){
      // 1. serialize DP state
      chkpt.save(SerializedState.DP_STATE_KEY, this.dp)
      logger.info("Serialized DP state")
      // 2. serialize operator state
      if (dp.operatorOpened) {
        dp.operator match {
          case support: CheckpointSupport =>
            dp.outputIterator.setTupleOutput(
              support.serializeState(dp.outputIterator.outputIter, chkpt)
            )
            logger.info("Serialized operator state")
          case _ =>
            logger.info("Operator does not support checkpoint, skip")
        }
      } else {
        logger.info("Operator does not open, nothing to serialize")
      }
    }else{
      totalSize += (dp.operator match {
        case support: CheckpointSupport =>
          support.getEstimatedCheckpointCost
        case _ => 0L
      })
    }
    // 3. record inflight messages
    logger.info(
      "Begin collecting inflight messages for all channels in the score except the current sender"
    )
    val channelsWithinScope = dp.epochManager.getChannelsWithinScope
    val channelsToCollect = channelsWithinScope - dp.epochManager.getContext.fromChannel
    val collectorFutures = dp.inputGateway.getAllChannels
      .filter(c => channelsToCollect.contains(c.channelId))
      .map(_.collectMessagesUntilMarker(dp.epochManager.getContext.marker.id))
    Future.collect(collectorFutures.toSeq).flatMap { iterables =>
      val promise = Promise[Long]()
      val closure = () => {
        if (!msg.estimationOnly) {
          chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, iterables.flatten)
          logger.info(
            s"Serialized all inflight messages, start to push checkpoint to the storage. checkpoint size = ${chkpt.size()} bytes"
          )
          val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.writeTo))
          val writer = storage.getWriter(actorId.name.replace("Worker:", ""))
          writer.writeRecord(chkpt)
          writer.flush()
          totalSize = chkpt.size()
          logger.info(s"Checkpoint finalized")

        }
        promise.setValue(totalSize)
      }
      val channel = dp.inputGateway.getChannel(InternalDelayedClosureChannelID)
      channel.acceptMessage(WorkflowFIFOMessage(InternalDelayedClosureChannelID, channel.getCurrentSeq, DelayedCallPayload(closure)))
      promise
    }
  }
}
