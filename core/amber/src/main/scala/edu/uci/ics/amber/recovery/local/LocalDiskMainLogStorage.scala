package edu.uci.ics.amber.recovery.local

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.recovery.MainLogStorage

class LocalDiskMainLogStorage(id:ActorVirtualIdentity) extends MainLogStorage {
  override def persistentEntireMessage(message: ControlInputPort.WorkflowControlMessage): Unit = ???

  override def persistSenderIdentifier(sender: VirtualIdentity, seq: Long): Unit = ???

  override def load(): Iterable[MainLogStorage.MainLogElement] = ???
}
