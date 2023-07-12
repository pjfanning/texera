package edu.uci.ics.amber.engine.architecture.recovery

import edu.uci.ics.amber.engine.architecture.checkpoint.SavedCheckpoint
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelEndpointID,
  MarkerCollectionSupport,
  WorkflowFIFOMessagePayload
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PendingCheckpoint(
    val checkpointId: String,
    val logicalSnapshotId: String,
    val actorId: ActorVirtualIdentity,
    var startTime: Long,
    val stepCursorAtCheckpoint: Long,
    var fifoInputState: Map[ChannelEndpointID, Long],
    var fifoOutputState: Map[ChannelEndpointID, Long],
    var initialCheckpointTime: Long,
    val chkpt: SavedCheckpoint,
    toAlign: Set[ChannelEndpointID]
) extends MarkerCollectionSupport
    with AmberLogging {

  private var onComplete: (PendingCheckpoint) => Unit = (_) => {}
  def setOnComplete(onComplete: (PendingCheckpoint) => Unit): Unit = {
    this.onComplete = onComplete
  }

  private val aligned = new mutable.HashSet[ChannelEndpointID]()
  def isNoLongerPending: Boolean = toAlign.subsetOf(aligned)

  @volatile var checkpointDone = false

  def onReceiveMarker(channel: ChannelEndpointID): Unit = {
    aligned.add(channel)
    checkCompletion()
    logger.info(
      s"finish recording input channel $channel current = ${aligned}, target = $toAlign, recorded input for this channel = ${chkpt.getInputData
        .getOrElse(channel, mutable.ArrayBuffer.empty)
        .size}"
    )
  }

  def checkCompletion(): Unit = {
    if (checkpointDone && toAlign.subsetOf(aligned)) {
      onComplete(this)
    }
  }

  def onReceivePayload(channel: ChannelEndpointID, p: WorkflowFIFOMessagePayload): Unit = {
    if (checkpointDone) {
      if (!aligned.contains(channel) && toAlign.contains(channel)) {
        chkpt.addInputData(channel, p)
      }
    }
  }
}
