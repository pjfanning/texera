package edu.uci.ics.amber.engine.recovery.local

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.MainLogStorage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.MainLogElement

import scala.collection.mutable

class LocalDiskMainLogStorage(id: ActorVirtualIdentity)
    extends MainLogStorage
    with LocalDiskLogStorage {
  override def persistElement(elem: MainLogElement): Unit = writeToDisk(elem)

  override def load(): Iterable[MainLogElement] = loadFromDisk[MainLogElement]()

  override def clear(): Unit = clearFile()

  override lazy val getFilePath: Path = Paths.get(s"./logs/main/$id.logfile")
}
