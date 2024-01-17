package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  ChannelMarkerPayload,
  NoAlignment,
  RequireAlignment
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class EpochManager(val actorId: ActorVirtualIdentity) extends AmberLogging {

  private val epochMarkerReceived =
    new mutable.HashMap[String, Set[ChannelID]]().withDefaultValue(Set())

  // markers the arrival of an epoch marker,
  // returns a boolean indicating if the epoch marker is completely received from all senders within scope
  def isMarkerAligned(
      upstreamLinkStatus: UpstreamLinkStatus,
      from: ChannelID,
      marker: ChannelMarkerPayload
  ): Boolean = {
    val markerId = marker.id
    epochMarkerReceived.update(markerId, epochMarkerReceived(markerId) + from)
    // check if the epoch marker is completed
    // TODO: rework the following logic to support control channels between workers
    val sendersWithinScope = upstreamLinkStatus.allUncompletedSenders
      .filter(sender => marker.scope.links.contains(upstreamLinkStatus.getInputLink(sender)))
      .map(senderId => ChannelID(senderId, actorId, isControl = false))
    val markerReceivedFromAllChannels = sendersWithinScope.subsetOf(epochMarkerReceived(markerId))
    val epochMarkerCompleted = marker.markerType match {
      case RequireAlignment => markerReceivedFromAllChannels
      case NoAlignment      => epochMarkerReceived(markerId).size == 1 // only the first marker triggers
    }
    if (markerReceivedFromAllChannels) {
      epochMarkerReceived.remove(markerId) // clean up if all markers are received
    }
    epochMarkerCompleted
  }

}
