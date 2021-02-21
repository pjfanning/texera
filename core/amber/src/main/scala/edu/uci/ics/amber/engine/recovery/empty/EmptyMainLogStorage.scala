package edu.uci.ics.amber.engine.recovery.empty

import edu.uci.ics.amber.engine.recovery.MainLogStorage

class EmptyMainLogStorage extends MainLogStorage {

  override def load(): Iterable[MainLogStorage.MainLogElement] = Iterable.empty

  override def persistElement(elem: MainLogStorage.MainLogElement): Unit = {}
}
