package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

abstract class CreditMonitor extends Serializable {
  def getSenderCredits(channel: ChannelEndpointID): Int
  def increaseCredit(channel: ChannelEndpointID): Unit
  def decreaseCredit(channel: ChannelEndpointID): Unit
}

class CreditMonitorWithMaxCredit extends CreditMonitor {
  def getSenderCredits(channel: ChannelEndpointID): Int = {
    Constants.unprocessedBatchesCreditLimitPerSender
  }
  def increaseCredit(channel: ChannelEndpointID): Unit = {}
  def decreaseCredit(channel: ChannelEndpointID): Unit = {}
}

class CreditMonitorImpl extends CreditMonitor {
  // the values in below maps are in tuples (not batches)
  private val inputTuplesPutInQueue =
    new mutable.HashMap[ChannelEndpointID, Long]() // read and written by main thread
  @volatile private var inputTuplesTakenOutOfQueue =
    new mutable.HashMap[ChannelEndpointID, Long]() // written by DP thread, read by main thread

  def getSenderCredits(channel: ChannelEndpointID): Int = {
    (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutInQueue
      .getOrElseUpdate(channel, 0L) - inputTuplesTakenOutOfQueue.getOrElseUpdate(
      channel,
      0L
    )).toInt) / Constants.defaultBatchSize
  }

  def increaseCredit(channel: ChannelEndpointID): Unit = {
    if (inputTuplesPutInQueue.contains(channel)) {
      inputTuplesPutInQueue(channel) += 1
    } else {
      inputTuplesPutInQueue(channel) = 1
    }
  }

  def decreaseCredit(channel: ChannelEndpointID): Unit = {
    if (inputTuplesTakenOutOfQueue.contains(channel)) {
      inputTuplesTakenOutOfQueue(channel) += 1
    } else {
      inputTuplesTakenOutOfQueue(channel) = 1
    }
  }
}
