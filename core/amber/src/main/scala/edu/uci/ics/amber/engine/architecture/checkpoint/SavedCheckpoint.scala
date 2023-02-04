package edu.uci.ics.amber.engine.architecture.checkpoint


import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkMessage
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import org.apache.commons.lang3.SerializationUtils

class SavedCheckpoint {

  var DPstate:DataProcessor = null
  var messages: Array[(ActorVirtualIdentity, Iterable[NetworkMessage])] = null

  def saveThread(dataProcessor: DataProcessor): Unit ={
    DPstate = SerializationUtils.clone(dataProcessor)
  }

  def saveMessages(msgs:Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]): Unit ={
    messages = msgs
  }

}
