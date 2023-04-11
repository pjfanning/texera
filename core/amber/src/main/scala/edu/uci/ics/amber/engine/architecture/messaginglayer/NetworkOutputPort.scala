package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DataPayload, FIFOMarker, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}

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
    val handler: (ActorVirtualIdentity, WorkflowFIFOMessage) => Unit
) extends AmberLogging with Serializable {
  private val idToSequenceNums = new mutable.HashMap[ChannelEndpointID, AtomicLong]()

  def sendTo(to: ActorVirtualIdentity, payload: WorkflowFIFOMessagePayload): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = actorId
    }
    val useControlChannel = !payload.isInstanceOf[DataPayload]
    val outChannelEndpointID = ChannelEndpointID(receiverId, useControlChannel)
    val seqNum = idToSequenceNums.getOrElseUpdate(outChannelEndpointID, new AtomicLong()).getAndIncrement()
    val inChannelEndpointID = ChannelEndpointID(actorId, useControlChannel)
    handler(receiverId, WorkflowFIFOMessage(inChannelEndpointID, seqNum, payload))
  }

  def getFIFOState:Map[ChannelEndpointID, Long] = idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def broadcastMarker(marker:FIFOMarker): Unit ={
    idToSequenceNums.foreach{
      case (outChannel, seq) =>
        logger.info(s"send $marker to ${outChannel}")
        val inChannelEndpointID = ChannelEndpointID(actorId, outChannel.isControlChannel)
        handler(outChannel.endpointWorker, WorkflowFIFOMessage(inChannelEndpointID, seq.get(), marker))
    }
  }

}
