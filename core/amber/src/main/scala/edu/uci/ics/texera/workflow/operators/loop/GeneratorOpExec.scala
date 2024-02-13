package edu.uci.ics.texera.workflow.operators.loop

import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.InputExhausted
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.operators.source.SourceOperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable

class GeneratorOpExec extends SourceOperatorExecutor {
  var iteration = 0
  var buffer = new mutable.ArrayBuffer[Tuple]

  override def produceTexeraTuple(): Iterator[Tuple] = {
    tuple match {
      case Left(t) =>
        buffer.append(t)
        Iterator()
      case Right(_) => buffer.iterator
    }
  }

  override def open(): Unit = {}

  override def close(): Unit = {}
}
