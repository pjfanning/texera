package edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.processing.DataProcessorRPCHandlerInitializer
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.WorkerEpochMarkerHandler.WorkerPropagateEpochMarker
import edu.uci.ics.amber.engine.common.ambermessage.EpochMarker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object WorkerEpochMarkerHandler {

  final case class WorkerPropagateEpochMarker(epochMarker: EpochMarker) extends ControlCommand[Unit]

}

trait WorkerEpochMarkerHandler {
  this: DataProcessorRPCHandlerInitializer =>

  registerHandler { (msg: WorkerPropagateEpochMarker, sender) =>
    {
      dp.epochManager.triggerEpochMarkerOnCompletion(msg.epochMarker)
    }
  }

}
