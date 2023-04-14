package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

abstract class CreditMonitor extends Serializable {
  def getSenderCredits(actor: ActorVirtualIdentity): Int
  def increaseCredit(actor: ActorVirtualIdentity): Unit
  def decreaseCredit(actor: ActorVirtualIdentity): Unit
}

class CreditMonitorWithMaxCredit extends CreditMonitor {
  def getSenderCredits(actor: ActorVirtualIdentity): Int = {
    Constants.unprocessedBatchesCreditLimitPerSender
  }
  def increaseCredit(actor: ActorVirtualIdentity): Unit = {}
  def decreaseCredit(actor: ActorVirtualIdentity): Unit = {}
}

class CreditMonitorImpl extends CreditMonitor {
  // the values in below maps are in tuples (not batches)
  private val inputTuplesPutInQueue =
    new mutable.HashMap[ActorVirtualIdentity, Long]() // read and written by main thread
  @volatile private var inputTuplesTakenOutOfQueue =
    new mutable.HashMap[ActorVirtualIdentity, Long]() // written by DP thread, read by main thread

  def getSenderCredits(actor: ActorVirtualIdentity): Int = {
    (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutInQueue
      .getOrElseUpdate(actor, 0L) - inputTuplesTakenOutOfQueue.getOrElseUpdate(
      actor,
      0L
    )).toInt) / Constants.defaultBatchSize
  }

  def increaseCredit(actor: ActorVirtualIdentity): Unit = {
    if (inputTuplesPutInQueue.contains(actor)) {
      inputTuplesPutInQueue(actor) += 1
    } else {
      inputTuplesPutInQueue(actor) = 1
    }
  }

  def decreaseCredit(actor: ActorVirtualIdentity): Unit = {
    if (inputTuplesTakenOutOfQueue.contains(actor)) {
      inputTuplesTakenOutOfQueue(actor) += 1
    } else {
      inputTuplesTakenOutOfQueue(actor) = 1
    }
  }
}
