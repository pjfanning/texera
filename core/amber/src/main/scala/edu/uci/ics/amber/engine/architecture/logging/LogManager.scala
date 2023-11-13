package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogWriter
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  WorkflowFIFOMessage,
  WorkflowFIFOMessagePayload
}

//In-mem formats:
sealed trait InMemDeterminant {
  val steps: Long
}
case class ProcessingStep(channel: ChannelID, steps: Long, payload: WorkflowFIFOMessagePayload)
  extends InMemDeterminant
case class TimeStamp(value: Long, steps: Long) extends InMemDeterminant
case object TerminateSignal extends InMemDeterminant {
  val steps = 0
}


object LogManager {
  def getLogManager(
      enabledLogging: Boolean,
      handler: WorkflowFIFOMessage => Unit
  ): LogManager = {
    if (enabledLogging) {
      new LogManagerImpl(handler)
    } else {
      new EmptyLogManagerImpl(handler)
    }
  }
}

trait LogManager {
  def setupWriter(logWriter: DeterminantLogWriter): Unit

  def getDeterminantLogger: DeterminantLogger

  def sendCommitted(msg: WorkflowFIFOMessage, step: Long): Unit

  def terminate(): Unit

}

class EmptyLogManagerImpl(handler: WorkflowFIFOMessage => Unit) extends LogManager {
  override def setupWriter(logWriter: DeterminantLogStorage.DeterminantLogWriter): Unit = {}

  override def getDeterminantLogger: DeterminantLogger = new EmptyDeterminantLogger()

  override def sendCommitted(msg: WorkflowFIFOMessage, step: Long): Unit = {
    handler(msg)
  }

  override def terminate(): Unit = {}
}

class LogManagerImpl(handler: WorkflowFIFOMessage => Unit) extends LogManager {

  private val determinantLogger = new DeterminantLoggerImpl()

  private var writer: AsyncLogWriter = _

  def setupWriter(logWriter: DeterminantLogWriter): Unit = {
    writer = new AsyncLogWriter(handler, logWriter)
    writer.start()
  }

  def getDeterminantLogger: DeterminantLogger = determinantLogger

  def sendCommitted(msg: WorkflowFIFOMessage, step: Long): Unit = {
    writer.putDeterminants(determinantLogger.drainCurrentLogRecords(step))
    writer.putOutput(msg)
  }

  def terminate(): Unit = {
    writer.terminate()
  }

}
