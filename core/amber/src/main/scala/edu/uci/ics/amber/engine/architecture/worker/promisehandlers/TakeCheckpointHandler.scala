package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import akka.serialization.Serialization
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, SerializedState, VirtualIdentityUtils}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.net.URI


object TakeCheckpointHandler {
  final case class TakeCheckpoint(id:String, writeTo:URI) extends ControlCommand[Unit]
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    logger.info("Start to take checkpoint")
    val chkpt = new CheckpointState()
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
    }else{
      logger.info("Operator does not open, nothing to serialize")
    }
    // 3. record inflight messages
    logger.info("Begin collecting inflight messages for all channels in the score except the current sender")
    val collectorFutures = dp.inputGateway.getAllChannels.filter(_.channelId!=sender).map(_.collectMessagesUntilMarker(msg.id))
    Future.collect(collectorFutures.toSeq).map{
      iterables =>
        chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, iterables.flatten)
        logger.info(s"Serialized all inflight messages, start to push checkpoint to the storage. checkpoint size = ${chkpt.size()} bytes")
        val storage = SequentialRecordStorage.getStorage[CheckpointState](Some(msg.writeTo))
        val writer = storage.getWriter(actorId.name.replace("Worker:", ""))
        writer.writeRecord(chkpt)
        writer.flush()
        logger.info(s"Checkpoint finalized")
    }
  }
}
