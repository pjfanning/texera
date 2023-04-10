package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.ReportCheckpointStatsHandler.ReportCheckpointStats
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}


object ReportCheckpointStatsHandler{
  final case class ReportCheckpointStats(checkpointStats:CheckpointStats)
    extends ControlCommand[Unit]
      with SkipFaultTolerance
      with SkipReply
}



trait ReportCheckpointStatsHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: ReportCheckpointStats, sender) =>
    if(!cp.isReplaying){
      cp.interactionHistory.getSnapshot(msg.checkpointStats.markerId.toInt).addParticipant(sender, msg.checkpointStats)
    }
  }
}
