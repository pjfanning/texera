package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  ReportCurrentProcessingTuple,
  WorkflowPaused,
  WorkflowStatusUpdate
}
import PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.common.Interaction
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.controller.processing.{
  ControllerAsyncRPCHandlerInitializer,
  ControllerProcessor
}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.GetReplayAlignmentHandler
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.GetReplayAlignmentHandler.GetReplayAlignment
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryStatisticsHandler.QueryStatistics
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
        .collect(cp.workflow.getAllOperators.map { operator =>
          // create a buffer for the current input tuple
          // since we need to show them on the frontend
          val buffer = mutable.ArrayBuffer[(ITuple, ActorVirtualIdentity)]()
          val opExecution = cp.execution.getOperatorExecution(operator.id)
          Future
            .collect(
              opExecution.identifiers
                // send pause to all workers
                // pause message has no effect on completed or paused workers
                .map { worker =>
                  val info = opExecution.getWorkerInfo(worker)
                  // send a pause message
                  send(PauseWorker(), worker).map { ret =>
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
              sendToClient(ReportCurrentProcessingTuple(operator.id.operator, buffer.toArray))
            }
        }.toSeq)
        .map { ret =>
          Future
            .collect(cp.execution.getAllWorkers.map { worker =>
              send(GetReplayAlignment(), worker).map { alignment =>
                (worker, alignment)
              }
            }.toSeq)
            .map { alignments =>
              // update frontend workflow status
              sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
              // send paused to frontend
              sendToClient(WorkflowPaused())
              val time = (System.currentTimeMillis() - workflowStartTimeStamp)
              workflowPauseStartTime = System.currentTimeMillis()
              println(s"current paused numControl = ${cp.numControlSteps}")
              val interaction = new Interaction()
              alignments.foreach {
                case (identity, tuple) =>
                  interaction.addParticipant(identity, tuple._1, tuple._2, tuple._3)
              }
              interaction.addParticipant(CONTROLLER, cp.numControlSteps, 0, 0)
              interactionHistory.addInteraction(time, interaction)
            }
        }
        .unit
    }
  }

}
