package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.ClientEvent
import edu.uci.ics.amber.engine.common.ambermessage.{
  AmberInternalPayload,
  ChannelEndpointID,
  DataPayload,
  WorkflowClientMessage,
  WorkflowFIFOMessage,
  WorkflowFIFOMessagePayload,
  WorkflowMessage
}

import java.util.concurrent.atomic.AtomicLong
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, SELF}

import scala.collection.mutable

/**
  * NetworkOutput for generating sequence number when sending payloads
  * @param actorId ActorVirtualIdentity for the sender
  * @param handler actual sending logic
  */
class NetworkOutputPort(
    val actorId: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, WorkflowMessage) => Unit
) extends AmberLogging
    with Serializable {
  private val idToSequenceNums = new mutable.HashMap[ChannelEndpointID, AtomicLong]()

  def addOutputChannel(channel: ChannelEndpointID): Unit = {
    if (!idToSequenceNums.contains(channel)) {
      idToSequenceNums(channel) = new AtomicLong()
    }
  }

  def sendToClient(payload: ClientEvent): Unit = {
    handler(CLIENT, WorkflowClientMessage(payload))
  }

  def sendTo(to: ActorVirtualIdentity, payload: WorkflowFIFOMessagePayload): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = actorId
    }
    val useControlChannel = !payload.isInstanceOf[DataPayload]
    val outChannelEndpointID = ChannelEndpointID(receiverId, useControlChannel)
    sendThroughChannel(outChannelEndpointID, payload)
  }

  def getFIFOState: Map[ChannelEndpointID, Long] =
    idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def getActiveChannels: Iterable[ChannelEndpointID] = idToSequenceNums.keys

  def getSequenceNumber(channel: ChannelEndpointID): Long = {
    idToSequenceNums.getOrElseUpdate(channel, new AtomicLong()).getAndIncrement()
  }

  private def sendThroughChannel(
      to: ChannelEndpointID,
      payload: WorkflowFIFOMessagePayload
  ): Unit = {
    val inChannelEndpointID = ChannelEndpointID(actorId, to.isControlChannel)
    val seqNum = getSequenceNumber(to)
    handler(to.endpointWorker, WorkflowFIFOMessage(inChannelEndpointID, seqNum, payload))
  }

  def broadcastMarker(internalPayload: AmberInternalPayload): Unit = {
    idToSequenceNums.foreach {
      case (outChannel, seq) =>
        logger.info(s"send $internalPayload to ${outChannel}")
        sendThroughChannel(outChannel, internalPayload)
    }
  }

  def sendMarkerTo(
      internalPayload: AmberInternalPayload,
      receivers: Set[ChannelEndpointID]
  ): Unit = {
    receivers.foreach { outChannel =>
      logger.info(s"send $internalPayload to ${outChannel}")
      sendThroughChannel(outChannel, internalPayload)
    }
  }

}
