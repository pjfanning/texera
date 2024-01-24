package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.InputGateway
import edu.uci.ics.amber.engine.architecture.worker.ChannelMarkerManager.MarkerContext
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointState}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelMarkerPayload,
  NoAlignment,
  RequireAlignment
}
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  ChannelMarkerIdentity
}

import scala.collection.mutable

object ChannelMarkerManager {
  final case class MarkerContext(marker: ChannelMarkerPayload, fromChannel: ChannelID)
}

class ChannelMarkerManager(inputGateway: InputGateway, val actorId: ActorVirtualIdentity, inputGateway: InputGateway)
    extends AmberLogging {

  private val markerReceived =
    new mutable.HashMap[ChannelMarkerIdentity, Set[ChannelIdentity]]().withDefaultValue(Set())

  private var markerContext: MarkerContext = _

  val checkpoints = new mutable.HashMap[ChannelMarkerIdentity, CheckpointState]()

  def setContext(marker: ChannelMarkerPayload, from: ChannelID): Unit = {
    markerContext = MarkerContext(marker, from)
  }

  def getContext: MarkerContext = markerContext

  /**
    * Determines if an epoch marker is fully received from all relevant senders within its scope.
    * This method checks if the epoch marker, based on its type, has been received from all necessary channels.
    * For markers requiring alignment, it verifies receipt from all senders in the scope. For non-aligned markers,
    * it checks if it's the first received marker. Post verification, it cleans up the markers.
    *
    * @return true if the epoch marker is fully received from all senders in the scope, false otherwise.
    */
  def isMarkerAligned: Boolean = {
    assert(markerContext != null)
    val markerId = markerContext.marker.id
    markerReceived.update(markerId, markerReceived(markerId) + markerContext.fromChannel)
    // check if the epoch marker is completed
    val markerReceivedFromAllChannels =
      getChannelsWithinScope.subsetOf(markerReceived(markerId))
    val epochMarkerCompleted = markerContext.marker.markerType match {
      case RequireAlignment => markerReceivedFromAllChannels
      case NoAlignment      => markerReceived(markerId).size == 1 // only the first marker triggers
    }
    if (markerReceivedFromAllChannels) {
      markerReceived.remove(markerId) // clean up if all markers are received
    }
    epochMarkerCompleted
  }

  private def getChannelsWithinScope: Set[ChannelIdentity] = {
    assert(markerContext != null)
    val upstreams = markerContext.marker.scope.filter(_.toWorkerId == actorId)
    inputGateway.getAllChannels
      .map(_.channelId)
      .filter { id =>
        upstreams.contains(id)
      }
      .toSet
  }

}
