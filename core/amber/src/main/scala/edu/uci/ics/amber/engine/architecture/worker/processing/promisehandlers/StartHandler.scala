package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.EndMarker
import StartHandler.StartWorker
import edu.uci.ics.amber.engine.architecture.worker.processing.{DataProcessor, DataProcessorRPCHandlerInitializer}
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.{PAUSED, READY, RUNNING}
import edu.uci.ics.amber.engine.common.ISourceOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{SOURCE_STARTER_ACTOR, SOURCE_STARTER_OP}

object StartHandler {
  final case class StartWorker() extends ControlCommand[WorkerState]
}

trait StartHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: StartWorker, sender) =>
    if (dp.operator.isInstanceOf[ISourceOperatorExecutor]) {
      dp.stateManager.assertState(READY, PAUSED)
      dp.stateManager.transitTo(RUNNING)
      // add a virtual input channel just for kicking off the execution
      dp.registerInput(SOURCE_STARTER_ACTOR, LinkIdentity(SOURCE_STARTER_OP, dp.getOperatorId))
      dp.internalQueue.enqueueData(EndMarker(SOURCE_STARTER_ACTOR))
      dp.stateManager.getCurrentState
    } else {
      throw new WorkflowRuntimeException(
        s"non-source worker $actorId received unexpected StartWorker!"
      )
    }
  }
}
