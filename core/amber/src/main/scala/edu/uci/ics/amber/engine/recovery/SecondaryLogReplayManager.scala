package edu.uci.ics.amber.engine.recovery

import com.twitter.util.Promise

import scala.collection.mutable

class SecondaryLogReplayManager(storage: SecondaryLogStorage) {

  def isReplaying: Boolean = !completion.isDefined

  def onComplete(callback: () => Unit): Unit = {
    completion.onSuccess(x => callback())
  }

  private val completion = new Promise[Void]()

  private val correlatedSeq = storage.load().to[mutable.Queue]

  checkIfCompleted()

  def isCurrentCorrelated(cur: Long): Boolean = {
    correlatedSeq.nonEmpty && correlatedSeq.head == cur
  }

  def advanceCursor(): Unit = {
    correlatedSeq.dequeue()
    checkIfCompleted()
  }

  @inline
  private[this] def checkIfCompleted(): Unit = {
    if (correlatedSeq.isEmpty && !completion.isDefined) {
      completion.setValue(null)
    }
  }

}
