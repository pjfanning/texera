package edu.uci.ics.amber.engine.common

import edu.uci.ics.amber.engine.common.tuple.ITuple

trait CheckpointSupport {
  def serializeState(
      currentIteratorState: Iterator[(ITuple, Option[Int])],
      checkpoint: CheckpointState
  ): Iterator[(ITuple, Option[Int])]

  def deserializeState(
      checkpoint: CheckpointState
  ): Iterator[(ITuple, Option[Int])]

  def getEstimatedCheckpointTime: Int

}
