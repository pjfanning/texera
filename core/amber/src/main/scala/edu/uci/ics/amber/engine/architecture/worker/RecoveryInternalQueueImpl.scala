package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.ActorContext
import edu.uci.ics.amber.engine.architecture.logging._
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{
  ControlElement,
  DataElement,
  EndMarker,
  InputTuple,
  InternalElement
}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.ReplaceRecoveryQueue
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import scala.collection.mutable

class RecoveryInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  @transient
  private var records: Iterator[InMemDeterminant] = _
  @transient
  private var actorContext: ActorContext = _
  @transient
  private var orderedQueue: LinkedBlockingQueue[InternalElement] = _

  private var numRecordsRead: Int = 0
  private val inputMapping = mutable
    .HashMap[ActorVirtualIdentity, LinkedBlockingQueue[DataElement]]()
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ControlElement]]()
  private var step = 0L
  private var targetVId: ActorVirtualIdentity = _
  private var replayTo = -1L
  private var nextControlToEmit: ControlElement = _

  def setReplayTo(dest: Long): Unit = {
    replayTo = dest
  }

  def initialize(records: Iterator[InMemDeterminant], actorContext: ActorContext): Unit = {
    this.orderedQueue = new LinkedBlockingQueue[InternalElement]()
    this.actorContext = actorContext
    this.records = records.drop(numRecordsRead)
  }

  def getAllStashedInputs: Iterable[DataElement] = {
    val res = new mutable.ArrayBuffer[DataElement]
    inputMapping.values.foreach { x =>
      while (!x.isEmpty) {
        res.append(x.take())
      }
    }
    res
  }

  def getAllStashedControls: Iterable[ControlElement] = {
    val res = new mutable.ArrayBuffer[ControlElement]
    controlMessages.foreach { x =>
      while (x._2.nonEmpty) {
        res.append(x._2.dequeue())
      }
    }
    res
  }

  private def loadDeterminant(): Unit = {
    if (orderedQueue.isEmpty && step == 0 && records.hasNext) {
      val n = records.next()
      println(s"read: ${n}")
      n match {
        case StepDelta(sender, steps) =>
          targetVId = sender
          step = steps
        case ProcessControlMessage(controlPayload, from) =>
          nextControlToEmit = ControlElement(controlPayload, from)
        case TimeStamp(value) => ???
        case TerminateSignal  => throw new RuntimeException("Cannot handle terminate signal here.")
      }
      numRecordsRead += 1
    }
  }

  override def enqueueSystemCommand(
      control: AsyncRPCServer.ControlCommand[_]
        with AsyncRPCServer.SkipReply
        with AsyncRPCServer.SkipFaultTolerance
  ): Unit = {
    orderedQueue.put(ControlElement(ControlInvocation(control), SELF))
  }

  override def enqueueCommand(control: ControlElement): Unit = {
    controlMessages
      .getOrElseUpdate(control.from, new mutable.Queue[ControlElement]())
      .enqueue(control)
  }

  override def enqueueData(elem: DataElement): Unit = {
    elem match {
      case tuple: InputTuple =>
        creditMonitor.decreaseCredit(tuple.from)
        inputMapping
          .getOrElseUpdate(tuple.from, new LinkedBlockingQueue[DataElement]())
          .put(tuple)
      case EndMarker(from) =>
        inputMapping
          .getOrElseUpdate(from, new LinkedBlockingQueue[DataElement]())
          .put(EndMarker(from))
      case _ =>
      // pass
    }
  }

  override def peek(currentStep: Long): Option[InternalElement] = {
    forwardRecoveryProgress(currentStep, false)
    Option(orderedQueue.peek())
  }

  private def isRecoveryCompleted: Boolean = !records.hasNext && nextControlToEmit == null

  private def forwardRecoveryProgress(currentStep: Long, readInput: Boolean): Unit = {
    if (replayTo != currentStep) {
      loadDeterminant()
      if (step > 0) {
        step -= 1
        println(
          s"steps to next control = $step currentstep = $currentStep readInput = $readInput waiting on $targetVId"
        )
        if (readInput) {
          val data =
            inputMapping.getOrElseUpdate(targetVId, new LinkedBlockingQueue[DataElement]()).take()
          orderedQueue.put(data)
        }
      } else if (nextControlToEmit != null) {
        orderedQueue.put(nextControlToEmit)
        nextControlToEmit = null
      }
    }
  }

  override def take(currentStep: Long): InternalElement = {
    forwardRecoveryProgress(currentStep, true)
    val res = orderedQueue.take()
    if (isRecoveryCompleted) {
      val syncFuture = new CompletableFuture[Unit]()
      actorContext.self ! ReplaceRecoveryQueue(syncFuture)
      syncFuture.get()
    }
    res
  }

  override def getDataQueueLength: Int = 0

  override def getControlQueueLength: Int = 0

  override def setDataQueueEnabled(status: Boolean): Unit = {}
}
