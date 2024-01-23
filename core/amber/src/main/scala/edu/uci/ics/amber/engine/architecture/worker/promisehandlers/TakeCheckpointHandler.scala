package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{
  DataProcessorRPCHandlerInitializer,
  WorkflowWorker
}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.MainThreadDelegate
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, SerializedState}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import java.util.concurrent.CompletableFuture
import scala.collection.mutable

object TakeCheckpointHandler {
  final case class TakeCheckpoint(estimationOnly: Boolean)
      extends ControlCommand[Long] // return checkpoint size
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    logger.info("Start to take checkpoint")
    val chkpt = new CheckpointState()
    dp.channelMarkerManager.checkpoints(dp.channelMarkerManager.getContext.marker.id) = chkpt
    var totalSize = 0L
    if (!msg.estimationOnly) {
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
    } else {
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
    val waitFuture = new CompletableFuture[Unit]()
    val closure = (worker: WorkflowWorker) => {
      val queuedMsgs = mutable.ArrayBuffer[WorkflowFIFOMessage]()
      worker.inputQueue.forEach {
        case WorkflowWorker.FIFOMessageElement(msg)           => queuedMsgs.append(msg)
        case WorkflowWorker.TimerBasedControlElement(control) => // skip
        case WorkflowWorker.ActorCommandElement(cmd)          => // skip
      }
      chkpt.save("QueuedMessages", queuedMsgs)
      // start to record input messages on main thread
      worker.inputRecordings(dp.channelMarkerManager.getContext.marker.id) =
        new mutable.ArrayBuffer[WorkflowFIFOMessage]()
      // get all messages from worker.transferService
      chkpt.save("InflightMessages", worker.transferService.getAllUnAckedMessages.toArray)
      waitFuture.complete()
      ()
    }
    dp.outputHandler(Left(MainThreadDelegate(closure)))
    waitFuture.get()
    chkpt.size() // end of the first phase
  }
}
