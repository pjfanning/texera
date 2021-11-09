package edu.uci.ics.texera.workflow.operators.sink.storage

import edu.uci.ics.texera.workflow.common.tuple.Tuple

trait SinkStorage {

  def getAll: Iterable[Tuple]

  def getRange(from: Int, to: Int): Iterable[Tuple]

  def getCount: Long

  def getShardedStorage(idx: Int): ShardedStorage

  def clear(): Unit
}
