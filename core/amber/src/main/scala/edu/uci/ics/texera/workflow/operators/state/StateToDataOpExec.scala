package edu.uci.ics.texera.workflow.operators.state

import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.State
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

class StateToDataOpExec extends OperatorExecutor {
  private var stateTuple: Option[Tuple] = None

  override def processState(state: State, port: Int): Option[State] = {
    stateTuple = Some(state.toTuple)
    None
  }

  override def processTupleMultiPort(
      tuple: Tuple,
      port: Int
  ): Iterator[(TupleLike, Option[PortIdentity])] = {
    if (stateTuple.isDefined) {
      val outputTuple = stateTuple.get
      stateTuple = None
      Array((outputTuple, Some(PortIdentity())), (tuple, Some(PortIdentity(1)))).iterator
    } else {
      Iterator((tuple, Some(PortIdentity(1))))
    }
  }

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] =
    throw new NotImplementedError()
}
