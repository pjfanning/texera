package edu.uci.ics.amber.engine.recovery

import com.google.common.collect.{BiMap, HashBiMap}
import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.common.ambermessage.DataPayload
import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{FromID, IdentifierMapping}

import scala.collection.mutable

class MainLogReplayManager(logStorage: MainLogStorage, controlInputPort: ControlInputPort) {

  def isReplaying: Boolean = !completion.isDefined

  def onComplete(callback: () => Unit): Unit = {
    completion.onSuccess(x => callback())
  }

  private val completion = new Promise[Void]()
  private val idMappingForRecovery = HashBiMap.create[VirtualIdentity, Int]()

  private val persistedDataOrder =
    logStorage
      .load()
      .flatMap {
        case msg: IdentifierMapping =>
          idMappingForRecovery.put(msg.virtualId, msg.id)
          None
        case msg: FromID => Some(msg.id)
        case msg: WorkflowControlMessage =>
          controlInputPort.handleControlMessage(msg)
          None
      }
      .to[mutable.Queue]

  private val stashedMessages = mutable.HashMap[VirtualIdentity, mutable.Queue[DataPayload]]()

  checkIfCompleted()

  def filterMessage(
      from: VirtualIdentity,
      message: DataPayload
  ): Iterable[(VirtualIdentity, DataPayload)] = {
    if (completion.isDefined) {
      return Iterable((from, message))
    }
    if (
      idMappingForRecovery
        .containsKey(from) && persistedDataOrder.head == idMappingForRecovery.get(from)
    ) {
      persistedDataOrder.dequeue()
      Iterable((from, message)) ++ checkStashedMessages()
    } else {
      if (stashedMessages.contains(from)) {
        stashedMessages(from).enqueue(message)
      } else {
        stashedMessages(from) = mutable.Queue[DataPayload](message)
      }
      Iterable.empty
    }
  }

  private[this] def checkIfCompleted(): Unit = {
    if (persistedDataOrder.isEmpty && !completion.isDefined) {
      completion.setValue(null)
    }
  }

  private[this] def checkStashedMessages(): Iterable[(VirtualIdentity, DataPayload)] = {
    val ret = mutable.ArrayBuffer[(VirtualIdentity, DataPayload)]()
    var cont = persistedDataOrder.nonEmpty
    while (cont) {
      val vid = idMappingForRecovery.inverse().get(persistedDataOrder.head)
      if (stashedMessages.contains(vid)) {
        val payload = stashedMessages(vid).dequeue()
        ret.append((vid, payload))
        persistedDataOrder.dequeue()
        cont = persistedDataOrder.nonEmpty
      } else {
        cont = false
      }
    }
    checkIfCompleted()
    ret
  }

}
