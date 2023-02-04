package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseSkewMitigationHandler.PauseSkewMitigation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

// join-skew research related.
object PauseSkewMitigationHandler {
  final case class PauseSkewMitigation(
      skewedReceiverId: ActorVirtualIdentity,
      helperReceiverId: ActorVirtualIdentity
  ) extends ControlCommand[Boolean]
}

trait PauseSkewMitigationHandler {
  this: DataProcessor =>

  registerHandler { (cmd: PauseSkewMitigation, sender) =>
    batchProducer.pauseSkewMitigation(
      cmd.skewedReceiverId,
      cmd.helperReceiverId
    )
  }
}
