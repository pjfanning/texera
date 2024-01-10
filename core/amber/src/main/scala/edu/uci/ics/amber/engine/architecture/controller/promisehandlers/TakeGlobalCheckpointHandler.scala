package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.TakeGlobalCheckpointHandler.TakeGlobalCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.ambermessage.NoAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import java.net.URI
import java.time.Instant


object TakeGlobalCheckpointHandler{
  final case class TakeGlobalCheckpoint() extends ControlCommand[Unit]
}
trait TakeGlobalCheckpointHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: TakeGlobalCheckpoint, sender) => {
    val checkpointId = "Checkpoint_" + Instant.now().toString
    execute(
      PropagateEpochMarker(
        cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
        checkpointId,
        NoAlignment,
        cp.workflow.physicalPlan,
        cp.workflow.physicalPlan.operators.map(_.id),
        TakeCheckpoint(checkpointId, new URI(""))
      ),
      sender
    ).unit
}
