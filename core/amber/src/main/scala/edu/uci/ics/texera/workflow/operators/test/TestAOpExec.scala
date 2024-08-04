package edu.uci.ics.texera.workflow.operators.test

import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.State
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType

class TestAOpExec extends OperatorExecutor {

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] = {
      Iterator(tuple)
  }

  override def produceState(): State = {
    val state = State()
    state.add("count", AttributeType.STRING, "test")
    state
  }
}
