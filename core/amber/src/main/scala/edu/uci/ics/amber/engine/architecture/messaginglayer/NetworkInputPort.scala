package edu.uci.ics.amber.engine.architecture.messaginglayer

import akka.actor.ActorRef
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkAck
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class NetworkInputPort[T](
    val actorId: ActorVirtualIdentity,
    val handler: (ActorVirtualIdentity, T) => Unit
) extends AmberLogging {

  private val idToOrderingEnforcers =
    new mutable.HashMap[ActorVirtualIdentity, OrderingEnforcer[T]]()

  def handleMessage(
      sender: ActorRef,
      senderCredits: Int,
      messageID: Long,
      from: ActorVirtualIdentity,
      sequenceNumber: Long,
      payload: T
  ): Unit = {
    sender ! NetworkAck(messageID, Some(senderCredits))

    OrderingEnforcer.reorderMessage[T](
      idToOrderingEnforcers,
      from,
      sequenceNumber,
      payload
    ) match {
      case Some(iterable) =>
        iterable.foreach(v => handler.apply(from, v))
      case None =>
        // discard duplicate
        logger.info(s"receive duplicated: $payload from $from")
    }
  }

  def overwriteFIFOSeqNum(seqMap: Map[ActorVirtualIdentity, Long]): Unit = {
    seqMap.foreach {
      case (identity, l) =>
        val entry = idToOrderingEnforcers.getOrElseUpdate(identity, new OrderingEnforcer[T]())
        entry.setCurrent(l)
    }
  }

  def getFIFOState: Map[ActorVirtualIdentity, OrderingEnforcer[T]] = idToOrderingEnforcers.toMap

  def setFIFOState(fifoState: Map[ActorVirtualIdentity, OrderingEnforcer[T]]): Unit = {
    idToOrderingEnforcers.clear()
    idToOrderingEnforcers ++= fifoState
  }

  def increaseFIFOSeqNum(id: ActorVirtualIdentity): Unit = {
    idToOrderingEnforcers.getOrElseUpdate(id, new OrderingEnforcer[T]()).current += 1
  }

}
