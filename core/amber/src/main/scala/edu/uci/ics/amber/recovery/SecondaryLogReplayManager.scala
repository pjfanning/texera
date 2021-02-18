package edu.uci.ics.amber.recovery

import scala.collection.mutable

class SecondaryLogReplayManager(storage:SecondaryLogStorage) {

  private val correlatedSeq = storage.load().to[mutable.Queue]

  def isCurrentCorrelated(cur:Long):Boolean = {
    correlatedSeq.head == cur
  }

  def advanceCursor(): Unit ={
    correlatedSeq.dequeue()
  }

}
