package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.worker.statistics.{WorkerState, WorkerStatistics}

case class WorkerExecution(
    var state: WorkerState,
    var statistics: WorkerStatistics
)
