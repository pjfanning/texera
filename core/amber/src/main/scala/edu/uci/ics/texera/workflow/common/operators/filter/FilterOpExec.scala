package edu.uci.ics.texera.workflow.common.operators.filter

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.processing.PauseManager
import edu.uci.ics.amber.engine.common.{CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

abstract class FilterOpExec() extends OperatorExecutor with CheckpointSupport with Serializable {

  var filterFunc: Tuple => java.lang.Boolean = _

  def setFilterFunc(func: Tuple => java.lang.Boolean): Unit = {
    this.filterFunc = func
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
      case Left(t)  => if (filterFunc(t)) Iterator(t) else Iterator()
      case Right(_) => Iterator()
    }
  }

  override def serializeState(
                      currentIteratorState: Iterator[(ITuple, Option[Int])],
                      checkpoint: SavedCheckpoint
                    ): Iterator[(ITuple, Option[Int])] = {
    val iteratorToArr = currentIteratorState.toArray
    checkpoint.save(
      "currentIterator",
      iteratorToArr
    )
    iteratorToArr.toIterator
  }

  override def deserializeState(
                        checkpoint: SavedCheckpoint
                      ): Iterator[(ITuple, Option[Int])] = {
    val arr: Array[(ITuple, Option[Int])] = checkpoint.load("currentIterator")
    arr.toIterator
  }

  override def getEstimatedCheckpointTime: Int = 0

  override def getEstimatedStateLoadTime: Int = 0

}
