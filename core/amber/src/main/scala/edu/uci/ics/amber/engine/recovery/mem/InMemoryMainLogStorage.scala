package edu.uci.ics.amber.engine.recovery.mem

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.MainLogStorage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{DataMessageIdentifier, MainLogElement}

class InMemoryMainLogStorage(id:ActorVirtualIdentity) extends MainLogStorage {

  private val idString: String = id.toString

  override def persistentEntireMessage(message: ControlInputPort.WorkflowControlMessage): Unit = {
    InMemoryLogStorage.getMainLogOf(idString).enqueue(message)
  }

  override def persistSenderIdentifier(sender: VirtualIdentity, seq: Long): Unit = {
    InMemoryLogStorage.getMainLogOf(idString).enqueue(DataMessageIdentifier(sender,seq))
  }

  override def load(): Iterable[MainLogStorage.MainLogElement] = {
    InMemoryLogStorage.getMainLogOf(idString)
  }
}
