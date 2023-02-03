package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class CreditMonitor {
  // the values in below maps are in tuples (not batches)
  private var inputTuplesPutInQueue =
    new mutable.HashMap[ActorVirtualIdentity, Long]() // read and written by main thread
  @volatile private var inputTuplesTakenOutOfQueue =
    new mutable.HashMap[ActorVirtualIdentity, Long]() // written by DP thread, read by main thread

  def getSenderCredits(sender: ActorVirtualIdentity): Int = {
    (Constants.unprocessedBatchesCreditLimitPerSender * Constants.defaultBatchSize - (inputTuplesPutInQueue
      .getOrElseUpdate(sender, 0L) - inputTuplesTakenOutOfQueue.getOrElseUpdate(
      sender,
      0L
    )).toInt) / Constants.defaultBatchSize
  }

  def increaseCredit(sender: ActorVirtualIdentity): Unit ={
    if(inputTuplesPutInQueue.contains(sender)) {
      inputTuplesPutInQueue(sender) += 1
    }
    else {
      inputTuplesPutInQueue(sender) = 1
    }
  }


  def decreaseCredit(sender: ActorVirtualIdentity): Unit ={
    if(inputTuplesTakenOutOfQueue.contains(sender)) {
      inputTuplesTakenOutOfQueue(sender) += 1
    }
    else {
      inputTuplesTakenOutOfQueue(sender) = 1
    }
  }
}
