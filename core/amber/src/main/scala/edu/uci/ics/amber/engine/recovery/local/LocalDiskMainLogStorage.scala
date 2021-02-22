package edu.uci.ics.amber.engine.recovery.local

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.MainLogStorage

class LocalDiskMainLogStorage(id: ActorVirtualIdentity) extends MainLogStorage {

  override def load(): Iterable[MainLogStorage.MainLogElement] = ???

  override def persistElement(elem: MainLogStorage.MainLogElement): Unit = ???

  override def clear(): Unit = ???
}
