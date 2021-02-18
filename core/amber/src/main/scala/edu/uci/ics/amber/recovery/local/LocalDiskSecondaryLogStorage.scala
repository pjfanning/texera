package edu.uci.ics.amber.recovery.local

import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.recovery.SecondaryLogStorage

class LocalDiskSecondaryLogStorage(id:ActorVirtualIdentity) extends SecondaryLogStorage {
  override def persistCurrentDataCursor(dataCursor: Long): Unit = ???

  override def load(): Iterable[Long] = ???
}
