package edu.uci.ics.texera.workflow.operators.sink.managed

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.checkpoint.{SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.amber.engine.common.{CheckpointSupport, ISinkOperatorExecutor, InputExhausted}
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode._
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo
import edu.uci.ics.texera.workflow.common.{IncrementalOutputMode, ProgressiveUtils}
import edu.uci.ics.texera.workflow.operators.sink.storage.{SinkStorageReader, SinkStorageWriter}

import scala.collection.mutable

class ProgressiveSinkOpExec(
    val operatorSchemaInfo: OperatorSchemaInfo,
    val outputMode: IncrementalOutputMode,
    val storage: SinkStorageWriter
) extends ISinkOperatorExecutor
    with CheckpointSupport {

  var numTupleIntoStorage = 0

  override def open(): Unit = storage.open()

  override def close(): Unit = storage.close()

  override def consume(
      tuple: Either[ITuple, InputExhausted],
      input: Int
  ): Unit = {
    tuple match {
      case Left(t) =>
        numTupleIntoStorage += 1
        outputMode match {
          case SET_SNAPSHOT =>
            updateSetSnapshot(t.asInstanceOf[Tuple])
          case SET_DELTA =>
            storage.putOne(t.asInstanceOf[Tuple])
        }
      case Right(_) => // skip
    }
  }

  private def updateSetSnapshot(deltaUpdate: Tuple): Unit = {
    val (isInsertion, tupleValue) =
      ProgressiveUtils.getTupleFlagAndValue(deltaUpdate, operatorSchemaInfo)
    if (isInsertion) {
      storage.putOne(tupleValue)
    } else {
      storage.removeOne(tupleValue)
    }
  }

  override def getStateInformation: String = {
    s"Sink: number of tuple put into the storage is ${numTupleIntoStorage}"
  }

  override def serializeState(
      currentIteratorState: Iterator[(ITuple, Option[Int])],
      checkpoint: SavedCheckpoint
  ): Iterator[(ITuple, Option[Int])] = {
    checkpoint.save(
      "numTupleIntoStorage", numTupleIntoStorage)
    currentIteratorState
  }

  override def deserializeState(
      checkpoint: SavedCheckpoint
  ): Iterator[(ITuple, Option[Int])] = {
    open()
    numTupleIntoStorage = checkpoint.load("numTupleIntoStorage")
    Iterator.empty
  }

  override def getEstimatedCheckpointTime: Int = 0

  override def getEstimatedStateLoadTime: Int = 1
}
