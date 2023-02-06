package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.DeterminantLogReader
import edu.uci.ics.amber.engine.architecture.logging._
import edu.uci.ics.amber.engine.architecture.messaginglayer.CreditMonitor
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor._
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import java.util.concurrent.{CompletableFuture, LinkedBlockingQueue}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class InputHub(creditMonitor: CreditMonitor) extends Serializable{

  @transient
  private var records:Iterator[InMemDeterminant] = _
  @transient
  var recoveryCompleted: Boolean = true

  private var numRecordsRead: Int = 0
  private val inputMapping = mutable
    .HashMap[ActorVirtualIdentity, LinkedBlockingQueue[DataElement]]()
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ControlElement]]()
  private var step = 0L
  private var targetVId: ActorVirtualIdentity = _
  private val callbacksOnEnd = new ArrayBuffer[() => Unit]()
  private var replayTo = -1L
  private var nextControlToEmit: ControlElement = _

  val dataDeque = new ProactiveDeque[DataElement]({
    case InputTuple(from, _) => creditMonitor.increaseCredit(from)
    case other => //pass
  }, {
    case InputTuple(from, _) => creditMonitor.decreaseCredit(from)
    case other => //pass
  })
  val controlDeque = new ProactiveDeque[ControlElement]()
  val internalDeque = new ProactiveDeque[InternalCommand]()

  def setReplayTo(dest: Long, unblock:Boolean): Unit = {
    replayTo = dest
  }

  def setLogRecords(records:Iterator[InMemDeterminant]): Unit ={
    this.records = records.drop(numRecordsRead)
    recoveryCompleted = !records.hasNext
    // we assume the log has the following structure:
    // Ctrl -> [StepDelta] -> Ctrl -> [StepDelta] -> EOF|Ctrl
    processInternalEventsTillNextControl()
  }

  def registerOnEnd(callback: () => Unit): Unit = {
    callbacksOnEnd.append(callback)
  }

  def addInternal(elem:InternalCommand): Unit ={
    internalDeque.enqueue(elem)
  }

  def addData(elem: DataElement): Unit = {
    synchronized {
      if (recoveryCompleted) {
        dataDeque.enqueue(elem)
        return
      }
      elem match {
        case tuple: InputTuple =>
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
  }

  def addControl(control: ControlElement): Unit ={
    synchronized {
      if (recoveryCompleted) {
        controlDeque.enqueue(control)
        return
      }
      controlMessages
        .getOrElseUpdate(control.from, new mutable.Queue[ControlElement]())
        .enqueue(control)
    }
  }

  private def getAllStashedInputs: Iterable[DataElement] = {
    val res = new ArrayBuffer[DataElement]
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


  def prepareInput(totalValidStep: Long, outputAvailable: Boolean): Boolean ={
    if(recoveryCompleted){
      return false
    }
    if(totalValidStep == replayTo){
      // replay point reached
      // use internal command no operation to trigger replay again
      return true
    }
    val res = !records.hasNext && nextControlToEmit == null
    if (res && !recoveryCompleted) {
      synchronized {
        recoveryCompleted = true
        getAllStashedInputs.foreach(dataDeque.enqueue)
        getAllStashedControls.foreach(controlDeque.enqueue)
        callbacksOnEnd.foreach(callback => callback())
      }
    }
    if(!recoveryCompleted){
      if(step > 0){
        step -= 1
        println(s"current step = $step")
        fetchDataFromUpstream(outputAvailable)
      }else if(step == 0){
        controlDeque.enqueue(nextControlToEmit)
        nextControlToEmit = null
        processInternalEventsTillNextControl()
      }
    }
    false
  }

  def fetchDataFromUpstream(outputAvailable:Boolean): Unit ={
    if(!outputAvailable && dataDeque.size() == 0) {
      println(s"waiting on ${targetVId}")
      val data = inputMapping.getOrElseUpdate(targetVId, new LinkedBlockingQueue[DataElement]()).take()
      dataDeque.enqueue(data)
    }
  }

  private def processInternalEventsTillNextControl(): Unit = {
    var stop = step > 0 || nextControlToEmit != null
    while (records.hasNext && !stop) {
      records.next() match {
        case StepDelta(steps) =>
          step += steps
        case SenderActorChange(actorVirtualIdentity) =>
          targetVId = actorVirtualIdentity
        case ProcessControlMessage(controlPayload, from) =>
          nextControlToEmit = ControlElement(controlPayload, from)
          stop = true
        case TimeStamp(value) => ???
        case TerminateSignal  => throw new RuntimeException("Cannot handle terminate signal here.")
      }
      numRecordsRead += 1
    }
  }

  def getDataQueueLength: Int = dataDeque.size()
  def getControlQueueLength: Int = controlDeque.size()

}
