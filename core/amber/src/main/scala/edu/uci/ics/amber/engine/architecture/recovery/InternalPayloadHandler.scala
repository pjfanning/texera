package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


abstract class InternalPayloadHandler(val actorId:ActorVirtualIdentity) extends AmberLogging {

  private val pending = mutable.HashMap[Long, MarkerAlignmentInternalPayload]()
  private val seen = mutable.HashSet[Long]()

  def inputInternalPayload(payload:AmberInternalPayload):Unit

  def inputMarker(channel: ChannelEndpointID, payload:AmberInternalPayload):Unit = {
    payload match {
      case ip: IdempotentInternalPayload =>
        inputInternalPayload(payload)
      case op: OneTimeInternalPayload =>
        if(!seen.contains(op.id)){
          seen.add(op.id)
          inputInternalPayload(payload)
        }
      case mp: MarkerAlignmentInternalPayload =>
        if(pending.contains(mp.id)){
          pending(mp.id).onReceiveMarker(channel)
        }else{
          pending(mp.id) = mp
          mp.onReceiveMarker(channel)
          inputInternalPayload(mp)
        }
        if(mp.isAligned){
          inputInternalPayload(mp)
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
