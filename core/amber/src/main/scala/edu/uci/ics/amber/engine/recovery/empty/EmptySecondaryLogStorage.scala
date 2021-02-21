package edu.uci.ics.amber.engine.recovery.empty

import edu.uci.ics.amber.engine.recovery.SecondaryLogStorage

class EmptySecondaryLogStorage extends SecondaryLogStorage {
  override def persistCurrentDataCursor(dataCursor: Long): Unit = {}

  override def load(): Iterable[Long] = Iterable.empty
}
