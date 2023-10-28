package edu.uci.ics.amber.engine.architecture.common

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock

class DPQueue[T] {
  private val queue = new LinkedBlockingQueue[T]
  private val lock = new ReentrantLock

  @throws[InterruptedException]
  def offer(item: T): Unit = {
    lock.lock()
    try queue.offer(item)
    finally lock.unlock()
  }

  @throws[InterruptedException]
  def take: T = {
    lock.lock()
    try queue.take
    finally lock.unlock()
  }

  def size: Int = {
    lock.lock()
    try queue.size()
    finally lock.unlock()
  }

  def isBlocked: Boolean = lock.isLocked

  // Use this to block the queue
  def block(): Unit = {
    lock.lock()
  }

  // Use this to unblock the queue
  def unblock(): Unit = {
    lock.unlock()
  }
}
