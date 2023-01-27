package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.logging.{ProcessControlMessage, SenderActorChange, StepDelta, TerminateSignal, TimeStamp}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{ControlElement, EndMarker, InputTuple, InternalQueueElement}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import lbmq.LinkedBlockingMultiQueue

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class RecoveryQueue(logReader: DeterminantLogReader) {
  private val records = logReader.mkLogRecordIterator()
  private val inputMapping = mutable
    .HashMap[ActorVirtualIdentity, LinkedBlockingQueue[InternalQueueElement]]()
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ControlElement]]()
  private var step = 0L
  var totalStep = 0L
  private var targetVId: ActorVirtualIdentity = _
  private var cleaned = false
  private val callbacksOnEnd = new ArrayBuffer[() => Unit]()
  private var endCallbackTriggered = false
  private var nextRecordToEmit: InternalQueueElement = _

  private var replayTo = -1L
  def setReplayTo(dest: Long, unblock:Boolean): Unit = {
    replayTo = dest
    if(unblock){
      releaseFlag.complete()
    }
  }
  var releaseFlag = new CompletableFuture[Unit]()

  // calling it first to get nextRecordToEmit ready
  // we assume the log has the following structure:
  // Ctrl -> [StepDelta] -> Ctrl -> [StepDelta] -> EOF|Ctrl
  processInternalEventsTillNextControl()

  def registerOnEnd(callback: () => Unit): Unit = {
    callbacksOnEnd.append(callback)
  }

  def isReplayCompleted: Boolean = {
    val res = !records.hasNext && nextRecordToEmit == null
    if (res && !endCallbackTriggered) {
      endCallbackTriggered = true
      callbacksOnEnd.foreach(callback => callback())
    }
    res
  }

  def drainAllStashedElements(
      dataQueue: LinkedBlockingMultiQueue[Int, InternalQueueElement]#SubQueue,
      controlQueue: LinkedBlockingMultiQueue[Int, InternalQueueElement]#SubQueue
  ): Unit = {
    if (!cleaned) {
      getAllStashedInputs.foreach(dataQueue.add)
      getAllStashedControls.foreach(controlQueue.add)
      cleaned = true
    }
  }

  def add(elem: InternalQueueElement): Unit = {
    elem match {
      case tuple: InputTuple =>
        inputMapping
          .getOrElseUpdate(tuple.from, new LinkedBlockingQueue[InternalQueueElement]())
          .put(tuple)
      case control: ControlElement =>
        controlMessages
          .getOrElseUpdate(control.from, new mutable.Queue[ControlElement]())
          .enqueue(control)
      case WorkerInternalQueue.EndMarker(from) =>
        inputMapping
          .getOrElseUpdate(from, new LinkedBlockingQueue[InternalQueueElement]())
          .put(EndMarker(from))
    }
  }

  private def getAllStashedInputs: Iterable[InternalQueueElement] = {
    val res = new ArrayBuffer[InternalQueueElement]
    inputMapping.values.foreach { x =>
      while (!x.isEmpty) {
        res.append(x.take())
      }
    }
    res
  }

  private def getAllStashedControls: Iterable[ControlElement] = {
    val res = new ArrayBuffer[ControlElement]
    controlMessages.foreach { x =>
      while (x._2.nonEmpty) {
        res.append(x._2.dequeue())
      }
    }
    res
  }

  def isReadyToEmitNextControl: Boolean = {
    if(step == 0){
      return true
    }
    totalStep += 1
    if (replayTo == totalStep) {
      println("reach total step at "+replayTo+" step = "+step)
      releaseFlag.get()
      releaseFlag = new CompletableFuture[Unit]()
      println("exit total step at "+replayTo)
    }
    step -= 1
    step == 0
  }

  private def processInternalEventsTillNextControl(): Unit = {
    var stop = false
    while (records.hasNext && !stop) {
      records.next() match {
        case StepDelta(steps) =>
          step += steps
        case SenderActorChange(actorVirtualIdentity) =>
          targetVId = actorVirtualIdentity
        case ProcessControlMessage(controlPayload, from) =>
          nextRecordToEmit = ControlElement(controlPayload, from)
          stop = true
        case TimeStamp(value) => ???
        case TerminateSignal  => throw new RuntimeException("Cannot handle terminate signal here.")
      }
    }
  }

  def get(): InternalQueueElement = {
    if(step != 0){
      totalStep += 1
      if (replayTo == totalStep) {
        println("reach total step at "+replayTo+" step = "+step)
        releaseFlag.get()
        releaseFlag = new CompletableFuture[Unit]()
        println("exit total step at "+replayTo)
      }
      step -= 1
    }
    if (step > 0) {
      //wait until input[targetVId] available
      inputMapping
        .getOrElseUpdate(targetVId, new LinkedBlockingQueue[InternalQueueElement]())
        .take()
    } else {
      val res = nextRecordToEmit
      nextRecordToEmit = null
      processInternalEventsTillNextControl()
      res
    }
  }
}
