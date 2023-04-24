package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{ReportCurrentProcessingTuple, WorkflowStateUpdate, WorkflowStatusUpdate}
import PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}

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
        .collect(cp.execution.getAllOperatorExecutions.map {
          case (layerId, opExecution) =>
          // create a buffer for the current input tuple
          // since we need to show them on the frontend
          val buffer = mutable.ArrayBuffer[(ITuple, ActorVirtualIdentity)]()
          Future
            .collect(
              opExecution.identifiers
                // send pause to all workers
                // pause message has no effect on completed or paused workers
                .map { worker =>
                  val info = opExecution.getWorkerInfo(worker)
                  // send a pause message
                  send(PauseWorker(), worker).flatMap { ret =>
                    info.state = ret
                    send(QueryStatistics(), worker)
                      .join(send(QueryCurrentInputTuple(), worker))
                      // get the stats and current input tuple from the worker
                      .map {
                        case (stats, tuple) =>
                          info.stats = stats
                          buffer.append((tuple, worker))
                      }
                  }
                }.toSeq
            )
            .map { ret =>
              // for each paused operator, send the input tuple
              sendToClient(ReportCurrentProcessingTuple(layerId.operator, buffer.toArray))
            }
        }.toSeq)
        .map { ret =>
          // update frontend workflow status
          sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
          sendToClient(WorkflowStateUpdate(cp.execution.getState))
          workflowPauseStartTime = System.currentTimeMillis()
          logger.info(s"controller pause cursor = ${cp.cursor.getStep}")
        }
        .unit
    }
  }

}
