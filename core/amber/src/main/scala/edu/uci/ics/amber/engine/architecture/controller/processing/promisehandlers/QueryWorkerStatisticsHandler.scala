package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.WorkflowStatusUpdate
import QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

object QueryWorkerStatisticsHandler {

  final case class ControllerInitiateQueryStatistics(
      filterByWorkers: Option[List[ActorVirtualIdentity]] = None
  ) extends ControlCommand[Unit]
      with SkipReply

}

/** Get statistics from all the workers
  *
  * possible sender: controller(by statusUpdateAskHandle)
  */
trait QueryWorkerStatisticsHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler((msg: ControllerInitiateQueryStatistics, sender) => {
    // send to specified workers (or all workers by default)
    val workers = msg.filterByWorkers.getOrElse(cp.execution.getAllWorkers).toList
    println("send control to workers #worker = " + workers.length)
    // send QueryStatistics message
    val requests = workers.map(worker =>
      // must immediately update worker state and stats after reply
      send(QueryStatistics(), worker).map(res => {
        val workerInfo = cp.execution.getOperatorExecution(worker).getWorkerInfo(worker)
        workerInfo.state = res.workerState
        workerInfo.stats = res
      })
    )

    // wait for all workers to reply before notifying frontend
    Future
      .collect(requests)
      .map(_ => {
        println("collected worker stats")
//        if(!cp.isReplaying){
//          val time = (System.currentTimeMillis() - workflowStartTimeStamp)
//          val interaction = new LogicalExecutionSnapshot()
//          val markerId = cp.processingHistory.addSnapshot(time, interaction)
//          interaction.addParticipant(CONTROLLER, CheckpointStats(
//            markerId,
//            cp.inputPort.getFIFOState,
//            cp.outputPort.getFIFOState,
//            cp.determinantLogger.getStep + 1,0))
//          val marker = EstimationMarker(markerId)
//          cp.outputPort.broadcastMarker(marker)
//        }
        sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
      })
  })
}
