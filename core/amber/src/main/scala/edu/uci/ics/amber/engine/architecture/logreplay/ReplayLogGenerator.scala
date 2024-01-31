package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PrepareCheckpointHandler.PrepareCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayloadV2Message.SealedValue.ControlInvocation
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelMarkerPayload, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelMarkerIdentity}

import scala.collection.mutable

object ReplayLogGenerator {
  def generate(
      logStorage: SequentialRecordStorage[ReplayLogRecord],
      logFileName: String,
      replayTo: ChannelMarkerIdentity,
      additionalCheckpoints: List[ChannelMarkerIdentity]
  ): (mutable.Queue[ProcessingStep], mutable.Queue[WorkflowFIFOMessage]) = {
    val logs = logStorage.getReader(logFileName).mkRecordIterator()
    val steps = mutable.Queue[ProcessingStep]()
    val messages = mutable.Queue[WorkflowFIFOMessage]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        var messageToAdd = message
        message.payload match {
          case a: ControlPayload =>
            a match {
              case b@AsyncRPCClient.ControlInvocation(commandID, cmd: TakeGlobalCheckpoint) =>
                if (additionalCheckpoints.contains(cmd.checkpointId)) {
                  messageToAdd = message.copy(payload = b.copy(command = cmd.copy(estimationOnly = false)))
                }
              case _ => //skip
            }
          case _ => //skip
        }
        messages.enqueue(messageToAdd)
      case ReplayDestination(id) =>
        if (id == replayTo) {
          // we only need log record upto this point
          return (steps, messages)
        }
      case other =>
        throw new RuntimeException(s"cannot handle $other in the log")
    }
    (steps, messages)
  }
}
