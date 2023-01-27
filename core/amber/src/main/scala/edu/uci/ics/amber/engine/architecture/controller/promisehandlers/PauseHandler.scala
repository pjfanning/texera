package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{ReportCurrentProcessingTuple, WorkflowPaused, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

import scala.collection.mutable

object PauseHandler {

  final case class PauseWorkflow() extends ControlCommand[Unit]
}

/** pause the entire workflow
  *
  * possible sender: client, controller
  */
trait PauseHandler {
  this: ControllerAsyncRPCHandlerInitializer =>

  registerHandler { (msg: PauseWorkflow, sender) =>
    {
      disableStatusUpdate() // to be enabled in resume
      disableMonitoring()
      disableSkewHandling()
      Future
        .collect(workflow.getAllOperators.map { operator =>
          // create a buffer for the current input tuple
          // since we need to show them on the frontend
          val buffer = mutable.ArrayBuffer[(ITuple, ActorVirtualIdentity)]()
          Future
            .collect(
              operator.getAllWorkers
                // send pause to all workers
                // pause message has no effect on completed or paused workers
                .map { worker =>
                  // send a pause message
                  send(PauseWorker(), worker).map { ret =>
                    operator.getWorker(worker).state = ret
                    send(QueryStatistics(), worker)
                      .join(send(QueryCurrentInputTuple(), worker))
                      // get the stats and current input tuple from the worker
                      .map {
                        case (stats, tuple) =>
                          operator.getWorker(worker).stats = stats
                          buffer.append((tuple, worker))
                      }
                  }
                }.toSeq
            )
            .map { ret =>
              // for each paused operator, send the input tuple
              sendToClient(ReportCurrentProcessingTuple(operator.id.operator, buffer.toArray))
            }
        }.toSeq)
        .map { ret =>
          Future
            .collect(workflow.getAllWorkers.map{
              worker => send(GetReplayAlignment(), worker).map{
                alignment => (worker, alignment)
              }
            }.toSeq).map{
            alignments =>
              // update frontend workflow status
              sendToClient(WorkflowStatusUpdate(workflow.getWorkflowStatus))
              // send paused to frontend
              sendToClient(WorkflowPaused())
              numPauses += 1
              val time = ((System.currentTimeMillis() - workflowStartTimeStamp) / 1000).toInt
              interactionHistory.append(
                (time, alignments.toMap +(CONTROLLER -> numPauses))
              )
          }
        }.unit
    }
  }

}
