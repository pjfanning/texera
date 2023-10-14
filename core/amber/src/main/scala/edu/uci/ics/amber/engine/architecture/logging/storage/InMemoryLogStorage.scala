package edu.uci.ics.amber.engine.architecture.logging.storage

import com.esotericsoftware.kryo.io.Output
import edu.uci.ics.amber.engine.architecture.logging.InMemDeterminant
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.{DeterminantLogReader, DeterminantLogWriter}
import edu.uci.ics.amber.engine.architecture.logging.storage.InMemoryLogStorage.logs
import scala.collection.mutable

object InMemoryLogStorage{
  val logs = mutable.Map[String, mutable.Queue[InMemDeterminant]]()
}


class InMemoryLogStorage(name:String) extends DeterminantLogStorage {
  if(!logs.contains(name)){
    logs(name) = new mutable.Queue[InMemDeterminant]()
  }
  override def getWriter: DeterminantLogWriter = {
    new DeterminantLogWriter(null){
      override def writeLogRecord(obj: InMemDeterminant): Unit = {
        logs(name).enqueue(obj)
      }

      override def close(): Unit = {}

      override def flush(): Unit = {}
    }
  }

  override def getReader: DeterminantLogReader = {
    new DeterminantLogReader(null){
      override def getLogs[E <: InMemDeterminant : Manifest]: mutable.Queue[E] = {
        logs(name).asInstanceOf[mutable.Queue[E]]
      }
    }
  }

  override def deleteLog(): Unit = {
    // empty
    logs.remove(name)
  }

  override def cleanPartiallyWrittenLogFile(): Unit = {
    // empty
  }

  override def isLogAvailableForRead: Boolean = logs.contains(name)
}
