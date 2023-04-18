package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}


//object ReportCheckpointStatsHandler{
//  final case class ReportCheckpointStats(checkpointStats:CheckpointStats)
//    extends ControlCommand[Unit]
//      with SkipReply
//}
//
//
//
//trait ReportCheckpointStatsHandler {
//  this: ControllerAsyncRPCHandlerInitializer =>
//  registerHandler { (msg: ReportCheckpointStats, sender) =>
//    if(!cp.isReplaying){
//      cp.processingHistory.getSnapshot(msg.checkpointStats.markerId.toInt).addParticipant(sender, msg.checkpointStats)
//    }
//  }
//}
