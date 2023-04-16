package edu.uci.ics.amber.engine.common.lbmq

import java.util

/**
  * Chooses the next queue to be used from the highest priority group. If no queue is found it searches the lower
  * priority groups and so on until it finds a queue.
  */
class DefaultSubQueueSelection[K, E >: Null <: AnyRef] extends LinkedBlockingMultiQueue.SubQueueSelection[K, E] {
  private var priorityGroups:util.ArrayList[LinkedBlockingMultiQueue[K, E]#PriorityGroup] = _

  override def getNext: LinkedBlockingSubQueue[K, E] = {
    priorityGroups.forEach( priorityGroup => {
      val subQueue = priorityGroup.getNextSubQueue
      if (subQueue != null) return subQueue
    })
    null
  }

  override def peek: E = {
    priorityGroups.forEach( priorityGroup => {
      val dequed = priorityGroup.peek
      if (dequed != null) return dequed
    })
    null
  }

  def setPriorityGroups(priorityGroups: util.ArrayList[LinkedBlockingMultiQueue[K, E]#PriorityGroup]): Unit = {
    this.priorityGroups = priorityGroups
  }
}
