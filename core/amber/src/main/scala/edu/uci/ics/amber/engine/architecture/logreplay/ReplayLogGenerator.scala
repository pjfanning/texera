package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PrepareCheckpointHandler.PrepareCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelMarkerPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.storage.SequentialRecordStorage
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelMarkerIdentity}

import scala.collection.mutable

object ReplayLogGenerator {
  def generate(
      logStorage: SequentialRecordStorage[ReplayLogRecord],
      logFileName: String,
      replayTo: ChannelMarkerIdentity,
      actorId: ActorVirtualIdentity,
      additionalCheckpoints: List[ChannelMarkerIdentity]
  ): (mutable.Queue[ProcessingStep], mutable.Queue[WorkflowFIFOMessage]) = {
    val logs = logStorage.getReader(logFileName).mkRecordIterator()
    val steps = mutable.Queue[ProcessingStep]()
    val messages = mutable.Queue[WorkflowFIFOMessage]()
    logs.foreach {
      case s: ProcessingStep =>
        steps.enqueue(s)
      case MessageContent(message) =>
        message.payload match {
          case a: ChannelMarkerPayload =>
            if (additionalCheckpoints.contains(a.id)) {
              val invocation = a.commandMapping(actorId)
              val cmd = invocation.command.asInstanceOf[PrepareCheckpoint]
              val payload = a.copy(commandMapping =
                a.commandMapping + (actorId -> invocation.copy(command =
                  cmd.copy(estimationOnly = false)
                ))
              )
              messages.enqueue(message.copy(payload = payload))
            }
          case _ => messages.enqueue(message)
        }
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
