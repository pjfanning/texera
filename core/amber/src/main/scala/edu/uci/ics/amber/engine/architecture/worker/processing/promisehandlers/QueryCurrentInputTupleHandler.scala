package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple

object QueryCurrentInputTupleHandler {
  final case class QueryCurrentInputTuple() extends ControlCommand[ITuple]
}

trait QueryCurrentInputTupleHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: QueryCurrentInputTuple, sender) =>
    dp.getCurrentInputTuple
  }
}
