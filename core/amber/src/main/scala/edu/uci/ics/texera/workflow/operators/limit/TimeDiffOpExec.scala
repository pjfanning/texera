package edu.uci.ics.texera.workflow.operators.limit

import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.{CheckpointState, CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema}

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import scala.collection.mutable

class TimeDiffOpExec(schema:Schema) extends OperatorExecutor with CheckpointSupport {

  var lastTimestamp = mutable.HashMap[String, (String, Timestamp)]()
  override def processTexeraTuple(tuple: Either[Tuple, InputExhausted], input: Int, pauseManager: PauseManager, asyncRPCClient: AsyncRPCClient): Iterator[Tuple] = {
    tuple match {
      case Left(value) =>
        val key = value.getField[String]("customerId")
        val currentTime = value.getField[Timestamp]("timestamp")
        val currentStatus = value.getField[String]("customerStatus")
        if(lastTimestamp.contains(key)){
          Thread.sleep(10)
          val builder = Tuple.newBuilder(schema)
          val diff = Duration.between(lastTimestamp(key)._2.toInstant, currentTime.toInstant)
          lastTimestamp(key) = (currentStatus, currentTime)
          if(currentStatus.toLowerCase == "start watching"){
            Iterator.empty
          }else{
            val ret = builder.add("action", AttributeType.STRING, "watching").add("diff", AttributeType.LONG, diff.toMinutes).build()
            Iterator(ret)
          }
        }else{
          lastTimestamp(key) = (currentStatus, currentTime)
          Iterator()
        }
      case Right(value) => Iterator()
    }
  }

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[PortIdentity])], checkpoint: CheckpointState): Iterator[(ITuple, Option[PortIdentity])] = {
    checkpoint.save("lastTimestamp", lastTimestamp)
    val arr = currentIteratorState.toArray
    checkpoint.save("currentIter", arr)
    arr.toIterator
  }

  override def deserializeState(checkpoint: CheckpointState): Iterator[(ITuple, Option[PortIdentity])] = {
    lastTimestamp = checkpoint.load("lastTimestamp")
    checkpoint.load("currentIter").asInstanceOf[Array[(ITuple, Option[PortIdentity])]].toIterator
  }

  override def getEstimatedCheckpointCost: Long = 0L

  override def getState: String = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
    s"customerId,action,timestamp\n" +
      lastTimestamp.toSeq.sortBy(_._1).map {
        case (customerId, values) =>
          s"${customerId},${values._1},${format.format(values._2)}"
      }.mkString("\n")
  }
}
