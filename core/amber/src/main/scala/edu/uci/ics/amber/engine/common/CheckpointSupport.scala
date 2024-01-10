package edu.uci.ics.amber.engine.common

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
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

