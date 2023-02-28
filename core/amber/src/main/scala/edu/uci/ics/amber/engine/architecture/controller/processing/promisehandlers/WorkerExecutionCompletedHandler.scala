package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import WorkerExecutionCompletedHandler.WorkerExecutionCompleted
import edu.uci.ics.amber.engine.architecture.common.Interaction
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowCompleted,
  WorkflowReplayInfo
}
import edu.uci.ics.amber.engine.architecture.controller.processing.{
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
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
      interactionHistory.addCompletion(sender, msg.currentStep)
      val statsRequests = new mutable.MutableList[Future[Unit]]()
      statsRequests += execute(ControllerInitiateQueryStatistics(Option(List(sender))), CONTROLLER)

      Future
        .collect(statsRequests)
        .flatMap(_ => {
          // if entire workflow is completed, clean up
          if (cp.execution.isCompleted) {
            // after query result come back: send completed event, cleanup ,and kill workflow
            val interaction = new Interaction()
            interaction.addParticipant(CONTROLLER, -1L, 100000, 0)
            cp.execution.getAllWorkers.foreach { worker =>
              interaction.addParticipant(worker, -1L, 100000, 0)
            }
            interactionHistory
              .addInteraction((System.currentTimeMillis() - workflowStartTimeStamp), interaction)
            sendToClient(WorkflowReplayInfo(interactionHistory))
            sendToClient(WorkflowCompleted())
            disableStatusUpdate()
            disableMonitoring()
            disableSkewHandling()
            println("workflow completed!!!!!!!!!!!!!!")
            Future.Unit
          } else {
            cp.scheduler.onWorkerCompletion(sender, cp.availableNodes).flatMap(_ => Future.Unit)
          }
        })
    }
  }
}
