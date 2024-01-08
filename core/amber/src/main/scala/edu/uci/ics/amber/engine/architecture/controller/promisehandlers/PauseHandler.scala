package edu.uci.ics.amber.engine.architecture.controller.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  WorkflowPaused,
  WorkflowStatusUpdate
}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.PauseHandler.PauseWorkflow
import edu.uci.ics.amber.engine.architecture.controller.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.EpochMarkerHandler.PropagateEpochMarker
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.QueryWorkerStatisticsHandler.ControllerInitiateQueryStatistics
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.PauseHandler.PauseWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.common.ambermessage.NoAlignment
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

import java.time.Instant

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
      cp.controllerTimerService.disableStatusUpdate() // to be enabled in resume
      cp.controllerTimerService.disableMonitoring()
      cp.controllerTimerService.disableSkewHandling()
      val interactionSupported = cp.workflow.physicalPlan.operators.forall(!_.isPythonOperator)
      (if (interactionSupported) {
         execute(
           PropagateEpochMarker(
             cp.executionState.getAllOperatorExecutions.map(_._1).toSet,
             "Pause_" + Instant.now().toString,
             NoAlignment,
             cp.workflow.physicalPlan,
             cp.workflow.physicalPlan.operators.map(_.id),
             PauseWorker()
           ),
           sender
         )
       } else {
         Future
           .collect(
             cp.executionState.getAllBuiltWorkers
               .map(workerId => send(PauseWorker(), workerId).map(ret => (workerId, ret)))
               .toSeq
           )
       }).map { ret =>
        ret.foreach {
          case (worker, value) =>
            val info = cp.executionState.getOperatorExecution(worker).getWorkerInfo(worker)
            info.state = value.asInstanceOf[WorkerState]
        }
        execute(ControllerInitiateQueryStatistics(), sender).map { ret =>
          // update frontend workflow status
          sendToClient(WorkflowStatusUpdate(cp.executionState.getWorkflowStatus))
          sendToClient(WorkflowPaused())
          logger.info(s"workflow paused")
        }
      }.unit
    }
  }
}
