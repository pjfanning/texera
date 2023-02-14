package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{
  CONTROL_QUEUE,
  ControlElement,
  DATA_QUEUE,
  DataElement,
  InputTuple,
  InternalElement
}
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayload
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{
  ControlCommand,
  SkipFaultTolerance,
  SkipReply
}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
import lbmq.LinkedBlockingMultiQueue

object WorkerInternalQueue {

  final val DATA_QUEUE = 1
  final val CONTROL_QUEUE = 0

  // 4 kinds of elements can be accepted by internal queue
  sealed trait InternalElement

  sealed trait DataElement extends InternalElement

  case class ControlElement(payload: ControlPayload, from: ActorVirtualIdentity)
      extends InternalElement

  case class InputTuple(from: ActorVirtualIdentity, tuple: ITuple) extends DataElement

  case class EndMarker(from: ActorVirtualIdentity) extends DataElement

}

abstract class WorkerInternalQueue extends Serializable {

  def enqueueSystemCommand(
      control: ControlCommand[_] with SkipReply with SkipFaultTolerance
  ): Unit = {
    enqueueCommand(ControlElement(ControlInvocation(control), SELF))
  }

  def enqueueCommand(control: ControlElement): Unit

  def enqueueData(elem: DataElement): Unit

  def peek(currentStep: Long): Option[InternalElement]

  def take(currentStep: Long): InternalElement

  def setDataQueueEnabled(status: Boolean): Unit

  def getDataQueueLength: Int

  def getControlQueueLength: Int

}

class WorkerInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {
  private val lbmq = new LinkedBlockingMultiQueue[Int, InternalElement]()

  lbmq.addSubQueue(DATA_QUEUE, DATA_QUEUE)
  lbmq.addSubQueue(CONTROL_QUEUE, CONTROL_QUEUE)

  private val dataQueue = lbmq.getSubQueue(DATA_QUEUE)
  private val controlQueue = lbmq.getSubQueue(CONTROL_QUEUE)

  override def enqueueCommand(control: ControlElement): Unit = {
    controlQueue.add(control)
  }

  override def enqueueData(elem: DataElement): Unit = {
    elem match {
      case InputTuple(from, _) => creditMonitor.decreaseCredit(from)
      case other               => //pass
    }
    dataQueue.add(elem)
  }

  override def peek(currentStep: Long): Option[InternalElement] = {
    Option(lbmq.peek())
  }

  override def take(currentStep: Long): InternalElement = {
    lbmq.take() match {
      case elem @ InputTuple(from, _) =>
        creditMonitor.decreaseCredit(from)
        elem
      case other =>
        other
    }
  }

  override def getDataQueueLength: Int = dataQueue.size()

  override def getControlQueueLength: Int = controlQueue.size()

  override def setDataQueueEnabled(status: Boolean): Unit = {
    dataQueue.enable(status)
  }
}
