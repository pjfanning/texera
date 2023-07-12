package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.FlushNetworkBufferHandler.FlushNetworkBuffer
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

object FlushNetworkBufferHandler {
  final case class FlushNetworkBuffer() extends ControlCommand[Unit] with SkipReply
}

trait FlushNetworkBufferHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (flush: FlushNetworkBuffer, sender) =>
    dp.outputManager.flushAll()
  }
}
