package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.messaginglayer.OutputManager.FlushNetworkBuffer
import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCService

trait FlushNetworkBufferHandler {
  this: WorkerAsyncRPCService =>

  registerHandler { (flush: FlushNetworkBuffer, sender) =>
    outputManager.flushAll()
  }
}
