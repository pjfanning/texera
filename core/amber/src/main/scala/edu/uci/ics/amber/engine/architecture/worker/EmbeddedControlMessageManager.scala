package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.messaginglayer.InputGateway
import edu.uci.ics.amber.engine.common.{AmberLogging, CheckpointState}
import edu.uci.ics.amber.engine.common.ambermessage.{
  EmbeddedControlMessagePayload,
  NoAlignment,
  RequireAlignment
}
import edu.uci.ics.amber.engine.common.virtualidentity.{
  ActorVirtualIdentity,
  ChannelIdentity,
  EmbeddedControlMessageIdentity
}

import scala.collection.mutable

object EmbeddedControlMessageManager {
  final case class ECMContext(ecm: EmbeddedControlMessagePayload, fromChannel: ChannelIdentity)
}

class EmbeddedControlMessageManager(val actorId: ActorVirtualIdentity, inputGateway: InputGateway)
    extends AmberLogging {

  private val ecmReceived =
    new mutable.HashMap[EmbeddedControlMessageIdentity, Set[ChannelIdentity]]()

  val checkpoints = new mutable.HashMap[EmbeddedControlMessageIdentity, CheckpointState]()

  /**
    * Determines if an epoch marker is fully received from all relevant senders within its scope.
    * This method checks if the epoch marker, based on its type, has been received from all necessary channels.
    * For markers requiring alignment, it verifies receipt from all senders in the scope. For non-aligned markers,
    * it checks if it's the first received marker. Post verification, it cleans up the markers.
    *
    * @return Boolean indicating if the epoch marker is completely received from all senders
    *          within the scope. Returns true if the marker is aligned, otherwise false.
    */
  def isECMAligned(
                    from: ChannelIdentity,
                    ecm: EmbeddedControlMessagePayload
  ): Boolean = {
    val ecmId = ecm.id
    if (!ecmReceived.contains(ecmId)) {
      ecmReceived(ecmId) = Set()
    }
    ecmReceived.update(ecmId, ecmReceived(ecmId) + from)
    // check if the epoch marker is completed
    val ecmReceivedFromAllChannels =
      getChannelsWithinScope(ecm).subsetOf(ecmReceived(ecmId))
    val ecmCompleted = ecm.markerType match {
      case RequireAlignment => ecmReceivedFromAllChannels
      case NoAlignment      => ecmReceived(ecmId).size == 1 // only the first marker triggers
    }
    if (ecmReceivedFromAllChannels) {
      ecmReceived.remove(ecmId) // clean up if all markers are received
    }
    ecmCompleted
  }

  private def getChannelsWithinScope(ecm: EmbeddedControlMessagePayload): Set[ChannelIdentity] = {
    val upstreams = ecm.scope.filter(_.toWorkerId == actorId)
    inputGateway.getAllChannels
      .map(_.channelId)
      .filter { id =>
        upstreams.contains(id)
      }
      .toSet
  }

}
