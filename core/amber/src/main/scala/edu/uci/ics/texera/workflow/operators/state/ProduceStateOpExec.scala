package edu.uci.ics.texera.workflow.operators.state

import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.State
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType

class ProduceStateOpExec extends OperatorExecutor {

  override def onStartProduceState(port: Int): Option[State] = {
    val state = State()
    state.add("i", 1, AttributeType.INTEGER)
    Some(state)
  }

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] = {
    Iterator(tuple)
  }

}
