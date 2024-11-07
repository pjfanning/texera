package edu.uci.ics.texera.workflow.operators.controlBlock.ifStatement

import edu.uci.ics.amber.engine.common.executor.OperatorExecutor
import edu.uci.ics.amber.engine.common.model.tuple.{Tuple, TupleLike}
import edu.uci.ics.amber.engine.common.workflow.PortIdentity

class IfStatementOpExec extends OperatorExecutor {
  var output_port: Int = _

  override def processTupleMultiPort(tuple: Tuple, port: Int): Iterator[(TupleLike, Option[PortIdentity])] = {
    port match {
      case 0 =>
        output_port = if (tuple.getField[Boolean](0)) 0 else 1
        Iterator.empty
      case 1 =>
        Iterator((tuple, Some(PortIdentity(output_port))))
    }
  }

  override def processTuple(tuple: Tuple, port: Int): Iterator[TupleLike] = ???

}
