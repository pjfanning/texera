package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics
import edu.uci.ics.amber.engine.architecture.worker.WorkerResult
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipConsoleLog}
import edu.uci.ics.amber.engine.common.{Constants, ISinkOperatorExecutor}

object QueryStatisticsHandler {
  final case class QueryStatistics() extends ControlCommand[WorkerStatistics] with SkipConsoleLog
}

trait QueryStatisticsHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: QueryStatistics, sender) =>
    // report internal queue length if the gap > 30s
    val now = System.currentTimeMillis()
    if (now - lastReportTime > Constants.loggingQueueSizeInterval) {
      logger.info(
        s"Data Queue Length = ${dp.internalQueue.getDataQueueLength}, Control Queue Length = ${dp.internalQueue.getControlQueueLength}"
      )
      lastReportTime = now
    }

    // collect input and output row count
    val (in, out) = dp.collectStatistics()

    // sink operator doesn't output to downstream so internal count is 0
    // but for user-friendliness we show its input count as output count
    val displayOut = dp.operator match {
      case sink: ISinkOperatorExecutor =>
        in
      case _ =>
        out
    }

    val currentState = dp.stateManager.getCurrentState

    WorkerStatistics(currentState, in, displayOut)
  }

}
