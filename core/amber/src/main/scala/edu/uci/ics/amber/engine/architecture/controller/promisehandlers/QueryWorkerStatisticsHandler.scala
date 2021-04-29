package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowResultUpdate
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.{ControllerInitiateQueryResults, ControllerInitiateQueryStatistics, QueryWorkerResult, QueryWorkerStatistics}
import edu.uci.ics.amber.engine.architecture.principal.OperatorResult
import edu.uci.ics.amber.engine.architecture.worker.{WorkerResult, WorkerStatistics}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import scala.collection.mutable

object QueryWorkerStatisticsHandler {
  final case class ControllerInitiateQueryStatistics() extends ControlCommand[Unit]
  final case class QueryWorkerStatistics() extends ControlCommand[WorkerStatistics]

  final case class ControllerInitiateQueryResults() extends ControlCommand[Unit]
  final case class QueryWorkerResult() extends ControlCommand[Option[WorkerResult]]
}

/** Get statistics from all the workers
  *
  * possible sender: controller(by statusUpdateAskHandle)
  */
trait QueryWorkerStatisticsHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler((msg: ControllerInitiateQueryStatistics, sender) => {

    // send all worker QueryStatistics message
    val requests = workflow.getAllWorkers.toList.map(worker => {
      send(QueryWorkerStatistics(), worker)
    })

    // wait for all reply and collect all response, then update statistics
    Future
      .collect(requests)
      .map(res => {
        res.foreach(r => workflow.getWorkerInfo(r.workerId).stats = r)
        updateFrontendWorkflowStatus()
      })
  })

  registerHandler((msg: ControllerInitiateQueryResults, sender) => {
    val sinkWorkers = workflow.getSinkLayers.flatMap(l => l.workers.keys)

    // send all sink worker QueryResult message
    val requests = sinkWorkers.toList.map(worker => {
      send(QueryWorkerResult(), worker)
    })

    // wait for all reply and collect all response, then update operator results
    Future
      .collect(requests)
      .map(res => {
        val operatorResultUpdate = new mutable.HashMap[String, OperatorResult]()
        res.flatten.groupBy(r => workflow.getOperator(r.workerId).id).foreach(e => {
          val opId = e._1.operator
          val resultsPerWorker = e._2
          val outputMode = resultsPerWorker.head.outputMode
          val results = resultsPerWorker.flatMap(r => r.result).toList
          operatorResultUpdate(opId) = OperatorResult(outputMode, results)
        })
        updateFrontendWorkflowResult(WorkflowResultUpdate(operatorResultUpdate.toMap))
      })
  })
}
