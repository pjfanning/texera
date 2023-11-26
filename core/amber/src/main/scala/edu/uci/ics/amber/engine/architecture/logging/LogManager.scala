package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.common.ProcessingStepCursor
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogWriter
import edu.uci.ics.amber.engine.architecture.logging.storage.{
  DeterminantLogStorage,
  EmptyLogStorage
}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, WorkflowFIFOMessage}

//In-mem formats:
sealed trait InMemDeterminant

case class MessageContent(message: WorkflowFIFOMessage) extends InMemDeterminant
case class ProcessingStep(channelID: ChannelID, steps: Long) extends InMemDeterminant
case class TimeStamp(value: Long, steps: Long) extends InMemDeterminant
case object TerminateSignal extends InMemDeterminant

object LogManager {
  def getLogManager(
      logStorage: DeterminantLogStorage,
      handler: WorkflowFIFOMessage => Unit
  ): LogManager = {
    logStorage match {
      case _: EmptyLogStorage =>
        new EmptyLogManagerImpl(handler)
      case other =>
        val manager = new LogManagerImpl(handler)
        manager.setupWriter(other.getWriter)
        manager
    }
  }
}

trait LogManager {
  def setupWriter(logWriter: DeterminantLogWriter): Unit

  def sendCommitted(msg: WorkflowFIFOMessage): Unit

  def terminate(): Unit

  def getStep: Long

  def doFaultTolerantProcessing(
      channel: ChannelID,
      message: Option[WorkflowFIFOMessage]
  )(code: => Unit): Unit

}

class EmptyLogManagerImpl(handler: WorkflowFIFOMessage => Unit) extends LogManager {
  override def setupWriter(logWriter: DeterminantLogStorage.DeterminantLogWriter): Unit = {}

  override def sendCommitted(msg: WorkflowFIFOMessage): Unit = {
    handler(msg)
  }

  override def getStep: Long = 0L

  override def terminate(): Unit = {}

  override def doFaultTolerantProcessing(channel: ChannelID, message: Option[WorkflowFIFOMessage])(
      code: => Unit
  ): Unit = code
}

class LogManagerImpl(handler: WorkflowFIFOMessage => Unit) extends LogManager {

  private val determinantLogger = new DeterminantLoggerImpl()

  private var writer: AsyncLogWriter = _

  private val cursor = new ProcessingStepCursor()

  def doFaultTolerantProcessing(
      channel: ChannelID,
      message: Option[WorkflowFIFOMessage]
  )(code: => Unit): Unit = {
    determinantLogger.setCurrentStepWithMessage(cursor.getStep, channel, message)
    cursor.setCurrentChannel(channel)
    code
    cursor.stepIncrement()
  }

  def getStep: Long = cursor.getStep

  def setupWriter(logWriter: DeterminantLogWriter): Unit = {
    writer = new AsyncLogWriter(handler, logWriter)
    writer.start()
  }

  def sendCommitted(msg: WorkflowFIFOMessage): Unit = {
    writer.putDeterminants(determinantLogger.drainCurrentLogRecords(cursor.getStep))
    writer.putOutput(msg)
  }

  def terminate(): Unit = {
    writer.terminate()
  }

}
