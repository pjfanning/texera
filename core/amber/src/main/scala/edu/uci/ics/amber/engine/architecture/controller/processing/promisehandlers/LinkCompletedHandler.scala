package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import LinkCompletedHandler.LinkCompleted
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.{LinkIdentity, OperatorIdentity}

object LinkCompletedHandler {
  final case class LinkCompleted(linkID: LinkIdentity) extends ControlCommand[Unit] with SkipReply
}

/** Notify the completion of a particular link
  * (the receiver side has received all data from one link,
  * note that this does not mean the receiver has completed
  * since there can be multiple input links)
  *
  * possible sender: worker
  */
trait LinkCompletedHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: LinkCompleted, sender) =>
    {
      if (msg.linkID == null) {
        Future(())
      } else {
        // get the target link from workflow
        val link = cp.execution.getLinkExecution(msg.linkID)
        link.incrementCompletedReceiversCount()
        if (link.isCompleted) {
          cp.scheduler
            .onLinkCompletion(msg.linkID, cp.getAvailableNodes())
            .flatMap(_ => Future.Unit)
        } else {
          // if the link is not completed yet, do nothing
          Future(())
        }
      }
    }
  }

}
