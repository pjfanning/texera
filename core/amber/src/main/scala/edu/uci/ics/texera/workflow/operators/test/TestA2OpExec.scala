package edu.uci.ics.texera.workflow.operators.test

import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.State
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType

import scala.collection.mutable

class TestA2OpExec extends OperatorExecutor {
  var buffer = new mutable.ArrayBuffer[Tuple]()

  override def processState(state: State, port: Int): State = {
    val state = State()
    state.add("state1 from A2", AttributeType.STRING, "before process tuple")
    state
  }

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] = {
    buffer += tuple
    Iterator(tuple)
  }

  override def produceState(): State = {
    val state = State()
    state.add("state2 from A2", AttributeType.STRING, "after process tuple")
    state
  }

  override def onFinish(port: Int): Iterator[TupleLike] = {
    buffer.iterator
  }
}
