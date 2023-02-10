package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.{
  Controller,
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowCompleted,
  WorkflowReplayInfo
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import scala.collection.mutable

object WorkerExecutionCompletedHandler {
  final case class WorkerExecutionCompleted() extends ControlCommand[Unit] with SkipReply
}

/** indicate a worker has completed its job
  * i.e. received and processed all data from upstreams
  * note that this doesn't mean all the output of this worker
  * has been received by the downstream workers.
  *
  * possible sender: worker
  */
trait WorkerExecutionCompletedHandler {
  this: ControllerProcessor =>

  registerHandler { (msg: WorkerExecutionCompleted, sender) =>
    {
      assert(sender.isInstanceOf[ActorVirtualIdentity])
      // after worker execution is completed, query statistics immediately one last time
      // because the worker might be killed before the next query statistics interval
      // and the user sees the last update before completion
      val statsRequests = new mutable.MutableList[Future[Unit]]()
      statsRequests += execute(ControllerInitiateQueryStatistics(Option(List(sender))), CONTROLLER)

      Future
        .collect(statsRequests)
        .flatMap(_ => {
          // if entire workflow is completed, clean up
          if (execution.isCompleted) {
            // after query result come back: send completed event, cleanup ,and kill workflow
            interactionHistory
              .append(
                (((System.currentTimeMillis() - workflowStartTimeStamp) / 1000).toInt, execution.getAllWorkers.map(x => (x, -1L)).toMap + (CONTROLLER -> -1L))
              )
            sendToClient(WorkflowReplayInfo(interactionHistory))
            sendToClient(WorkflowCompleted())
            disableStatusUpdate()
            disableMonitoring()
            disableSkewHandling()
            println("workflow completed!!!!!!!!!!!!!!")
            Future.Unit
          } else {
            scheduler.onWorkerCompletion(sender, availableNodes).flatMap(_ => Future.Unit)
          }
        })
    }
  }
}
