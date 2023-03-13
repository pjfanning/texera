package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{ReportCurrentProcessingTuple, WorkflowPaused, WorkflowStatusUpdate}
import PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.common.Interaction
import edu.uci.ics.amber.engine.architecture.controller.processing.{ControllerAsyncRPCHandlerInitializer, ControllerProcessor}
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryCurrentInputTupleHandler.QueryCurrentInputTuple
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.ambermessage.{SnapshotMarker, TakeGlobalCheckpoint, WorkflowRecoveryMessage}
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
              sendToClient(ReportCurrentProcessingTuple(operator.id.operator, buffer.toArray))
            }
        }.toSeq)
        .map { ret =>
          // update frontend workflow status
          sendToClient(WorkflowStatusUpdate(cp.execution.getWorkflowStatus))
          // send paused to frontend
          sendToClient(WorkflowPaused())
          workflowPauseStartTime = System.currentTimeMillis()
          logger.info(s"controller pause cursor = ${cp.numControlSteps}")
          if(!cp.isReplaying){
            val time = (System.currentTimeMillis() - workflowStartTimeStamp)
            val interaction = new Interaction()
            val markerId = cp.interactionHistory.addInteraction(time, interaction)
            interaction.addParticipant(CONTROLLER, CheckpointStats(true, markerId, cp.controlInput.getFIFOState, cp.controlOutputPort.getFIFOState,0,cp.numControlSteps + 1,0,0,0))
            val marker = SnapshotMarker(markerId, true)
            cp.controlOutputPort.broadcastMarker(marker)
          }
        }
        .unit
    }
  }

}
