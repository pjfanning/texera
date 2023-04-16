package edu.uci.ics.amber.engine.common.lbmq

import java.util
import java.util.{Iterator, NoSuchElementException}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.{Condition, ReentrantLock}

class LinkedBlockingSubQueue[K,E >: Null <: AnyRef](val key: K,
                                                    val capacity: Int,
                                                    totalCount: AtomicInteger,
                                                    takeLock: ReentrantLock,
                                                    notEmpty: Condition) extends Serializable {

  if(capacity <= 0){
    throw new IllegalArgumentException("capacity must > 0")
  }

  private[lbmq] var priorityGroup: LinkedBlockingMultiQueue[K, E]#PriorityGroup = _

  private[lbmq] val putLock: ReentrantLock = new ReentrantLock
  private[lbmq] val notFull: Condition = putLock.newCondition

  private[lbmq] val count: AtomicInteger = new AtomicInteger
  private[lbmq] var enabled: Boolean = true

  def remainingCapacity: Int = capacity - count.get

  /** Head of linked list. Invariant: head.item == null */
  private[lbmq] var head: MultiQueueNode[E] = new MultiQueueNode[E](null)

  /** Tail of linked list. Invariant: last.next == null */
  private[lbmq] var last: MultiQueueNode[E] = head

  def signalNotEmpty(): Unit = {
    takeLock.lock()
    try notEmpty.signal()
    finally takeLock.unlock()
  }

  /**
    * Atomically removes all the elements from this queue. The queue will be empty after this call returns.
    */
  def clear(): Unit = {
    fullyLock()
    try {
      var h: MultiQueueNode[E] = head
      var p: MultiQueueNode[E] = h.next
      while ( {
        p != null
      }) {
        h.next = h
        p.item = null // help GC

        h = p
        p = h.next
      }
      head = last
      val oldCapacity: Int = count.getAndSet(0)
      if (oldCapacity == capacity) notFull.signal()
      if (enabled) totalCount.getAndAdd(-oldCapacity)
    } finally fullyUnlock()
  }

  /**
    * Enable or disable this sub-queue. Enabled queues' elements are taken from the common head of
    * the multi-queue. Elements from disabled queues are never taken. Elements can be added to a
    * queue regardless of this status (if there is enough remaining capacity).
    *
    * @param status true to enable, false to disable
    */
  def enable(status: Boolean): Unit = {
    fullyLock()
    try {
      val notChanged: Boolean = status == enabled
      if (notChanged) return
      enabled = status
      if (status) { // potentially unblock waiting polls
        val c: Int = count.get
        if (c > 0) {
          totalCount.getAndAdd(c)
          notEmpty.signal()
        }
      }
      else totalCount.getAndAdd(-count.get)
    } finally fullyUnlock()
  }

  /**
    * Returns whether this sub-queue is enabled
    *
    * @return true is this sub-queue is enabled, false if is disabled.
    */
  def isEnabled: Boolean = {
    takeLock.lock()
    try enabled
    finally takeLock.unlock()
  }

  def signalNotFull(): Unit = {
    putLock.lock()
    try notFull.signal()
    finally putLock.unlock()
  }

  private[lbmq] def enqueue(node: MultiQueueNode[E]): Unit = {
    last.next = node
    last = node
  }

  /**
    * Return the number of elements in this sub queue. This method returns the actual number of
    * elements, regardless of whether the queue is enabled or not.
    */
  def size: Int = count.get

  /**
    * Return whether the queue is empty. This method bases its return value in the actual number of
    * elements, regardless of whether the queue is enabled or not.
    */
  def isEmpty: Boolean = size == 0

  @throws[InterruptedException]
  def put(e: E): Unit = {
    if (e == null) throw new NullPointerException
    var oldSize: Long = -1
    /*
             * As this method never fails to insert, it is more efficient to pre-create the node outside the lock, to
             * reduce contention
             */ val node: MultiQueueNode[E] = new MultiQueueNode[E](e)
    putLock.lockInterruptibly()
    try {
      /*
       * Note that count is used in wait guard even though it is not protected by lock. This works because
       * count can only decrease at this point (all other puts are shut out by lock), and we (or some other
       * waiting put) are signaled if it ever changes from capacity. Similarly for all other uses of count in
       * other wait guards.
       */
      while (count.get == capacity) notFull.await()
      enqueue(node)
      if (count.getAndIncrement + 1 < capacity) { // queue not full after adding, notify next offerer
        notFull.signal()
      }
      if (enabled) oldSize = totalCount.getAndIncrement
    } finally putLock.unlock()
    if (oldSize == 0) { // just added an element to an empty queue, notify pollers
      signalNotEmpty()
    }
  }

  @throws[InterruptedException]
  def offer(e: E, timeout: Long, unit: TimeUnit): Boolean = {
    if (e == null) throw new NullPointerException
    var nanos: Long = unit.toNanos(timeout)
    var oldSize: Long = -1
    putLock.lockInterruptibly()
    try {
      while ( {
        count.get == capacity
      }) {
        if (nanos <= 0) return false
        nanos = notFull.awaitNanos(nanos)
      }
      enqueue(new MultiQueueNode[E](e))
      if (count.getAndIncrement + 1 < capacity) notFull.signal()
      if (enabled) oldSize = totalCount.getAndIncrement
    } finally putLock.unlock()
    if (oldSize == 0) signalNotEmpty()
    true
  }

  def add(e:E):Boolean = offer(e)

  def addAll(c: util.Collection[_ <: E]): Boolean = {
    if (c == null) throw new NullPointerException
    var modified = false
    c.forEach(e => { if (add(e)) modified = true})
    modified
  }

  def offer(e: E): Boolean = {
    if (e == null) throw new NullPointerException
    var oldSize: Long = -1
    if (count.get == capacity) return false
    putLock.lock()
    try {
      if (count.get == capacity) return false
      enqueue(new MultiQueueNode[E](e))
      if (count.getAndIncrement + 1 < capacity) notFull.signal()
      if (enabled) oldSize = totalCount.getAndIncrement
    } finally putLock.unlock()
    if (oldSize == 0) signalNotEmpty()
    true
  }

  def remove(o: Any): Boolean = {
    if (o == null) return false
    fullyLock()
    try {
      var trail: MultiQueueNode[E] = head
      var p: MultiQueueNode[E] = trail.next
      while ( {
        p != null
      }) {
        if (o == p.item) {
          unlink(p, trail)
          return true
        }

        trail = p
        p = p.next
      }
      false
    } finally fullyUnlock()
  }

  def contains(o: Any): Boolean = {
    if (o == null) return false
    fullyLock()
    try {
      var p: MultiQueueNode[E] = head.next
      while ( {
        p != null
      }) {
        if (o == p.item) return true

        p = p.next
      }
      false
    } finally fullyUnlock()
  }

  /** Unlinks interior Node p with predecessor trail. */
  private[lbmq] def unlink(p: MultiQueueNode[E], trail: MultiQueueNode[E]): Unit = { // assert isFullyLocked();
    // p.next is not changed, to allow iterators that are traversing p to maintain their
    // weak-consistency
    // guarantee.
    p.item = null
    trail.next = p.next
    if (last eq p) last = trail
    if (count.getAndDecrement == capacity) notFull.signal()
    if (enabled) totalCount.getAndDecrement
  }

  /** Locks to prevent both puts and takes. */
  private[lbmq] def fullyLock(): Unit = {
    takeLock.lock()
    putLock.lock()
  }

  /** Unlocks to allow both puts and takes. */
  private[lbmq] def fullyUnlock(): Unit = {
    putLock.unlock()
    takeLock.unlock()
  }

  /* Tells whether both locks are held by current thread. */
  // private boolean isFullyLocked() {
  // return putLock.isHeldByCurrentThread() && takeLock.isHeldByCurrentThread();
  // }

  /**
    * Removes a node from head of queue.
    *
    * @return the node
    */
  private[lbmq] def dequeue: E = { // assert takeLock.isHeldByCurrentThread();
    // assert size() > 0;
    val h: MultiQueueNode[E] = head
    val first: MultiQueueNode[E] = h.next
    h.next = h
    head = first
    val x: E = first.item
    first.item = null
    x
  }

  override def toString: String = {
    fullyLock()
    try {
      var p: MultiQueueNode[E] = head.next
      if (p == null) return "[]"
      val sb: StringBuilder = new StringBuilder
      sb.append('[')

      while (true) {
        val e: E = p.item
        sb.append(e)
        p = p.next
        if (p == null) {
          return sb.append(']').toString
        }
        sb.append(", ")
      }
      ""
    } finally{
      fullyUnlock()
    }
  }

  def toArray: Array[AnyRef] = {
    fullyLock()
    try {
      val size: Int = count.get
      val a: Array[AnyRef] = new Array[AnyRef](size)
      var k: Int = 0
      var p: MultiQueueNode[E] = head.next
      while ( {
        p != null
      }) {
        a({
          k += 1; k - 1
        }) = p.item

        p = p.next
      }
      a
    } finally fullyUnlock()
  }

  /**
    * Returns an iterator over the elements in this queue in proper sequence. The elements will be
    * returned in order from first (head) to last (tail).
    *
    * <p>The returned iterator is <a href="package-summary.html#Weakly"><i>weakly
    * consistent</i></a>.
    *
    * @return an iterator over the elements in this queue in proper sequence
    */
  def iterator: util.Iterator[E] = new Itr()

  private[lbmq] class Itr() extends util.Iterator[E] {
    /**
      * Basic weakly-consistent iterator. At all times hold the next item to hand out so that if
      * hasNext() reports true, we will still have it to return even if lost race with a take, etc.
      */
    private var current: MultiQueueNode[E] = null
    private var lastRet: MultiQueueNode[E] = null
    private var currentElement: E = null

    fullyLock()
    try {
      current = head.next
      if (current != null) currentElement = current.item
    } finally fullyUnlock()

    override def hasNext: Boolean = current != null

    /**
      * Returns the next live successor of p, or null if no such.
      *
      * <p>Unlike other traversal methods, iterators need to handle both: - dequeued nodes (p.next
      * == p) - (possibly multiple) interior removed nodes (p.item == null)
      */
    private def nextNode(p: MultiQueueNode[E]): MultiQueueNode[E] = {
      var current = p
      while (true) {
        val s: MultiQueueNode[E] = p.next
        if (s == p) return head.next
        if (s == null || s.item != null) return s
        current = s
      }
      null
    }

    def next: E = {
      fullyLock()
      try {
        if (current == null){
          throw new NoSuchElementException()
        }
        val x: E = currentElement
        lastRet = current
        current = nextNode(current)
        currentElement = if (current == null) null
        else current.item
        x
      } finally fullyUnlock()
    }

    override def remove(): Unit = {
      if (lastRet == null) {
        throw new IllegalStateException()
      }
      fullyLock()
      try {
        val node: MultiQueueNode[E] = lastRet
        lastRet = null
        var trail: MultiQueueNode[E] = head
        var p: MultiQueueNode[E] = trail.next
        var stop = false
        while (!stop && p != null) {
          if (p == node) {
            unlink(p, trail)
            stop = true
          }
          trail = p
          p = p.next
        }
      } finally {
        fullyUnlock()
      }
    }
  }

}