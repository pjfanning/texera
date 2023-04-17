package edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers

import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{FIFOFirstMarkerCommand, FIFOMarkerControlCommand, SkipFaultTolerance, SkipReply}

object CheckpointControlMessageHandler {
  final case class TakeCheckpoint() extends FIFOMarkerControlCommand[Unit]{
    override def commandOnFirstMarker(): Option[FIFOFirstMarkerCommand] = Some(CreateCheckpoint())
  }

  final case class CreateCheckpoint() extends FIFOFirstMarkerCommand

}
