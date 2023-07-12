package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

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
