package edu.uci.ics.texera.workflow.operators.nn

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.PauseManager
import edu.uci.ics.amber.engine.common.{AmberUtils, CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

import java.sql.Timestamp
import java.util.Calendar
import scala.collection.mutable

class MonthlyCountOpExec(outSchema:Schema) extends OperatorExecutor with Serializable with CheckpointSupport {

  var buffer = mutable.ArrayBuffer[Tuple]()
  var currentMonth:Int = 0
  var currentDate: Int = 0
  override def processTexeraTuple(tuple: Either[Tuple, InputExhausted], input: Int, pauseManager: PauseManager, asyncRPCClient: AsyncRPCClient): Iterator[Tuple] = {
    if(tuple.isLeft){
      val time = tuple.left.get.getField("create_at").asInstanceOf[Timestamp]
      var count = -1
      if(currentMonth != time.getMonth){
        currentMonth = time.getMonth
        count = buffer.size
        buffer.clear()
      }
      buffer.append(tuple.left.get)
      if(count >= 0){
        val builder = Tuple.newBuilder(outSchema).add("count", AttributeType.INTEGER, count)
        Iterator(builder.build())
      }else{
        Iterator.empty
      }
    }else{
      val count = buffer.size
      if (count > 0) {
        val builder = Tuple.newBuilder(outSchema).add("count", AttributeType.INTEGER, count)
        Iterator(builder.build())
      } else {
        Iterator.empty
      }
    }
  }

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[Int])], checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    checkpoint.save("buffer", buffer)
    checkpoint.save("currentMonth", currentMonth)
    checkpoint.save("currentYear", currentDate)
    val iterArr = currentIteratorState.toArray
    checkpoint.save("currentIter", iterArr)
    iterArr.toIterator
  }

  override def deserializeState(checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    buffer = checkpoint.load("buffer")
    currentMonth = checkpoint.load("currentMonth")
    currentDate = checkpoint.load("currentYear")
    checkpoint.load("currentIter").asInstanceOf[Array[(ITuple, Option[Int])]].toIterator
  }

  override def getEstimatedCheckpointTime: Int = {
    AmberUtils.serde.serialize(buffer).get.length
  }

  override def open(): Unit = {}

  override def close(): Unit = {}
}
