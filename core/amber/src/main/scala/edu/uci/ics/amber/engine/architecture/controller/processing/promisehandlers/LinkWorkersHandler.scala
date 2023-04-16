package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import LinkWorkersHandler.LinkWorkers
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.AddPartitioningHandler.AddPartitioning
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.UpdateInputLinkingHandler.UpdateInputLinking
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity

object LinkWorkersHandler {
  final case class LinkWorkers(link: LinkIdentity) extends ControlCommand[Unit]
}

/** add a data transfer partitioning to the sender workers and update input linking
  * for the receiver workers of a link strategy.
  *
  * possible sender: controller, client
  */
trait LinkWorkersHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: LinkWorkers, sender) =>
    {
      val opExecution = cp.execution.getOperatorExecution(msg.link.to)
      // get the list of (sender id, partitioning, set of receiver ids) from the link
      val futures = cp.workflow.getLink(msg.link).getPartitioning.flatMap {
        case (from, link, partitioning, tos) =>
          // send messages to sender worker and receiver workers
          tos.foreach{
            worker =>
              // add upstream
              opExecution.getWorkerInfo(worker).upstreamChannelCount += 1
          }
          Seq(send(AddPartitioning(link, partitioning), from)) ++ tos.map(
            send(UpdateInputLinking(from, msg.link), _)
          )
      }

      Future.collect(futures.toSeq).map { _ =>
        // returns when all has completed

      }
    }
  }

}
