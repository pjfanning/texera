package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.EndOfIteration
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ResumeLoopHandler.{
  ResumeLoop,
  loopToSelfChannelId
}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.ambermessage.{DataFrame, EndOfUpstream}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  OperatorIdentity,
  PhysicalOpIdentity
}
import edu.uci.ics.amber.engine.common.workflow.{PhysicalLink, PortIdentity}
import edu.uci.ics.texera.workflow.operators.loop.LoopStartOpExec

object ResumeLoopHandler {
  final case class ResumeLoop() extends ControlCommand[Unit]

  val loopToSelfLink = PhysicalLink(
    ResumeLoopHandler.loopSelfOp,
    PortIdentity(),
    ResumeLoopHandler.loopSelfOp,
    PortIdentity()
  )
  val loopSelfOp = PhysicalOpIdentity(OperatorIdentity("loopSelf"), "loopSelf")
  val loopSelf = ActorVirtualIdentity("loopSelf")
  val loopToSelfChannelId = ChannelIdentity(loopSelf, loopSelf, isControl = false)
}

trait ResumeLoopHandler {
  this: DataProcessorRPCHandlerInitializer =>
  registerHandler { (_: ResumeLoop, _) =>
    {
      val ls = dp.operator.asInstanceOf[LoopStartOpExec]
      //if (ls.iteration < ls.termination) {
       // dp.processDataPayload(
        //  loopToSelfChannelId,
        //  DataFrame(ls.buffer.toArray ++ Array(EndOfIteration(dp.actorId)))
      //  )
      //} else {
       // dp.processDataPayload(loopToSelfChannelId, EndOfUpstream())
     // }
    }
  }
}
