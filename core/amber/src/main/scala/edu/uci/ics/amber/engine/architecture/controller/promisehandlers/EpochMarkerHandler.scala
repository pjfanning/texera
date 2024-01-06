package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, MarkerType}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalOpIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

object EpochMarkerHandler {

  final case class PropagateEpochMarker(
      sourceOpToStartProp: Set[PhysicalOpIdentity],
      id: String,
      markerType: MarkerType,
      scope: PhysicalPlan,
      targetOps: Set[PhysicalOpIdentity],
      markerCommand: ControlCommand[_]
  ) extends ControlCommand[Seq[(ActorVirtualIdentity, Any)]]

}

trait EpochMarkerHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: PropagateEpochMarker, sender) =>
    {
      // step1: create separate control commands for each target actor.
      val inputSet = msg.targetOps.flatMap { target =>
        cp.executionState.getOperatorExecution(target).getBuiltWorkerIds.map { worker =>
          worker -> createInvocation(msg.markerCommand)
        }
      }
      // step 2: packing all control commands into one compound command.
      val cmdMapping: Map[ActorVirtualIdentity, ControlInvocation] = inputSet.map {
        case (workerId, (control, _)) => (workerId, control)
      }.toMap
      val futures: Set[Future[(ActorVirtualIdentity, Any)]] = inputSet.map {
        case (workerId, (_, future)) => future.map(ret => (workerId, ret))
      }

      // step 3: start prop, send marker through control channel with the compound command from sources.
      msg.sourceOpToStartProp.foreach { source =>
        cp.executionState.getOperatorExecution(source).getBuiltWorkerIds.foreach { worker =>
          sendMarker(
            msg.id,
            msg.markerType,
            msg.scope,
            cmdMapping,
            ChannelID(actorId, worker, isControl = true)
          )
        }
      }

      // step 4: wait for the marker propagation.
      Future.collect(futures.toList).map { ret =>
        cp.logManager.markAsReplayDestination(msg.id)
        ret
      }
    }
  }
}
