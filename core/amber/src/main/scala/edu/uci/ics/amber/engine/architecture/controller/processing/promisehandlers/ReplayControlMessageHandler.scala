package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.WorkflowReplayConfig
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}

object ReplayControlMessageHandler {

  final case class ContinueReplay(workflowStateRestoreConfig: WorkflowReplayConfig)
    extends ControlCommand[Unit] with SkipFaultTolerance

  //for notifying client only
  final case class WorkflowReplayCompleted() extends ControlCommand[Unit]
}


trait ReplayControlMessageHandler{
  this: ControllerAsyncRPCHandlerInitializer =>

}
