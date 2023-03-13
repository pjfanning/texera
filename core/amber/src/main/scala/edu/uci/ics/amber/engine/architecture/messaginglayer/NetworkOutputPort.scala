package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.AmberLogging
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
    val actorId: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, ActorVirtualIdentity, Boolean, Long, WorkflowFIFOMessagePayload) => Unit
) extends AmberLogging with Serializable {
  private val idToSequenceNums = new mutable.HashMap[(ActorVirtualIdentity, Boolean), AtomicLong]()
  private val sentMessages = new mutable.HashMap[(ActorVirtualIdentity, Boolean), mutable.Queue[(Long,WorkflowFIFOMessagePayload)]]()

  def sendTo(to: ActorVirtualIdentity, payload: WorkflowFIFOMessagePayload): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = actorId
    }
    val isData = payload.isInstanceOf[DataPayload]
    val seqNum = idToSequenceNums.getOrElseUpdate((receiverId,isData), new AtomicLong()).getAndIncrement()
    val channel = (to, isData)
    if(!sentMessages.contains(channel)){
      sentMessages(channel) = mutable.Queue()
    }
    sentMessages(channel).enqueue((seqNum,payload))
    handler(to, actorId, isData, seqNum, payload)
  }

  def clearSentMessages():Unit = {
    sentMessages.clear()
  }


  def receiveWatermark(channel:(ActorVirtualIdentity, Boolean), watermark:Long):Unit = {
    if (sentMessages.contains(channel)) {
      logger.info(s"removing messages to $channel till seq num = $watermark")
      while (true) {
        sentMessages(channel).headOption match {
          case Some((seq, msg)) =>
            if (seq < watermark) {
              sentMessages(channel).dequeue()
            } else {
              return
            }
          case None => return
        }
      }
    }
  }

  def resendMessages(exclude:Set[ActorVirtualIdentity] = Set()): Unit = {
    sentMessages.foreach {
      case (id, queue) =>
        if(!exclude.contains(id._1)){
          var first = -1L
          var last = -1L
          queue.foreach {
            case (seq, msg) =>
              if(first == -1){
                first = seq
              }
              last = seq
              handler(id._1, actorId,id._2, seq, msg)
          }
          if(first != -1L){
            logger.info(s"resend message to ${id} from seq $first to $last")
          }
        }
    }
  }

  def getFIFOState:Map[(ActorVirtualIdentity, Boolean), Long] = idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def broadcastMarker(marker:SnapshotMarker): Unit ={
    idToSequenceNums.foreach{
      case (channelId, seq) =>
        if(channelId._1 != CLIENT){
          handler(channelId._1, actorId, channelId._2, seq.get(), marker)
        }
    }
  }

}
