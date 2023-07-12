package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.recovery.RecoveryInternalQueueImpl
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueueImpl
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.ReplayControlMessageHandler.ContinueReplayTo
import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}

object ReplayControlMessageHandler {
  final case class ContinueReplayTo(replayTo: Long) extends ControlCommand[Unit] with SkipReply
}

trait ReplayControlMessageHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: ContinueReplayTo, _) =>
    dp.internalQueue match {
      case impl: RecoveryInternalQueueImpl =>
        val replayCompletion = Promise[Unit]()
        logger.info("set replay to " + msg.replayTo)
//        impl.replayOrderEnforcer.setReplayTo(msg.replayTo, replayCompletion)
        //impl.executeThroughDP(NoOp()) //kick start replay process
        replayCompletion
      case impl: WorkerInternalQueueImpl =>
        logger.info("received continue replay request but dp is not in replay mode!")
        Future.Unit
    }
  }

}
