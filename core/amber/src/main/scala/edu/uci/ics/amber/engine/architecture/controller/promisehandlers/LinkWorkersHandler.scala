package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.LinkWorkersHandler.LinkWorkers
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.AddPartitioningHandler.AddPartitioning
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.UpdateInputLinkingHandler.UpdateInputLinking
import edu.uci.ics.amber.engine.common.ambermessage.ChannelID
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.PhysicalLinkIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

object LinkWorkersHandler {
  final case class LinkWorkers(linkId: PhysicalLinkIdentity) extends ControlCommand[Unit]
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
      val partitionings = cp.workflow.physicalPlan.getLink(msg.linkId).partitionings
      val senderWorkerIds = cp.workflow.physicalPlan.getOperator(msg.linkId.from).getWorkerIds
      val futures = senderWorkerIds
        .zip(partitionings)
        .flatMap({
          case (senderWorkerId, (partitioning, receiverWorkerIds)) =>
            cp.executionState.builtChannels
              .add(ChannelID(CONTROLLER, senderWorkerId, isControl = true))
            Seq(
              send(AddPartitioning(msg.linkId, partitioning), senderWorkerId)
            ) ++ receiverWorkerIds.map { receiverId =>
              cp.executionState.builtChannels
                .add(ChannelID(CONTROLLER, receiverId, isControl = true))
              cp.executionState.builtChannels
                .add(ChannelID(senderWorkerId, receiverId, isControl = false))
              send(UpdateInputLinking(senderWorkerId, msg.linkId), receiverId)
            }
        })

      Future.collect(futures).map { _ =>
        // returns when all has completed

      }
    }
  }

}
