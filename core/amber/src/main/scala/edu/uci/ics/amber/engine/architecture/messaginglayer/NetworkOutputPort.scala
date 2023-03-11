package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.{DataPayload, SnapshotMarker, WorkflowFIFOMessagePayload}

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
    selfID: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, ActorVirtualIdentity, Boolean, Long, WorkflowFIFOMessagePayload) => Unit
) extends Serializable {
  private val idToSequenceNums = new mutable.HashMap[(ActorVirtualIdentity, Boolean), AtomicLong]()

  def sendTo(to: ActorVirtualIdentity, payload: WorkflowFIFOMessagePayload): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = selfID
    }
    val isData = payload.isInstanceOf[DataPayload]
    val seqNum = idToSequenceNums.getOrElseUpdate((receiverId,isData), new AtomicLong()).getAndIncrement()
    handler(to, selfID, isData, seqNum, payload)
  }

  def getFIFOState:Map[(ActorVirtualIdentity, Boolean), Long] = idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def broadcastMarker(marker:SnapshotMarker): Unit ={
    idToSequenceNums.foreach{
      case (channelId, seq) =>
        if(channelId._1 != CLIENT){
          handler(channelId._1, selfID, channelId._2, seq.get(), marker)
        }
    }
  }

}
