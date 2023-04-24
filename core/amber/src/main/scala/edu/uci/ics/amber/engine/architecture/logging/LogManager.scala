package edu.uci.ics.amber.engine.architecture.logging

import akka.actor.ActorRef
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogWriter
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload}
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest


//In-mem formats:
sealed trait InMemDeterminant
case class StepsOnChannel(channel: ChannelEndpointID, steps: Long)
    extends InMemDeterminant
case class RecordedPayload(channel:ChannelEndpointID, payload:ControlPayload) extends InMemDeterminant
case class TimeStamp(value: Long) extends InMemDeterminant
case object TerminateSignal extends InMemDeterminant

object LogManager {
  def getLogManager(
      enabledLogging: Boolean,
      networkCommunicationActor: ActorRef,
      determinantLogger:DeterminantLogger
  ): LogManager = {
    if (enabledLogging) {
      new LogManagerImpl(networkCommunicationActor, determinantLogger)
    } else {
      new EmptyLogManagerImpl(networkCommunicationActor)
    }
  }
}

trait LogManager {
  def setupWriter(logWriter: DeterminantLogWriter): Unit

  def sendCommitted(sendRequest: SendRequest, step:Long): Unit

  def terminate(): Unit

}

class EmptyLogManagerImpl(
    networkCommunicationActor: ActorRef
) extends LogManager {
  override def setupWriter(logWriter: DeterminantLogStorage.DeterminantLogWriter): Unit = {}

  override def sendCommitted(
      sendRequest: SendRequest, step:Long
  ): Unit = {
    networkCommunicationActor ! sendRequest
  }

  override def terminate(): Unit = {}
}

class LogManagerImpl(
    networkCommunicationActor: ActorRef,
    determinantLogger: DeterminantLogger
) extends LogManager {

  private var writer: AsyncLogWriter = _

  def setupWriter(logWriter: DeterminantLogWriter): Unit = {
    writer = new AsyncLogWriter(networkCommunicationActor, logWriter)
    writer.start()
  }

  def getDeterminantLogger: DeterminantLogger = determinantLogger

  def sendCommitted(sendRequest: SendRequest, step:Long): Unit = {
    writer.putDeterminants(determinantLogger.drainCurrentLogRecords(step))
    writer.putOutput(sendRequest)
  }

  def terminate(): Unit = {
    writer.terminate()
  }
}
