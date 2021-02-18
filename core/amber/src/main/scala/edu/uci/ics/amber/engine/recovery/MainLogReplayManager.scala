package edu.uci.ics.amber.engine.recovery

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.architecture.messaginglayer.DataInputPort.WorkflowDataMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{DataMessageIdentifier, MainLogElement}

import scala.collection.mutable

class MainLogReplayManager(logStorage: MainLogStorage, controlInputPort: ControlInputPort) {

  def isReplaying: Boolean = !completion.isDefined

  def onComplete(callback:() => Unit): Unit ={
    completion.onSuccess(x => callback())
  }

  private val completion = new Promise[Void]()

  private val persistedDataOrder =
    logStorage.load().flatMap {
      case msg: DataMessageIdentifier => Some(msg)
      case msg: WorkflowControlMessage =>
        controlInputPort.handleControlMessage(msg)
        None
    }.to[mutable.Queue[DataMessageIdentifier]]

  private val stashedMessages = mutable.HashMap[DataMessageIdentifier, WorkflowDataMessage]()

  checkIfCompleted()

  def filterMessage(messageIn: WorkflowDataMessage): Iterable[WorkflowDataMessage] = {
    if(completion.isDefined){
      return Iterable(messageIn)
    }
    val messageIdentifier = DataMessageIdentifier(messageIn.from, messageIn.sequenceNumber)
    if (persistedDataOrder.head == messageIdentifier) {
      persistedDataOrder.dequeue()
      Iterable(messageIn) ++ checkStashedMessages()
    } else {
      stashedMessages(messageIdentifier) = messageIn
      Iterable.empty
    }
  }

  private[this] def checkIfCompleted(): Unit ={
    if(persistedDataOrder.isEmpty && !completion.isDefined){
      completion.setValue(null)
    }
  }

  private[this] def checkStashedMessages(): Iterable[WorkflowDataMessage] = {
    val ret = mutable.ArrayBuffer[WorkflowDataMessage]()
    while (persistedDataOrder.nonEmpty && stashedMessages.contains(persistedDataOrder.head)) {
      ret.append(stashedMessages.remove(persistedDataOrder.head).get)
      persistedDataOrder.dequeue()
    }
    checkIfCompleted()
    ret
  }

}
