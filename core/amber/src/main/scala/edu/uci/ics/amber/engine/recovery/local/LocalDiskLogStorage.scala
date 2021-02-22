package edu.uci.ics.amber.engine.recovery.local

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.esotericsoftware.kryo.KryoException
import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.ScalaKryoInstantiator
import edu.uci.ics.amber.engine.architecture.messaginglayer.ControlInputPort.WorkflowControlMessage
import edu.uci.ics.amber.engine.recovery.MainLogStorage.{FromID, IdentifierMapping}

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect._

trait LocalDiskLogStorage {

  def getFilePath: Path

  Files.createDirectories(getFilePath.getParent)
  private val kryoInit = new ScalaKryoInstantiator
  kryoInit.setRegistrationRequired(false)
  private val kryo = kryoInit.newKryo()
  kryo.register(WorkflowControlMessage.getClass)
  kryo.register(IdentifierMapping.getClass)
  kryo.register(FromID.getClass)
  private lazy val controlSerializer = new Output(
    Files.newOutputStream(getFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
  )

  def loadFromDisk[T: ClassTag](): Iterable[T] = {
    // read file from disk
    if (!Files.exists(getFilePath)) {
      return Seq.empty
    }
    val input = new Input(Files.newInputStream(getFilePath))
    var flag = true
    val buf = mutable.ArrayBuffer.empty[T]
    while (flag) {
      try {
        val message = kryo.readClassAndObject(input).asInstanceOf[T]
        buf.append(message)
      } catch {
        case e: KryoException =>
          input.close()
          flag = false
        case other =>
          throw other
      }
    }
    buf
  }

  def writeToDisk(elem: Any): Unit = {
    // write message to disk
    try {
      kryo.writeClassAndObject(controlSerializer, elem)
    } finally {
      controlSerializer.flush()
    }
  }

  def clearFile(): Unit = {
    //delete file
    if (Files.exists(getFilePath)) {
      Files.delete(getFilePath)
    }
  }

}
