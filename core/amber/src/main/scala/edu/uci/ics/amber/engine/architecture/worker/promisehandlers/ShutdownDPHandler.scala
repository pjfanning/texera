package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ShutdownDPHandler.ShutdownDP
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance, SkipReply}

import java.util.concurrent.CompletableFuture


object ShutdownDPHandler{
  final case class ShutdownDP(reason:Option[Throwable], completion:CompletableFuture[Unit]) extends ControlCommand[Unit] with SkipFaultTolerance with SkipReply
}

trait ShutdownDPHandler {
  this: DataProcessor =>
  registerHandler{
    (msg: ShutdownDP, sender) =>
      logManager.terminate()
      msg.completion.complete(())
      dpThread.cancel(true) // interrupt
      dpThreadExecutor.shutdownNow() // destroy thread
      if (msg.reason.isEmpty) {
        throw new InterruptedException() // actively interrupt itself
      } else {
        throw msg.reason.get
      }
  }
}
