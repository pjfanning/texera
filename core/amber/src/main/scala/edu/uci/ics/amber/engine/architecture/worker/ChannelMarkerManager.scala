package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.InputGateway
import edu.uci.ics.amber.engine.architecture.worker.EpochManager.MarkerContext
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointState}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  ChannelMarkerPayload,
  NoAlignment,
  RequireAlignment
}
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelMarkerIdentity}

import scala.collection.mutable

object EpochManager {
  final case class MarkerContext(marker: ChannelMarkerPayload, fromChannel: ChannelID)
}

class ChannelMarkerManager(inputGateway: InputGateway, val actorId: ActorVirtualIdentity)
    extends AmberLogging {

  private val epochMarkerReceived =
    new mutable.HashMap[ChannelMarkerIdentity, Set[ChannelID]]().withDefaultValue(Set())

  private var markerContext: MarkerContext = _

  val checkpoints = new mutable.HashMap[ChannelMarkerIdentity, CheckpointState]()

  def setContext(marker: ChannelMarkerPayload, from: ChannelID): Unit = {
    markerContext = MarkerContext(marker, from)
  }

  def getContext: MarkerContext = markerContext

  // markers the arrival of an epoch marker,
  // returns a boolean indicating if the epoch marker is completely received from all senders within scope
  def isMarkerAligned: Boolean = {
    assert(markerContext != null)
    val markerId = markerContext.marker.id
    epochMarkerReceived.update(markerId, epochMarkerReceived(markerId) + markerContext.fromChannel)
    // check if the epoch marker is completed
    val markerReceivedFromAllChannels =
      getChannelsWithinScope.subsetOf(epochMarkerReceived(markerId))
    val epochMarkerCompleted = markerContext.marker.markerType match {
      case RequireAlignment => markerReceivedFromAllChannels
      case NoAlignment      => epochMarkerReceived(markerId).size == 1 // only the first marker triggers
    }
    if (markerReceivedFromAllChannels) {
      epochMarkerReceived.remove(markerId) // clean up if all markers are received
    }
    epochMarkerCompleted
  }

  private def getChannelsWithinScope: Set[ChannelID] = {
    assert(markerContext != null)
    val upstreams = markerContext.marker.scope.filter(_.to == actorId)
    inputGateway.getAllChannels
      .map(_.channelId)
      .filter { id =>
        upstreams.contains(id)
      }
      .toSet
  }

}
