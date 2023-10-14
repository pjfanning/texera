package edu.uci.ics.texera.workflow.operators.nn

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.PauseManager
import edu.uci.ics.amber.engine.common.{AmberUtils, CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}

import scala.collection.mutable

class Sentiment2OpExec(outSchema:Schema) extends OperatorExecutor with Serializable with CheckpointSupport {

  var context = new mutable.Queue[(String,String)]
  val knn = new TextKNN()
  knn.addDocument("I love this product", "good")
  knn.addDocument("This is the worst product I've ever seen", "bad")
  val contextSize = 1000
  override def processTexeraTuple(tuple: Either[Tuple, InputExhausted], input: Int, pauseManager: PauseManager, asyncRPCClient: AsyncRPCClient): Iterator[Tuple] = {
    if(tuple.isLeft){
      val review = tuple.left.get.getField("reviewText").asInstanceOf[String]
      var result = "unknown"
      if(review != null){
        result = knn.predictSentiment(review)
        context.enqueue((review, result))
        if (context.size > contextSize) {
          val old = context.dequeue()
          knn.removeDocument(old._1)
        }
      }
      val builder = Tuple.newBuilder(outSchema).add("sentiment", AttributeType.STRING, result)
      Iterator(builder.build())
    }else{
      Iterator.empty
    }
  }

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[Int])], checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    checkpoint.save("context", context)
    val iterArr = currentIteratorState.toArray
    checkpoint.save("currentIter", iterArr)
    iterArr.toIterator
  }

  override def deserializeState(checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    context = checkpoint.load("context")
    checkpoint.load("currentIter").asInstanceOf[Array[(ITuple, Option[Int])]].toIterator
  }

  override def getEstimatedCheckpointTime: Int = {
    AmberUtils.serde.serialize(context).get.length
  }

  override def open(): Unit = {}

  override def close(): Unit = {}
}
