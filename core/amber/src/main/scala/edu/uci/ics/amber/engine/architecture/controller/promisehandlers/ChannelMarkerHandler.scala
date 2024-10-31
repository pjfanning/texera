package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.rpc.controlcommands.{
  AsyncRPCContext,
  ControlInvocation,
  PropagateChannelMarkerRequest
}
import edu.uci.ics.amber.engine.architecture.rpc.controlreturns.{
  ControlReturn,
  PropagateChannelMarkerResponse
}
import edu.uci.ics.amber.engine.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelIdentity}
trait ChannelMarkerHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  override def propagateChannelMarker(
      msg: PropagateChannelMarkerRequest,
      ctx: AsyncRPCContext
  ): Future[PropagateChannelMarkerResponse] = {
    // step1: create separate control commands for each target actor.
    val inputSet = msg.targetOps.flatMap { target =>
      cp.workflowExecution.getRunningRegionExecutions
        .map(_.getOperatorExecution(target))
        .flatMap(_.getWorkerIds.map { worker =>
          worker -> createInvocation(msg.markerMethodName, msg.markerCommand, worker)
        })
    }
    // step 2: packing all control commands into one compound command.
    val cmdMapping: Map[String, ControlInvocation] = inputSet.map {
      case (workerId, (control, _)) => (workerId.name, control)
    }.toMap
    val futures: Set[Future[(ActorVirtualIdentity, ControlReturn)]] = inputSet.map {
      case (workerId, (_, future)) => future.map(ret => (workerId, ret.asInstanceOf[ControlReturn]))
    }.toSet

    // step 3: convert scope DAG to channels.
    val channelScope = cp.workflowExecution.getRunningRegionExecutions
      .flatMap(regionExecution =>
        regionExecution.getAllLinkExecutions
          .map(_._2)
          .flatMap(linkExecution => linkExecution.getAllChannelExecutions.map(_._1))
      )
      .filter(channelId => {
        msg.scope
          .contains(VirtualIdentityUtils.getPhysicalOpId(channelId.fromWorkerId)) &&
          msg.scope
            .contains(VirtualIdentityUtils.getPhysicalOpId(channelId.toWorkerId))
      })
    val controlChannels = msg.sourceOpToStartProp.flatMap { source =>
      cp.workflowExecution.getLatestOperatorExecution(source).getWorkerIds.flatMap { worker =>
        Seq(
          ChannelIdentity(CONTROLLER, worker, isControl = true),
          ChannelIdentity(worker, CONTROLLER, isControl = true)
        )
      }
    }

    val finalScope = channelScope ++ controlChannels

    // step 4: start prop, send marker through control channel with the compound command from sources.
    msg.sourceOpToStartProp.foreach { source =>
      cp.workflowExecution.getLatestOperatorExecution(source).getWorkerIds.foreach { worker =>
        sendChannelMarker(
          msg.id,
          msg.markerType,
          finalScope.toSet,
          cmdMapping,
          ChannelIdentity(actorId, worker, isControl = true)
        )
      }
    }

    // step 5: wait for the marker propagation.
    Future.collect(futures.toList).map { ret =>
      cp.logManager.markAsReplayDestination(msg.id)
      PropagateChannelMarkerResponse(ret.map(x => (x._1.name, x._2)).toMap)
    }
  }
}
