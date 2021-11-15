package edu.uci.ics.texera.workflow.operators.sink.storage

import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable.ArrayBuffer

class MemoryStorage extends SinkStorage with ShardedStorage {

  private val results = new ArrayBuffer[Tuple]()

  override def getAll: Iterable[Tuple] =
    synchronized {
      results
    }

  override def putOne(tuple: Tuple): Unit =
    synchronized {
      results += tuple
    }

  override def clear(): Unit =
    synchronized {
      results.clear()
    }

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def getShardedStorage(idx: Int): ShardedStorage = this

  override def getRange(from: Int, to: Int): Iterable[Tuple] =
    synchronized {
      results.slice(from, to)
    }

  override def getCount: Long = results.length
}
