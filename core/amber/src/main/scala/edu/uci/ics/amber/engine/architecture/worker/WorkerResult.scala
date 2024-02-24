package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode

case class WorkerResult(
    outputMode: IncrementalOutputMode,
    result: List[TupleLike]
)
