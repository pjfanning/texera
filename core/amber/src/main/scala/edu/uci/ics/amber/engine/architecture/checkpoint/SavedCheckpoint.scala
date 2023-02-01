package edu.uci.ics.amber.engine.architecture.checkpoint

import edu.uci.ics.amber.engine.architecture.controller.Workflow
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkMessage, SendRequest}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.InternalQueueElement
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import org.apache.commons.lang3.SerializationUtils

class SavedCheckpoint {

  var state: IOperatorExecutor = null
  var messages: Array[(ActorVirtualIdentity, Iterable[NetworkMessage])] = null
  val threadState: Int = 0
  var queuedData: Array[InternalQueueElement] = null

  def restoreThread(dataProcessor: DataProcessor): Unit ={

  }

  def saveThread(dataProcessor: DataProcessor): Unit ={

  }

  def saveMessages(msgs:Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]): Unit ={
    messages = msgs
  }

}
