package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.WorkerEpochMarkerHandler.WorkerPropagateEpochMarker
import edu.uci.ics.amber.engine.common.ambermessage.EpochMarker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.LayerIdentity

object EpochMarkerHandler {

  final case class PropagateEpochMarker(destOperator: LayerIdentity, epochMarker: EpochMarker)
      extends ControlCommand[Unit]

}

trait EpochMarkerHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: PropagateEpochMarker, sender) =>
    {
      val futures = cp.execution
        .getOperatorExecution(msg.destOperator)
        .identifiers
        .map(worker => send(WorkerPropagateEpochMarker(msg.epochMarker), worker))
        .toList
      Future.collect(futures).unit
    }
  }

}
