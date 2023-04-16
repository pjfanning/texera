package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.common.LogicalExecutionSnapshot
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkflowCompleted, WorkflowReplayInfo}
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import scala.collection.mutable

object WorkerExecutionCompletedHandler {
  final case class WorkerExecutionCompleted(currentStep: Long)
      extends ControlCommand[Unit]
      with SkipReply
}

/** indicate a worker has completed its job
  * i.e. received and processed all data from upstreams
  * note that this doesn't mean all the output of this worker
  * has been received by the downstream workers.
  *
  * possible sender: worker
  */
trait WorkerExecutionCompletedHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

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
          if (cp.execution.isCompleted) {
            // after query result come back: send completed event, cleanup ,and kill workflow
            sendToClient(WorkflowReplayInfo(cp.processingHistory))
            sendToClient(WorkflowCompleted())
            disableStatusUpdate()
            disableMonitoring()
            disableSkewHandling()
            println("workflow completed!!!!!!!!!!!!!!")
            Future.Unit
          } else {
            cp.scheduler.onWorkerCompletion(sender, cp.getAvailableNodes()).flatMap(_ => Future.Unit)
          }
        })
    }
  }
}
