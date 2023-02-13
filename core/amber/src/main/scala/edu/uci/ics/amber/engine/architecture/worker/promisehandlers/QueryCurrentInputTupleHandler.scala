package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple

object QueryCurrentInputTupleHandler {
  final case class QueryCurrentInputTuple() extends ControlCommand[ITuple]
}

trait QueryCurrentInputTupleHandler {
  this: DataProcessor =>

  registerHandler { (msg: QueryCurrentInputTuple, sender) =>
    getCurrentInputTuple
  }
}
