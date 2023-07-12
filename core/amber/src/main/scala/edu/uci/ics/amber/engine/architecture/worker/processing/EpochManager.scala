package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.common.ambermessage.{ControlInvocation, EpochMarker}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class EpochManager extends Serializable {

  @transient
  var dp: DataProcessor = _

  def initialize(dp: DataProcessor): Unit = {
    this.dp = dp
  }

  private val epochMarkerReceived =
    new mutable.HashMap[String, Set[ActorVirtualIdentity]]().withDefaultValue(Set())

  // markers the arrival of an epoch marker,
  // returns a boolean indicating if the epoch marker is completely received from all senders within scope
  def processEpochMarker(from: ActorVirtualIdentity, marker: EpochMarker): Unit = {
    val markerId = marker.id
    dp.pauseManager.pauseInputChannel(EpochMarkerPause(markerId), List(from))
    epochMarkerReceived.update(markerId, epochMarkerReceived(markerId) + from)

    // check if the epoch marker is completed
    val sendersWithinScope = dp.upstreamLinkStatus.allUncompletedSenders.filter(sender =>
      marker.scope.links.contains(dp.upstreamLinkStatus.getInputLink(sender))
    )
    val epochMarkerCompleted = epochMarkerReceived(markerId) == sendersWithinScope
    if (epochMarkerCompleted) {
      epochMarkerReceived.remove(markerId) // clean up on epoch marker completion
      triggerEpochMarkerOnCompletion(marker)
    }

  }

  def triggerEpochMarkerOnCompletion(marker: EpochMarker): Unit = {
    // invoke the control command carried with the epoch marker
    if (marker.command.nonEmpty) {
      dp.asyncRPCServer.receive(
        ControlInvocation(marker.command.get),
        dp.actorId
      )
    }
    // if this operator is not the final destination of the marker, pass it downstream
    if (!marker.scope.sinkOperators.contains(dp.getOperatorId)) {
      dp.outputManager.emitEpochMarker(marker)
    }
    // unblock input channels
    dp.pauseManager.resume(EpochMarkerPause(marker.id))
  }

}
