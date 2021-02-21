package edu.uci.ics.amber.engine.recovery.mem

import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.MainLogStorage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.MainLogElement

class InMemoryMainLogStorage(id: ActorVirtualIdentity) extends MainLogStorage {

  private val idString: String = id.toString

  override def persistElement(elem: MainLogElement): Unit = {
    InMemoryLogStorage.getMainLogOf(idString).enqueue(elem)
  }

  override def load(): Iterable[MainLogStorage.MainLogElement] = {
    InMemoryLogStorage.getMainLogOf(idString)
  }
}
