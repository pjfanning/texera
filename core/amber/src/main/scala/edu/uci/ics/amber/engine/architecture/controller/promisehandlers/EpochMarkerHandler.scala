package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, MarkerType}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, PhysicalOpIdentity}
import edu.uci.ics.texera.workflow.common.workflow.PhysicalPlan

object EpochMarkerHandler {

  final case class PropagateEpochMarker(
      sourceOpToStartProp: Set[PhysicalOpIdentity],
      id: String,
      markerType: MarkerType,
      scope: PhysicalPlan,
      markerCommand: ControlCommand[_]
  ) extends ControlCommand[Seq[(ActorVirtualIdentity, Any)]]

}

trait EpochMarkerHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: PropagateEpochMarker, sender) =>
    {
      val futures = msg.sourceOpToStartProp.flatMap { source =>
        cp.executionState.getOperatorExecution(source).getBuiltWorkerIds.map { worker =>
          sendAsMarker(
            msg.id,
            msg.markerType,
            msg.scope,
            msg.markerCommand,
            ChannelID(actorId, worker, isControl = true)
          ).map { ret =>
            (worker, ret)
          }
        }
      }
      Future.collect(futures.toList).map { ret =>
        cp.logManager.markAsReplayDestination(msg.id)
        ret
      }
    }
  }
}
