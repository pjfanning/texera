package edu.uci.ics.amber.engine.common.lbmq

/*
 * Derived from work made by Doug Lea with assistance from members of JCP JSR-166 Expert Group
 * (https://jcp.org/en/jsr/detail?id=166). The original work is in the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.io.Serializable
import java.util
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import scala.util.control.Breaks.{break, breakable}

object LinkedBlockingMultiQueue {

  /** Allows to choose the next subQueue. */
  trait SubQueueSelection[K, E >: Null <: AnyRef] {

    /**
      * Returns the next subQueue to be used.
      *
      * @return a subQueue
      */
    def getNext: LinkedBlockingSubQueue[K, E]

    /**
      * Returns the next element from the queue but keeps it in the queue.
      *
      * @return the next element from the queue
      */
    def peek: E

    /**
      * Sets priority groups.
      *
      * @param priorityGroups priority groups
      */
    def setPriorityGroups(
        priorityGroups: util.ArrayList[LinkedBlockingMultiQueue[K, E]#PriorityGroup]
    ): Unit
  }
}

class LinkedBlockingMultiQueue[K, E >: Null <: AnyRef](
    /** Allows to choose the next subQueue to be used. */
    val subQueueSelection: LinkedBlockingMultiQueue.SubQueueSelection[K, E]
) extends AbstractPollable[E]
    with Serializable {

  final private val subQueues = new ConcurrentHashMap[K, LinkedBlockingSubQueue[K, E]]

  /** Lock held by take, poll, etc */
  final private val takeLock = new ReentrantLock

  /** Wait queue for waiting takes */
  final private val notEmpty = takeLock.newCondition

  /** Current number of elements in enabled sub-queues */
  final private val totalCount = new AtomicInteger

  /** A list of priority groups. Group consists of multiple queues. */
  val priorityGroups = new util.ArrayList[LinkedBlockingMultiQueue[K, E]#PriorityGroup]

  this.subQueueSelection.setPriorityGroups(this.priorityGroups)

  def this() = {
    this(new DefaultSubQueueSelection[K, E]())
  }

  /** Set of sub-queues with the same priority */
  private[lbmq] class PriorityGroup(val priority: Int) {
    val queues = new util.ArrayList[LinkedBlockingSubQueue[K, E]](0)
    private[lbmq] var nextIdx = 0

    private[lbmq] def addQueue(subQueue: LinkedBlockingSubQueue[K, E]): Unit = {
      queues.add(subQueue)
      subQueue.priorityGroup = this
    }

    private[lbmq] def removeQueue(removed: LinkedBlockingSubQueue[K, E]): Unit = {
      val it = queues.iterator
      while ({
        it.hasNext
      }) {
        val subQueue = it.next
        if (subQueue.key == removed.key) {
          removed.putLock.lock
          try {
            it.remove()
            if (nextIdx == queues.size) nextIdx = 0
            if (subQueue.enabled) totalCount.getAndAdd(-removed.size)
            return
          } finally removed.putLock.unlock
        }
      }
    }

    private[lbmq] def getNextSubQueue
        : LinkedBlockingSubQueue[K, E] = { // assert takeLock.isHeldByCurrentThread();
      val startIdx = nextIdx
      val queues = this.queues
      do {
        val child = queues.get(nextIdx)
        nextIdx += 1
        if (nextIdx == queues.size) nextIdx = 0
        if (child.enabled && child.size > 0) return child
      } while ({
        nextIdx != startIdx
      })
      null
    }

    private[lbmq] def drainTo(c: util.Collection[_ >: E], maxElements: Int) = {
      var drained = 0
      var emptyQueues = 0
      do {
        val child = queues.get(nextIdx)
        nextIdx += 1
        if (nextIdx == queues.size) nextIdx = 0
        if (child.enabled && child.size > 0) {
          emptyQueues = 0
          c.add(child.dequeue)
          drained += 1
          val oldSize = child.count.getAndDecrement
          if (oldSize == child.capacity) child.signalNotFull()
        } else emptyQueues += 1
      } while ({
        drained < maxElements && emptyQueues < queues.size
      })
      drained
    }

    private[lbmq] def peek: E = {
      val startIdx = nextIdx
      do {
        val child = queues.get(nextIdx)
        if (child.enabled && child.size > 0) return child.head.next.item
        else {
          nextIdx += 1
          if (nextIdx == queues.size) nextIdx = 0
        }
      } while ({
        nextIdx != startIdx
      })
      null
    }
  }

  /**
    * Add a sub queue if absent
    *
    * @param key      the key used to identify the queue
    * @param priority the queue priority, a lower number means higher priority
    * @return the previous queue associated with the specified key, or {@code null} if there was no
    *         queue for the key
    */
  def addSubQueue(key: K, priority: Int): LinkedBlockingSubQueue[K, E] =
    addSubQueue(key, priority, Integer.MAX_VALUE)

  /**
    * Add a sub-queue if absent
    *
    * @param key      the key used to identify the queue
    * @param priority the queue priority, a lower number means higher priority
    * @param capacity the capacity of the new sub-queue
    * @return the previous queue associated with the specified key, or {@code null} if there was no
    *         queue for the key
    */
  def addSubQueue(key: K, priority: Int, capacity: Int): LinkedBlockingSubQueue[K, E] = {
    val subQueue = new LinkedBlockingSubQueue[K, E](key, capacity, totalCount, takeLock, notEmpty)
    takeLock.lock()
    try {
      val old = subQueues.putIfAbsent(key, subQueue)
      if (old == null) {
        var i = 0
        var added = false
        breakable {
          priorityGroups.forEach(pg => {
            if (pg.priority == priority) {
              pg.addQueue(subQueue)
              added = true
              break
            } else if (pg.priority > priority) {
              val newPg = new PriorityGroup(priority)
              priorityGroups.add(i, newPg)
              newPg.addQueue(subQueue)
              added = true
              break
            }
            i += 1
          })
          if (!added) {
            val newPg = new PriorityGroup(priority)
            priorityGroups.add(newPg)
            newPg.addQueue(subQueue)
          }
        }
      }
      subQueue
    } finally takeLock.unlock()
  }

  /**
    * Remove a sub-queue
    *
    * @param key the key f the sub-queue that should be removed
    * @return the removed LinkedBlockingSubQueue<K,E> or null if the key was not in the map
    */
  def removeSubQueue(key: K): LinkedBlockingSubQueue[K, E] = {
    takeLock.lock()
    try {
      val removed = subQueues.remove(key)
      if (removed != null) {
        removed.priorityGroup.removeQueue(removed)
        if (removed.priorityGroup.queues.size == 0)
          this.priorityGroups.remove(removed.priorityGroup)
      }
      removed
    } finally takeLock.unlock()
  }

  def enableAllSubQueue(): Unit = {
    subQueues.values().forEach { queue =>
      if (!queue.isEnabled) {
        queue.enable(true)
      }
    }
  }

  def disableSubQueueExcept(exceptions: K*): Unit = {
    subQueues.values().forEach { queue =>
      if (queue.isEnabled) {
        queue.enable(false)
      }
    }
    exceptions.foreach { k =>
      val queue = getSubQueue(k)
      if (queue != null && !queue.isEnabled) {
        queue.enable(true)
      }
    }
  }

  /**
    * Gets a sub-queue
    *
    * @param key the key f the sub-queue that should be returned
    * @return the sub-queue with the corresponding key or null if it does not exist
    */
  def getSubQueue(key: K): LinkedBlockingSubQueue[K, E] = subQueues.get(key)

  @throws[InterruptedException]
  override def poll(timeout: Long, unit: TimeUnit): E = {
    var remaining = unit.toNanos(timeout)
    var subQ: LinkedBlockingSubQueue[K, E] = null
    var element: E = null
    var oldSize = 0
    takeLock.lockInterruptibly()
    try {
      while ({
        totalCount.get == 0
      }) {
        if (remaining <= 0) return null
        remaining = notEmpty.awaitNanos(remaining)
      }
      // at this point we know there is an element
      subQ = subQueueSelection.getNext
      element = subQ.dequeue
      oldSize = subQ.count.getAndDecrement
      if (totalCount.getAndDecrement > 1) { // sub-queue still has elements, notify next poller
        notEmpty.signal()
      }
    } finally takeLock.unlock()
    if (oldSize == subQ.capacity) { // we just took an element from a full queue, notify any blocked offers
      subQ.signalNotFull()
    }
    element
  }

  @throws[InterruptedException]
  override def take: E = {
    var subQ: LinkedBlockingSubQueue[K, E] = null
    var oldSize = 0
    var element: E = null
    takeLock.lockInterruptibly()
    try {
      while ({
        totalCount.get == 0
      }) notEmpty.await()
      subQ = subQueueSelection.getNext
      element = subQ.dequeue
      oldSize = subQ.count.getAndDecrement
      if (totalCount.getAndDecrement > 1) notEmpty.signal()
    } finally takeLock.unlock()
    if (oldSize == subQ.capacity) subQ.signalNotFull()
    element
  }

  override def poll: E = {
    var subQ: LinkedBlockingSubQueue[K, E] = null
    var element: E = null
    var oldSize = 0
    takeLock.lock()
    try {
      if (totalCount.get == 0) return null
      subQ = subQueueSelection.getNext
      element = subQ.dequeue
      oldSize = subQ.count.getAndDecrement
      if (totalCount.getAndDecrement > 1) notEmpty.signal()
    } finally takeLock.unlock()
    if (oldSize == subQ.capacity) subQ.signalNotFull()
    element
  }

  override def peek: E = {
    takeLock.lock()
    try if (totalCount.get == 0) null
    else subQueueSelection.peek
    finally takeLock.unlock()
  }

  /**
    * Returns the total size of this multi-queue, that is, the sum of the sizes of all the enabled
    * sub-queues.
    *
    * @return the total size of this multi-queue
    */
  def totalSize: Int = totalCount.get

  /**
    * Returns whether this multi-queue is empty, that is, whether there is any element ready to be
    * taken from the head.
    *
    * @return whether this multi-queue is empty.
    */
  def isEmpty: Boolean = totalSize == 0

  override def drainTo(c: util.Collection[_ >: E]): Int = drainTo(c, Integer.MAX_VALUE)

  override def drainTo(c: util.Collection[_ >: E], maxElements: Int): Int = {
    if (c == null) throw new NullPointerException
    if (maxElements <= 0) return 0
    takeLock.lock()
    try {
      val n = Math.min(maxElements, totalCount.get)
      // ordered iteration, begin with lower index (highest priority)
      var drained = 0
      var i = 0
      while ({
        i < priorityGroups.size && drained < n
      }) {
        drained += priorityGroups.get(i).drainTo(c, n - drained)

        i += 1
      }
      // assert drained == n;
      totalCount.getAndAdd(-drained)
      drained
    } finally takeLock.unlock()
  }

  /**
    * Counts the priority groups currently registered in {@link LinkedBlockingMultiQueue}. Suitable
    * for debugging and testing.
    *
    * @return the number of priority groups currently registered
    */
  def getPriorityGroupsCount: Int = priorityGroups.size
}
