package edu.uci.ics.texera.workflow.operators.sortPartitions

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.{PauseManager, PauseType}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.{AmberUtils, CheckpointSupport, Constants, InputExhausted}
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, OperatorSchemaInfo}

import java.sql.Timestamp
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SortPartitionOpExec(
    sortAttributeName: String
) extends OperatorExecutor with CheckpointSupport {

  var unorderedTuples: ArrayBuffer[Tuple] = new ArrayBuffer[Tuple]()
  var calls = 0
  var emitted = 0
  var queue:mutable.Queue[Tuple] = _

  def sortTuples(): Iterator[Tuple] = {
    new Iterator[Tuple] {
      override def hasNext: Boolean = queue.nonEmpty

      override def next(): Tuple = {
        Thread.sleep(1)
        queue.dequeue()
      }
    }
  }

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = {
    tuple match {
      case Left(t) =>
        unorderedTuples.append(t)
        Iterator()
      case Right(_) =>
        queue = mutable.Queue(unorderedTuples.sortWith(compareTuples):_*)
        unorderedTuples.clear()
        sortTuples()
    }
  }

  def compareTuples(t1: Tuple, t2: Tuple): Boolean = {
    val attributeType = t1.getSchema().getAttribute(sortAttributeName).getType()
    val attributeIndex = t1.getSchema().getIndex(sortAttributeName)
    attributeType match {
      case AttributeType.LONG =>
        t1.getLong(attributeIndex) < t2.getLong(attributeIndex)
      case AttributeType.INTEGER =>
        t1.getInt(attributeIndex) < t2.getInt(attributeIndex)
      case AttributeType.DOUBLE =>
        t1.getDouble(attributeIndex) < t2.getDouble(attributeIndex)
      case AttributeType.TIMESTAMP =>
        t1.get(attributeIndex).asInstanceOf[Timestamp].getTime < t2.get(attributeIndex).asInstanceOf[Timestamp].getTime
      case _ =>
        true // unsupported type
    }
  }

  override def open = {
    unorderedTuples = new ArrayBuffer[Tuple]()
  }

  override def close = {
    unorderedTuples.clear()
  }

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[Int])], checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    currentIteratorState
  }

  override def deserializeState(checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    Iterator.empty
  }

  override def getEstimatedCheckpointTime: Int = {
    if(queue == null){
      AmberUtils.serde.serialize(unorderedTuples).get.length
    }else{
      AmberUtils.serde.serialize(queue).get.length
    }
  }
}
