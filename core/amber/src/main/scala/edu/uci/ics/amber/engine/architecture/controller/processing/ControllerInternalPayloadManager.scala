package edu.uci.ics.amber.engine.architecture.controller.processing

import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerCollectionSupport, OneTimeInternalPayload}

class ControllerInternalPayloadManager(controller:Controller) extends InternalPayloadManager{

  override def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload): Unit = {

  }

  override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit ={

  }

  override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {
    null
  }

  override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {

  }
}
