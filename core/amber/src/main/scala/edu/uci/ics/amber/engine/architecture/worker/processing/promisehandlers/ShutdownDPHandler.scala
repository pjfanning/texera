package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer

object ShutdownDPHandler {
//  final case class ShutdownDP(reason: Option[Throwable], completion: CompletableFuture[Unit])
//      extends ControlCommand[Unit]
//      with SkipReply
}

trait ShutdownDPHandler {
  this: DataProcessorRPCHandlerInitializer =>
//  registerHandler { (msg: ShutdownDP, sender) =>
//    dp.logManager.terminate()
//    msg.completion.complete(())
//    dp.dpThread.stop()
//    if (msg.reason.isEmpty) {
//      throw new InterruptedException() // actively interrupt itself
//    } else {
//      throw msg.reason.get
//    }
//  }
}
