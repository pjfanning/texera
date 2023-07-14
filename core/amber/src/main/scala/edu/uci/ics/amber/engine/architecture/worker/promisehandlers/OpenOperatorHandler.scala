package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCService
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.{OpenOperatorRequest, OpenOperatorResponse}

import scala.concurrent.Future

trait OpenOperatorHandler {
  this: WorkerAsyncRPCService =>
  override def openOperator(request: OpenOperatorRequest): Future[OpenOperatorResponse] = {
    operator.open()
    Future{
      OpenOperatorResponse()
    }
  }
}
