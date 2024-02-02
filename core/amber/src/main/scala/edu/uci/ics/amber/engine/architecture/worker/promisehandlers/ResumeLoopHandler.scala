package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeLoopHandler.ResumeLoop
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.ambermessage.{DataFrame, EndOfUpstream}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.texera.workflow.operators.loop.LoopStartOpExec

object ResumeLoopHandler {
  final case class ResumeLoop() extends ControlCommand[Unit]
}

trait ResumeLoopHandler {
  this: DataProcessorRPCHandlerInitializer =>
  registerHandler { (_: ResumeLoop, _) =>
    {
      dp.processDataPayload(
        dp.currentBatchChannel,
        DataFrame(dp.operator.asInstanceOf[LoopStartOpExec].buffer.toArray)
      )
      dp.processDataPayload(dp.currentBatchChannel, EndOfUpstream())
    }
  }
}
