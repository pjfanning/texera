package edu.uci.ics.amber.engine.architecture.worker.promisehandlers

import edu.uci.ics.amber.engine.architecture.worker.WorkerAsyncRPCService
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.WorkerEpochMarkerHandler.WorkerPropagateEpochMarker
import edu.uci.ics.amber.engine.common.ambermessage.EpochMarker
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand

object WorkerEpochMarkerHandler {

  final case class WorkerPropagateEpochMarker(epochMarker: EpochMarker) extends ControlCommand[Unit]

}

trait WorkerEpochMarkerHandler {
  this: WorkerAsyncRPCService =>

  registerHandler { (msg: WorkerPropagateEpochMarker, sender) =>
    {
      epochManager.triggerEpochMarkerOnCompletion(msg.epochMarker)
    }
  }

}
