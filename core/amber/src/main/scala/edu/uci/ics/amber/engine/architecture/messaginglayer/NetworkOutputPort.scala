package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{DataPayload, FIFOMarker, WorkflowFIFOMessagePayload}

import java.util.concurrent.atomic.AtomicLong
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, SELF}

import scala.collection.mutable

/**
  * NetworkOutput for generating sequence number when sending payloads
  * @param selfID ActorVirtualIdentity for the sender
  * @param handler actual sending logic
  */
class NetworkOutputPort(
    val actorId: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, ActorVirtualIdentity, Boolean, Long, WorkflowFIFOMessagePayload) => Unit
) extends AmberLogging with Serializable {
  private val idToSequenceNums = new mutable.HashMap[(ActorVirtualIdentity, Boolean), AtomicLong]()

  def sendTo(to: ActorVirtualIdentity, payload: WorkflowFIFOMessagePayload): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = actorId
    }
    val isData = payload.isInstanceOf[DataPayload]
    val seqNum = idToSequenceNums.getOrElseUpdate((receiverId,isData), new AtomicLong()).getAndIncrement()
    handler(to, actorId, isData, seqNum, payload)
  }

  def getFIFOState:Map[(ActorVirtualIdentity, Boolean), Long] = idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def broadcastMarker(marker:FIFOMarker): Unit ={
    idToSequenceNums.foreach{
      case (channelId, seq) =>
        if(channelId._1 != CLIENT && channelId._1 != SELF){
          logger.info(s"send $marker to ${channelId}")
          handler(channelId._1, actorId, channelId._2, seq.get(), marker)
        }
    }
  }

}
