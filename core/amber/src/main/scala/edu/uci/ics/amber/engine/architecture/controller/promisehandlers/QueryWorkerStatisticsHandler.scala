package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerAsyncRPCHandlerInitializer, ControllerProcessor}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowStatusUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object QueryWorkerStatisticsHandler {

  final case class ControllerInitiateQueryStatistics(
      filterByWorkers: Option[List[ActorVirtualIdentity]] = None
  ) extends ControlCommand[Unit]

}

/** Get statistics from all the workers
  *
  * possible sender: controller(by statusUpdateAskHandle)
  */
trait QueryWorkerStatisticsHandler {
  this: ControllerProcessor =>

  registerHandler((msg: ControllerInitiateQueryStatistics, sender) => {
    // send to specified workers (or all workers by default)
    val workers = msg.filterByWorkers.getOrElse(execution.getAllWorkers).toList
    println("send control to workers #worker = "+workers.length)
    // send QueryStatistics message
    val requests = workers.map(worker =>
      // must immediately update worker state and stats after reply
      send(QueryStatistics(), worker).map(res => {
        val workerInfo = execution.getOperatorExecution(worker).getWorkerInfo(worker)
        workerInfo.state = res.workerState
        workerInfo.stats = res
      })
    )

    // wait for all workers to reply before notifying frontend
    Future
      .collect(requests)
      .map(_ => {
        println("collected worker stats")
        sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))
      })
  })
}
