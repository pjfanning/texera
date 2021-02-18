package edu.uci.ics.amber.recovery

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage
import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity
import edu.uci.ics.amber.recovery.MainLogStorage.{DataMessageIdentifier, MainLogElement}

import scala.collection.mutable

class MainLogReplayManager(logStorage:MainLogStorage, controlInputPort: ControlInputPort) {

  private val persistedMessages = logStorage.load()

  persistedMessages.collect {
    case msg:WorkflowControlMessage => controlInputPort.handleControlMessage(msg)
  }

  private val persistedDataOrder = persistedMessages.collect {case msg: DataMessageIdentifier => msg}.to[mutable.Queue]
  private val stashedMessages = mutable.HashMap[DataMessageIdentifier, WorkflowMessage]()

  def filterMessage(messageIn:WorkflowMessage):Iterable[WorkflowMessage] = {
    val messageIdentifier = DataMessageIdentifier(messageIn.from, messageIn.sequenceNumber)
    if(persistedDataOrder.head == messageIdentifier){
      persistedDataOrder.dequeue()
      Iterable(messageIn) ++ checkStashedMessages()
    }else{
      stashedMessages(messageIdentifier) = messageIn
      Iterable.empty
    }
  }


  private def checkStashedMessages():Iterable[WorkflowMessage] = {
    val ret = mutable.ArrayBuffer[WorkflowMessage]()
    while(stashedMessages.contains(persistedDataOrder.head)){
      ret.append(stashedMessages.remove(persistedDataOrder.head).get)
    }
    ret
  }

}
