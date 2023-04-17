package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


class InternalPayloadManager(actor:WorkflowActor, handleCommand: AmberInternalPayload => Unit) extends AmberLogging {

  override def actorId: ActorVirtualIdentity = actor.actorId

  private val pending = mutable.HashMap[Long, MarkerAlignmentInternalPayloadWithState]()
  private val seen = mutable.HashSet[Long]()

  def inputMarker(channel: ChannelEndpointID, payload:AmberInternalPayload):Unit = {
    payload match {
      case ip: IdempotentInternalPayload =>
        handleCommand(payload)
      case op: OneTimeInternalPayload =>
        if(!seen.contains(op.id)){
          seen.add(op.id)
          handleCommand(payload)
        }
      case mp: MarkerAlignmentInternalPayload =>
        if(pending.contains(mp.id)){
          pending(mp.id).onReceiveMarker(channel)
        }else{
          val payloadWithState = mp.toPayloadWithState(actor)
          pending(mp.id) = payloadWithState
          handleCommand(payloadWithState)
          payloadWithState.onReceiveMarker(channel)
          payloadWithState.syncFuture.get() // wait for (possibly) DP thread to prepare state
        }
        if(pending(mp.id).isCompleted){
          handleCommand(pending(mp.id))
          pending.remove(mp.id)
        }
    }
  }

  def inputPayload(channel:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit ={
    pending.foreach{
      case (id, marker) =>
        marker.onReceivePayload(channel, payload)
    }
  }
}
