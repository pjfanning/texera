package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.recovery.RecoveryQueue
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue._
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, EpochMarker}
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
    def from: ActorVirtualIdentity
  }

  sealed trait DataElement extends InternalQueueElement

  case class ControlElement(payload: ControlPayload, from: ActorVirtualIdentity)
      extends InternalQueueElement

  case class InputTuple(from: ActorVirtualIdentity, tuple: ITuple) extends DataElement

  case class EndMarker(from: ActorVirtualIdentity) extends DataElement
  case class InputEpochMarker(from: ActorVirtualIdentity, epochMarker: EpochMarker)
    extends InternalQueueElement


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
  private[architecture] val lbmq = new LinkedBlockingMultiQueue[String, InternalQueueElement]()

  lbmq.addSubQueue(CONTROL_QUEUE_KEY, CONTROL_QUEUE_PRIORITY)

  private[architecture] val dataQueues =
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
