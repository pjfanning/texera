package edu.uci.ics.texera.workflow.common.operators.map

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.checkpoint.{SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.worker.processing.PauseManager
import edu.uci.ics.amber.engine.common.{CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

/**
  * Common operator executor of a map() function
  * A map() function transforms one input tuple to exactly one output tuple.
  */
abstract class MapOpExec() extends OperatorExecutor with Serializable with CheckpointSupport {

  var mapFunc: Tuple => Tuple = _

  /**
    * Provides the flatMap function of this executor, it should be called in the constructor
    * If the operator executor is implemented in Java, it should be called with:
    * setMapFunc((Function1<TexeraTuple, TexeraTuple> & Serializable) func)
    */
  def setMapFunc(func: Tuple => Tuple): Unit = {
    mapFunc = func
  }

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = {
    tuple match {
      case Left(t)  => Iterator(mapFunc(t))
      case Right(_) => Iterator()
    }
  }

  override def getEstimatedCheckpointTime: Int = 0

  override def getEstimatedStateLoadTime: Int = 0

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[Int])], checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    if(currentIteratorState != null){
      val arr = currentIteratorState.toArray
      checkpoint.save("currentIter", arr)
      arr.toIterator
    }
    currentIteratorState
  }

  override def deserializeState(checkpoint: SavedCheckpoint): Iterator[(ITuple, Option[Int])] = {
    val arr:Array[(ITuple, Option[Int])] = checkpoint.load("currentIter")
    if(arr != null){
      arr.toIterator
    }else{
      null
    }
  }
}
