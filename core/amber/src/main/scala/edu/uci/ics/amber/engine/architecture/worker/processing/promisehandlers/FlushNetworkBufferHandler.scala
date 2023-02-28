package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.messaginglayer.OutputManager.FlushNetworkBuffer
import edu.uci.ics.amber.engine.architecture.worker.processing.{
  DataProcessor,
  DataProcessorRPCHandlerInitializer
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer

trait FlushNetworkBufferHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (flush: FlushNetworkBuffer, sender) =>
    dp.outputManager.flushAll()
  }
}
