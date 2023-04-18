package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


object InternalPayloadManager{

  case class EstimateCheckpointCost(id:Long) extends OneTimeInternalPayload
  case class NoOp() extends IdempotentInternalPayload
  case class ShutdownDP() extends IdempotentInternalPayload
  case class RestoreFromCheckpoint(fromCheckpoint:Option[Long], replayTo:Option[Long]) extends IdempotentInternalPayload
  case class CheckpointCompleted(id:Long, step:Long) extends IdempotentInternalPayload

  case class TakeCheckpoint(id:Long, alignmentMap:Map[ActorVirtualIdentity, Set[ChannelEndpointID]]) extends MarkerAlignmentInternalPayload

  final case class CheckpointStats(markerId: Long,
                                   inputWatermarks: Map[ChannelEndpointID, Long],
                                   outputWatermarks: Map[ChannelEndpointID, Long],
                                   alignment: Long,
                                   saveStateCost: Long)
}

class EmptyInternalPayloadManager extends InternalPayloadManager{
    override def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload): Unit = {}

    override def handlePayload(channel: ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload): Unit = {}

    override def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload): MarkerCollectionSupport = {null}

    override def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport): Unit = {}
}


abstract class InternalPayloadManager {

  private val pending = mutable.HashMap[Long, MarkerCollectionSupport]()
  private val seen = mutable.HashSet[Long]()

  def handlePayload(oneTimeInternalPayload: OneTimeInternalPayload):Unit

  def handlePayload(channel:ChannelEndpointID, idempotentInternalPayload: IdempotentInternalPayload):Unit

  def markerAlignmentStart(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload):MarkerCollectionSupport

  def markerAlignmentEnd(markerAlignmentInternalPayload: MarkerAlignmentInternalPayload, support: MarkerCollectionSupport):Unit

  def inputMarker(channel: ChannelEndpointID, payload:AmberInternalPayload):Unit = {
    payload match {
      case ip: IdempotentInternalPayload =>
        handlePayload(channel, ip)
      case op: OneTimeInternalPayload =>
        if(!seen.contains(op.id)){
          seen.add(op.id)
          handlePayload(op)
        }
      case mp: MarkerAlignmentInternalPayload =>
        if(!pending.contains(mp.id)){
          pending(mp.id) = markerAlignmentStart(mp)
        }
        pending(mp.id).onReceiveMarker(channel)
        if(pending(mp.id).isCompleted){
          markerAlignmentEnd(mp, pending(mp.id))
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
