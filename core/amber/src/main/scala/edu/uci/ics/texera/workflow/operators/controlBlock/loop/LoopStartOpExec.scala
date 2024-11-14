package edu.uci.ics.texera.workflow.operators.controlBlock.loop

import edu.uci.ics.amber.engine.common.executor.OperatorExecutor
import edu.uci.ics.amber.engine.common.model.tuple.{Tuple, TupleLike}

import scala.collection.mutable

class LoopStartOpExec extends OperatorExecutor {
  private val data = new mutable.ArrayBuffer[Tuple]
  private var currentIteration = 0

  def produceTupleOnIterationStart(): Iterator[TupleLike] = {
    currentIteration += 1
    data.iterator
  }

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] = {
    data.append(tuple)
    Iterator.empty
  }
}
