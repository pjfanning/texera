package edu.uci.ics.amber.engine.architecture.deploysemantics.layer

import edu.uci.ics.amber.engine.architecture.worker.statistics.{WorkerState, WorkerStatistics}


// TODO: remove redundant info
class WorkerExecution(
    var state: WorkerState,
    var stats: WorkerStatistics,
) extends Serializable {

  def setState(state: WorkerState): Unit = {
    this.state = state
  }

  def setStats(stats: WorkerStatistics): Unit = {
    this.stats = stats
  }
}
