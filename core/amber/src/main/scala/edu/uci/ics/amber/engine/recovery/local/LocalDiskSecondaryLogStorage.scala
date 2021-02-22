package edu.uci.ics.amber.engine.recovery.local

import java.nio.file.{Path, Paths}

import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.SecondaryLogStorage

class LocalDiskSecondaryLogStorage(id: ActorVirtualIdentity)
    extends SecondaryLogStorage
    with LocalDiskLogStorage {
  override def persistCurrentDataCursor(dataCursor: Long): Unit = writeToDisk(dataCursor)

  override def load(): Iterable[Long] = loadFromDisk[Long]()

  override def clear(): Unit = clearFile()

  override lazy val getFilePath: Path = Paths.get(s"./logs/secondary/$id.logfile")
}
