package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.{
  QueryWorkerResult,
  QueryWorkerStatistics
}
import edu.uci.ics.amber.engine.architecture.worker.{
  WorkerAsyncRPCHandlerInitializer,
  WorkerResult,
  WorkerStatistics
}
import edu.uci.ics.amber.engine.common.ITupleSinkOperatorExecutor

trait QueryStatisticsHandler {
  this: WorkerAsyncRPCHandlerInitializer =>

  registerHandler((msg: QueryWorkerStatistics, sender) => {
    // collect input and output row count
    val (in, out) = dataProcessor.collectStatistics()
    val state = stateManager.getCurrentState

    // sink operator doesn't output to downstream so internal count is 0
    // but for user-friendliness we show its input count as output count
    val displayOut = operator match {
      case _: ITupleSinkOperatorExecutor => in
      case _                             => out
    }

    WorkerStatistics(selfID, state, in, displayOut)
  })

  registerHandler((msg: QueryWorkerResult, sender) => {
    operator match {
      case sink: ITupleSinkOperatorExecutor =>
        Option(WorkerResult(selfID, sink.getOutputMode(), sink.getResultTuples()))
      case _ =>
        Option.empty
    }
  })

}
