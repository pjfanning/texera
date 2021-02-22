package edu.uci.ics.amber.engine.recovery.local

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, VirtualIdentity}
import edu.uci.ics.amber.engine.recovery.MainLogStorage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.MainLogElement

import scala.collection.mutable

class LocalDiskMainLogStorage(id: ActorVirtualIdentity) extends MainLogStorage {

  val filePath: Path = Paths.get(s"./logs/$id.logfile")
  Files.createDirectories(filePath.getParent)
  private val kryoInit = new ScalaKryoInstantiator
  kryoInit.setRegistrationRequired(false)
  private val kryo = kryoInit.newKryo()
  private lazy val controlSerializer = new Output(
    Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  )

  override def load(): Iterable[MainLogStorage.MainLogElement] = {
    // read file from disk
    if(!Files.exists(filePath)){
      return Seq.empty
    }
    val input = new Input(Files.newInputStream(filePath))
    var flag = true
    val buf = mutable.ArrayBuffer.empty[MainLogElement]
    while (flag) {
      try {
        val message = kryo.readObject(input, classOf[MainLogElement])
        buf.append(message)
      } catch {
        case e: KryoException =>
          input.close()
          flag = false
      }
    }
    buf
  }

  override def persistElement(elem: MainLogStorage.MainLogElement): Unit = {
    // write message to disk
    try {
      kryo.writeObject(controlSerializer, elem)
    }
    controlSerializer.flush()
  }

  override def clear(): Unit = ???
}
