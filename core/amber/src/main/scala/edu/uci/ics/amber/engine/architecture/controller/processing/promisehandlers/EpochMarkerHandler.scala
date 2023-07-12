package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

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
