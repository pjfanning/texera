package edu.uci.ics.amber.engine.architecture.logging.storage

import com.esotericsoftware.kryo.io.{Input, Output}
import com.twitter.chill.{KryoBase, KryoPool, KryoSerializer, ScalaKryoInstantiator}
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, ProcessingStep}
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.{
  DeterminantLogReader,
  DeterminantLogWriter
}
import edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlCommandV2Message.SealedValue.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.common.AmberUtils
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}

import java.io.{DataInputStream, DataOutputStream}
import scala.collection.mutable.ArrayBuffer

object DeterminantLogStorage {
  private val kryoPool = {
    val r = KryoSerializer.registerAll
    val ki = new ScalaKryoInstantiator {
      override def newKryo(): KryoBase = {
        val kryo = super.newKryo()
        kryo.register(classOf[ControlInvocation])
        kryo.register(classOf[WorkerState])
        kryo.register(classOf[ReturnInvocation])
        kryo.register(classOf[QueryStatistics])
        kryo.register(classOf[ProcessingStep])
        kryo
      }
    }.withRegistrar(r)
    KryoPool.withByteArrayOutputStream(Runtime.getRuntime.availableProcessors * 2, ki)
  }

  // For debugging purpose only
  def fetchAllLogRecords(storage: DeterminantLogStorage): Iterable[InMemDeterminant] = {
    val reader = storage.getReader
    val recordIter = reader.mkLogRecordIterator()
    val buffer = new ArrayBuffer[InMemDeterminant]()
    while (recordIter.hasNext) {
      buffer.append(recordIter.next())
    }
    buffer
  }

  class DeterminantLogWriter(outputStream: DataOutputStream) {
    lazy val output = new Output(outputStream)
    def writeLogRecord(obj: InMemDeterminant): Unit = {
      val bytes = kryoPool.toBytesWithClass(obj)
      output.writeInt(bytes.length)
      output.write(bytes)
    }
    def flush(): Unit = {
      output.flush()
    }
    def close(): Unit = {
      output.close()
    }
  }

  class DeterminantLogReader(inputStreamGen: () => DataInputStream) {
    def mkLogRecordIterator(): Iterator[InMemDeterminant] = {
      lazy val input = new Input(inputStreamGen())
      new Iterator[InMemDeterminant] {
        var record: InMemDeterminant = internalNext()
        private def internalNext(): InMemDeterminant = {
          try {
            val len = input.readInt()
            val bytes = input.readBytes(len)
            kryoPool.fromBytes(bytes).asInstanceOf[InMemDeterminant]
          } catch {
            case e: Throwable =>
              input.close()
              null
          }
        }
        override def next(): InMemDeterminant = {
          val currentRecord = record
          record = internalNext()
          currentRecord
        }
        override def hasNext: Boolean = record != null
      }
    }
  }

  def getLogStorage(storageType: String, name: String): DeterminantLogStorage = {
    storageType match {
      case "local" => new LocalFSLogStorage(name)
      case "hdfs" =>
        val hdfsIP: String =
          AmberUtils.amberConfig.getString("fault-tolerance.hdfs-storage.address")
        new HDFSLogStorage(name, hdfsIP)
      case "none" =>
        new EmptyLogStorage()
      case other => throw new RuntimeException("Cannot support log storage type of " + other)
    }
  }

}

abstract class DeterminantLogStorage {

  def getWriter: DeterminantLogWriter

  def getReader: DeterminantLogReader

  def isLogAvailableForRead: Boolean

  def deleteLog(): Unit

  def cleanPartiallyWrittenLogFile(): Unit

  protected def copyReadableLogRecords(writer: DeterminantLogWriter): Unit = {
    val recordIterator = getReader.mkLogRecordIterator()
    while (recordIterator.hasNext) {
      writer.writeLogRecord(recordIterator.next())
    }
    writer.close()
  }

}
