package edu.uci.ics.amber.engine.architecture.messaginglayer

import java.util.concurrent.atomic.AtomicLong

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import scala.collection.mutable

/**
  * NetworkOutput for generating sequence number when sending payloads
  * @param selfID ActorVirtualIdentity for the sender
  * @param handler actual sending logic
  * @tparam T payload
  */
class NetworkOutputPort[T](
    selfID: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, ActorVirtualIdentity, Long, T) => Unit
) extends Serializable {
  private val idToSequenceNums = new mutable.HashMap[ActorVirtualIdentity, AtomicLong]()
  private val sentMessages = new mutable.HashMap[ActorVirtualIdentity, mutable.Queue[(Long,T)]]()

  def sendTo(to: ActorVirtualIdentity, payload: T): Unit = {
    var receiverId = to
    if (to == SELF) {
      // selfID and VirtualIdentity.SELF should be one key
      receiverId = selfID
    }
    val seqNum = idToSequenceNums.getOrElseUpdate(receiverId, new AtomicLong()).getAndIncrement()
    if(!sentMessages.contains(to)){
      sentMessages(to) = mutable.Queue[(Long,T)]()
    }
    sentMessages(to).enqueue((seqNum,payload))
    handler(to, selfID, seqNum, payload)
  }

  def getFIFOState:Map[ActorVirtualIdentity, Long] = idToSequenceNums.map(x => (x._1, x._2.get())).toMap

  def clearSentMessages():Unit = {
    sentMessages.clear()
  }

  def receiveWatermark(id:ActorVirtualIdentity, watermark:Long):Unit = {
    if(sentMessages.contains(id)){
      while(true){
        sentMessages(id).headOption match {
          case Some((seq, msg)) =>
            if(seq < watermark){
              sentMessages(id).dequeue()
            } else{
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
        if(!exclude.contains(id)){
          queue.foreach {
            case (seq, msg) =>
              handler(id, selfID, seq, msg)
          }
        }
    }
  }
}
