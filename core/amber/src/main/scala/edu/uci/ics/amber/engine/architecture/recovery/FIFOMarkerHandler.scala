package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.ControlInvocation
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipFaultTolerance}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


abstract class FIFOMarkerHandler(val actorId:ActorVirtualIdentity) extends AmberLogging {

  private val pendingMarkers = mutable.HashMap[Long, FIFOMarker]()
  private val seenMarkerIds = mutable.HashSet[Long]()

  def handleCommand(channel:ChannelEndpointID, controlCommand: ControlInvocation):Unit

  def inputMarker(channel: ChannelEndpointID, marker:FIFOMarker):Unit = {
    if(!seenMarkerIds.contains(marker.id)){
      seenMarkerIds.add(marker.id)
      val cmd = marker.sharedCommand.commandOnFirstMarker()
      if(cmd.isDefined){
        cmd.get.marker = marker
        handleCommand(channel, ControlInvocation(cmd.get))
        cmd.get.syncFuture.get()
        marker.sharedCommand.state = cmd.get.state
      }else{
        marker.sharedCommand.state = new FIFOMarkerCollectionState(marker.markerCounts(actorId))
      }
      marker.sharedCommand.marker = marker
      pendingMarkers(marker.id) = marker
    }
    if(pendingMarkers.contains(marker.id)){
      val pendingMarker = pendingMarkers(marker.id)
      val collectionState = pendingMarker.sharedCommand.state
      collectionState.acceptMarker(channel)
      if(collectionState.isCompleted){
        handleCommand(channel, ControlInvocation(pendingMarker.controlIdMap(actorId), pendingMarker.sharedCommand))
        pendingMarkers.remove(pendingMarker.id)
      }
    }
  }

  def inputPayload(channel:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit ={
    pendingMarkers.foreach{
      case (id, marker) =>
        if(!payload.isInstanceOf[SkipFaultTolerance]){
          marker.sharedCommand.state.acceptInputPayload(channel, payload)
        }
    }
  }
}
