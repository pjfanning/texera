package edu.uci.ics.amber.engine.common

import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.amber.engine.common.workflow.PortIdentity

trait ISinkOperatorExecutor extends IOperatorExecutor {

  override def processTuple(
                             tuple: Either[TupleLike, InputExhausted],
                             input: Int,
                             pauseManager: PauseManager,
                             asyncRPCClient: AsyncRPCClient
  ): Iterator[(TupleLike, Option[PortIdentity])] = {
    consume(tuple, input)
    Iterator.empty
  }

  def consume(tuple: Either[TupleLike, InputExhausted], input: Int): Unit
}
