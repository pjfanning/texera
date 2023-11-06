package edu.uci.ics.amber.engine.common

import java.util.concurrent.{LinkedBlockingQueue, Semaphore}

class DPOutputQueue[T] {
  private val queue = new LinkedBlockingQueue[T]
  private val offerlock = new Semaphore(1)

  @throws[InterruptedException]
  def offer(item: T): Unit = {
    offerlock.acquire()
    try queue.offer(item)
    finally offerlock.release()
  }

  @throws[InterruptedException]
  def take: T = {
    queue.take
  }

  def size: Int = {
    queue.size()
  }

  def isBlocked: Boolean = offerlock.availablePermits() == 0

  // Use this to block the queue
  def block(): Unit = {
    if (!isBlocked) {
      offerlock.acquire()
    }
  }

  // Use this to unblock the queue
  def unblock(): Unit = {
    if (isBlocked) {
      offerlock.release()
    }
  }
}
