package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, EpochMarker}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import lbmq.LinkedBlockingMultiQueue

import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable

object WorkerInternalQueue {

  final val DATA_QUEUE_PRIORITY = 1

  final val CONTROL_QUEUE_KEY = "CONTROL_QUEUE"
  final val CONTROL_QUEUE_PRIORITY = 0

  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalQueueElement {
    def channel: ChannelEndpointID
  }

  sealed trait DataElement extends InternalQueueElement

  case class ControlElement(channel: ChannelEndpointID, payload: ControlPayload)
      extends InternalQueueElement

  case class InputTuple(channel: ChannelEndpointID, tuple: ITuple) extends DataElement

  case class EndMarker(channel: ChannelEndpointID) extends DataElement
  case class InputEpochMarker(channel: ChannelEndpointID, epochMarker: EpochMarker)
      extends DataElement

}

abstract class WorkerInternalQueue extends Serializable {

  private[architecture] def dataQueues
      : mutable.HashMap[String, LinkedBlockingMultiQueue[String, InternalQueueElement]#SubQueue]

  def enqueueSystemCommand(
      control: ControlCommand[_] with SkipReply with SkipFaultTolerance
  ): Unit = {
    enqueueCommand(ControlElement(ChannelEndpointID(SELF, true), ControlInvocation(control)))
  }

  def registerInput(sender: String): Unit

  def enqueueCommand(control: ControlElement): Unit

  def enqueueData(elem: DataElement): Unit

  def peek(currentStep: Long): Option[InternalQueueElement]

  def take(currentStep: Long): InternalQueueElement

  def getDataQueueLength: Int

  def getControlQueueLength: Int

  def getData:Map[String, Array[InternalQueueElement]]

}

class WorkerInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {
  private[architecture] val lbmq = new LinkedBlockingMultiQueue[String, InternalQueueElement]()

  lbmq.addSubQueue(CONTROL_QUEUE_KEY, CONTROL_QUEUE_PRIORITY)

  private[architecture] override val dataQueues =
    new mutable.HashMap[String, LinkedBlockingMultiQueue[String, InternalQueueElement]#SubQueue]()
  private[architecture] val controlQueue = lbmq.getSubQueue(CONTROL_QUEUE_KEY)

  override def enqueueCommand(control: ControlElement): Unit = {
    controlQueue.add(control)
  }

  override def enqueueData(elem: DataElement): Unit = {
    elem match {
      case InputTuple(from, _) => creditMonitor.decreaseCredit(from)
      case other               => //pass
    }
    if (elem == null || elem.from == null || elem.from.name == null) {
      throw new RuntimeException("from is null for element " + elem)
    }
    if (dataQueues(elem.from.name) == null) {
      throw new RuntimeException(elem.from.name + " actor not registered")
    }
    dataQueues(elem.from.name).add(elem)
  }

  override def peek(currentStep: Long): Option[InternalQueueElement] = {
    Option(lbmq.peek())
  }

  override def take(currentStep: Long): InternalQueueElement = {
    lbmq.take() match {
      case elem @ InputTuple(from, _) =>
        creditMonitor.decreaseCredit(from)
        elem
      case other =>
        other
    }
  }

  override def getDataQueueLength: Int = dataQueues.values.map(q => q.size()).sum

  override def getControlQueueLength: Int = controlQueue.size()

  override def registerInput(sender: String): Unit = {
    lbmq.addSubQueue(sender, DATA_QUEUE_PRIORITY)
    dataQueues(sender) = lbmq.getSubQueue(sender)
  }

  override def getData: Map[String, Array[InternalQueueElement]] = {
    dataQueues.map{
      case (k, queue) =>
        k -> queue.toArray(Array[InternalQueueElement]())
    }.toMap
  }
}
