package edu.uci.ics.amber.engine.architecture.recovery

import akka.serialization.SerializationExtension
import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, IdempotentInternalPayload, MarkerAlignmentInternalPayload, MarkerAlignmentInternalPayloadWithState, OneTimeInternalPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object InternalPayloadHandler{

  case class ShutdownDP() extends IdempotentInternalPayload

  case class EstimateCheckpointCost(id:Long) extends OneTimeInternalPayload

  case class TakeCheckpoint(id:Long, alignmentMap:Map[ActorVirtualIdentity, Set[ChannelEndpointID]]) extends MarkerAlignmentInternalPayload {
    override def toPayloadWithState(actor:WorkflowActor): MarkerAlignmentInternalPayloadWithState = {
      val startTime = System.currentTimeMillis()
      val chkpt = new SavedCheckpoint()
      chkpt.attachSerialization(SerializationExtension(actor.context.system))
      chkpt.save("fifoState", actor.inputPort.getFIFOState)
      new PendingCheckpoint(actor.actorId, startTime, chkpt, this, alignmentMap(actor.actorId))
    }
  }

  case class LoadCheckpoint(id:Long) extends OneTimeInternalPayload
}


trait InternalPayloadHandler {

  def process(cmd:AmberInternalPayload):Unit

}
