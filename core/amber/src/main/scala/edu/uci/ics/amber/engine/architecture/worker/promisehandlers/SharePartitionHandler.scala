package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.SharePartitionHandler.SharePartition
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object SharePartitionHandler {
  final case class SharePartition(
      skewedReceiverId: ActorVirtualIdentity,
      helperReceiverId: ActorVirtualIdentity,
      tuplesToRedirectNumerator: Long,
      tuplesToRedirectDenominator: Long
  ) extends ControlCommand[Boolean]
}

trait SharePartitionHandler {
  this: DataProcessor =>

  registerHandler { (cmd: SharePartition, sender) =>
    batchProducer.sharePartition(
      cmd.skewedReceiverId,
      cmd.helperReceiverId,
      cmd.tuplesToRedirectNumerator,
      cmd.tuplesToRedirectDenominator
    )
  }
}
