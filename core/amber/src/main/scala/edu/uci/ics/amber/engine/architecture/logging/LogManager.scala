package edu.uci.ics.amber.engine.architecture.logging

import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogWriter
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ChannelID, ControlPayload, WorkflowExecutionPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

//In-mem formats:
sealed trait InMemDeterminant{
  val steps:Long
}
case class ProcessingStep(channel: ChannelID, steps: Long, payload:WorkflowFIFOMessagePayload)
  extends InMemDeterminant
case class TimeStamp(value: Long, steps:Long) extends InMemDeterminant
case object TerminateSignal extends InMemDeterminant{
  val steps = 0
}

object LogManager {
  def getLogManager(
      logStorageType: String,
      logName:String,
      handler: WorkflowFIFOMessage => Unit
  ): LogManager = {
    logStorageType match{
      case "none" =>
        new EmptyLogManagerImpl(handler)
      case other =>
        val manager = new LogManagerImpl(handler)
        val storage = DeterminantLogStorage.getLogStorage(other, logName)
        manager.setupWriter(storage.getWriter)
        manager
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
    writer.putDeterminants(determinantLogger.drainCurrentLogRecords())
    writer.putOutput(msg)
  }

  def terminate(): Unit = {
    writer.terminate()
  }

}
