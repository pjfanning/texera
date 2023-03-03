package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.common.Interaction
import edu.uci.ics.amber.engine.architecture.controller.processing.ControllerAsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.CollectAlignmentInformationHandler.CollectAlignmentInformation
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.GetReplayAlignmentHandler.{GetReplayAlignment, ReplayAlignmentInfo}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER

object CollectAlignmentInformationHandler {
  final case class CollectAlignmentInformation() extends ControlCommand[Unit]
}



trait CollectAlignmentInformationHandler {
  this: ControllerAsyncRPCHandlerInitializer =>
  registerHandler { (msg: CollectAlignmentInformation, sender) =>
    Future
      .collect(cp.execution.getAllWorkers.map { worker =>
        send(GetReplayAlignment(), worker).map { alignment =>
          (worker, alignment)
        }
      }.toSeq).map{
      alignments =>
      val time = (System.currentTimeMillis() - workflowStartTimeStamp)
      logger.info(s"current alignment numControl = ${cp.numControlSteps}")
      val interaction = new Interaction()
      alignments.foreach {
        case (identity, info) =>
          interaction.addParticipant(identity, info)
      }
      interaction.addParticipant(CONTROLLER, ReplayAlignmentInfo(cp.controlInput.getFIFOState, cp.numControlSteps, 0,0,0,cp.controlOutputPort.getFIFOState))
      interactionHistory.addInteraction(time, interaction)
    }
  }
}
