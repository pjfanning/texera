package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import akka.serialization.Serialization
import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, SerializedState}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import java.net.URI


object TakeCheckpointHandler {
  final case class TakeCheckpoint(id:String, writeTo:URI) extends ControlCommand[String] // return destination URI of the checkpoint
}

trait TakeCheckpointHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: TakeCheckpoint, sender) =>
    val chkpt = new CheckpointState()
    // 1. serialize DP state
    chkpt.save(SerializedState.DP_STATE_KEY, this.dp)
    // 2. serialize operator state
    if (dp.operatorOpened) {
      dp.operator match {
        case support: CheckpointSupport =>
          dp.outputIterator.setTupleOutput(
            support.serializeState(dp.outputIterator.outputIter, chkpt)
          )
        case _ =>
      }
    }
    // 3. record inflight messages
    val dataMessagesInflight = dp.inputGateway.getAllDataChannels.map(_.collectMessagesUntilMarker(msg.id))
    val controlMessagesInflight = dp.inputGateway.getAllDataChannels.map(_.collectMessagesUntilMarker(msg.id))
    Future.collect((dataMessagesInflight ++ controlMessagesInflight).toSeq).map{
      iterables =>
        chkpt.save(SerializedState.IN_FLIGHT_MSG_KEY, iterables.flatten)

        "Success" // return the URI
    }
  }
}
