package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.logging._
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{
  ControlElement,
  DataElement,
  EndMarker,
  InputTuple,
  InternalQueueElement
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF
import lbmq.LinkedBlockingMultiQueue

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import scala.collection.mutable

class RecoveryInternalQueueImpl(creditMonitor: CreditMonitor) extends WorkerInternalQueue {

  @transient
  private var records: Iterator[InMemDeterminant] = _
  @transient
  private var onRecoveryComplete: () => Unit = _
  @transient
  private var onReplayComplete: () => Unit = _
  @transient
  private var orderedQueue: LinkedBlockingQueue[InternalQueueElement] = _

  private val inputMapping = mutable
    .HashMap[ActorVirtualIdentity, LinkedBlockingQueue[DataElement]]()
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ControlElement]]()
  private var step = 0L
  private var targetVId: ActorVirtualIdentity = _
  private var replayTo = -1L
  private var nextControlToEmit: ControlElement = _

  private var recordRead = 0L

  val registeredInputs = new mutable.HashSet[String]()

  def setReplayTo(dest: Long, onReplayEnd: () => Unit): Unit = {
    replayTo = dest
    onReplayComplete = onReplayEnd
  }

  def initialize(
      records: Iterator[InMemDeterminant],
      currentDPStep: Long,
      onRecoveryCompleted: () => Unit
  ): Unit = {
    this.orderedQueue = new LinkedBlockingQueue[InternalQueueElement]()
    this.onRecoveryComplete = onRecoveryCompleted
    // restore replay progress by dropping some of the entries
    val copiedRead = recordRead
    recordRead = 0
    var accumulatedSteps = 0L
    this.records = records
    while (accumulatedSteps < currentDPStep) {
      loadDeterminant()
      accumulatedSteps += (if (step == 0) 1 else step)
      step = 0
    }
    println(s"Internal Queue: accumulated step = $accumulatedSteps actual steps = $currentDPStep")
    if (accumulatedSteps > currentDPStep) {
      step = accumulatedSteps - currentDPStep
    }
    println(s"Internal Queue: recovered read = $recordRead actual read = $copiedRead")
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
    val n = records.next()
    //println(s"read: ${n}")
    n match {
      case StepDelta(sender, steps) =>
        targetVId = sender
        step = steps
      case ProcessControlMessage(controlPayload, from) =>
        nextControlToEmit = ControlElement(controlPayload, from)
      case TimeStamp(value) => ???
      case TerminateSignal  => throw new RuntimeException("Cannot handle terminate signal here.")
    }
    recordRead += 1
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

  override def peek(currentStep: Long): Option[InternalQueueElement] = {
    forwardRecoveryProgress(currentStep, false)
    Option(orderedQueue.peek())
  }

  private def isRecoveryCompleted: Boolean = !records.hasNext && nextControlToEmit == null

  private def forwardRecoveryProgress(currentStep: Long, readInput: Boolean): Unit = {
    if (replayTo != currentStep) {
      if (orderedQueue.isEmpty && step == 0 && records.hasNext) {
        loadDeterminant()
      }
      if(replayTo - currentStep < 100){
        println(
          s"replayInfo: replayTo = ${replayTo} Q empty = ${orderedQueue.isEmpty} controlToEmit = $nextControlToEmit steps to next control = $step currentstep = $currentStep readInput = $readInput waiting on $targetVId"
        )
      }
      if (step > 0) {
        step -= 1
        if (readInput) {
          val data =
            inputMapping.getOrElseUpdate(targetVId, new LinkedBlockingQueue[DataElement]()).take()
          orderedQueue.put(data)
        }
      } else if (nextControlToEmit != null) {
        orderedQueue.put(nextControlToEmit)
        nextControlToEmit = null
      }
    }else{
        println(
          s"replayInfo: replayTo = ${replayTo} Q empty = ${orderedQueue.isEmpty} controlToEmit = $nextControlToEmit steps to next control = $step currentstep = $currentStep readInput = $readInput waiting on $targetVId"
        )
      if(onReplayComplete != null){
        onReplayComplete()
        onReplayComplete = null
      }
    }
  }

  override def take(currentStep: Long): InternalQueueElement = {
    forwardRecoveryProgress(currentStep, true)
    val res = orderedQueue.take()
    if (isRecoveryCompleted && onRecoveryComplete != null) {
      onRecoveryComplete()
    }
    res
  }

  override def getDataQueueLength: Int = 0

  override def getControlQueueLength: Int = 0

  private[architecture] override def dataQueues
      : mutable.HashMap[String, LinkedBlockingMultiQueue[String, InternalQueueElement]#SubQueue] =
    mutable.HashMap.empty

  override def registerInput(sender: String): Unit = {
    registeredInputs.add(sender)
  }
}
