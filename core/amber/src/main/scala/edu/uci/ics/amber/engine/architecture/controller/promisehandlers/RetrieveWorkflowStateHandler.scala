package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.rpc.ChannelMarkerType.NO_ALIGNMENT
import edu.uci.ics.amber.engine.architecture.rpc.{
  AsyncRPCContext,
  EmptyRequest,
  PropagateChannelMarkerRequest
}
import edu.uci.ics.amber.engine.architecture.rpc.{RetrieveWorkflowStateResponse, StringResponse}
import edu.uci.ics.amber.engine.architecture.rpc.WorkerServiceGrpc.METHOD_RETRIEVE_STATE
import edu.uci.ics.amber.engine.common.util.SELF
import edu.uci.ics.amber.engine.common.ChannelMarkerIdentity

import java.time.Instant
trait RetrieveWorkflowStateHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  override def retrieveWorkflowState(
      request: EmptyRequest,
      ctx: AsyncRPCContext
  ): Future[RetrieveWorkflowStateResponse] = {
    val targetOps = cp.workflowScheduler.physicalPlan.operators.map(_.id).toSeq
    val markerMessage = PropagateChannelMarkerRequest(
      cp.workflowExecution.getRunningRegionExecutions
        .flatMap(_.getAllOperatorExecutions.map(_._1))
        .toSeq,
      ChannelMarkerIdentity("RetrieveWorkflowState_" + Instant.now().toString),
      NO_ALIGNMENT,
      targetOps,
      targetOps,
      EmptyRequest(),
      METHOD_RETRIEVE_STATE.getBareMethodName
    )
    controllerInterface
      .propagateChannelMarker(
        markerMessage,
        mkContext(SELF)
      )
      .map { ret =>
        RetrieveWorkflowStateResponse(ret.returns.map {
          case (actorId, value) =>
            val finalret = value match {
              case s: StringResponse => s.value
              case other =>
                ""
            }
            (actorId, finalret)
        })
      }
  }

}
