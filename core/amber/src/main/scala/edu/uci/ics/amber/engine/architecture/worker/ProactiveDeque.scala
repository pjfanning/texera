package edu.uci.ics.amber.engine.architecture.worker

import java.util
import java.util.concurrent.CompletableFuture

class ProactiveDeque[T](preEnqueue: T => Unit = null, postDequeue: T => Unit = null) {

  val queue = new util.ArrayDeque[T]()
  var availableFuture: CompletableFuture[Unit] = new CompletableFuture[Unit]()

  def addFirst(t: T): Unit = {
    synchronized {
      queue.addFirst(t)
      availableFuture.complete(())
    }
  }

  def dequeue(): T = {
    val result = queue.pollFirst()
    if (postDequeue != null) {
      postDequeue(result)
    }
    result
  }

  def enqueue(t: T): Unit = {
    synchronized {
      if (preEnqueue != null) {
        preEnqueue(t)
      }
      queue.addLast(t)
      availableFuture.complete(())
    }
  }

  def availableNotification(): CompletableFuture[Unit] = {
    synchronized {
      if (queue.isEmpty && availableFuture.isDone) {
        availableFuture = new CompletableFuture[Unit]()
      }
      availableFuture
    }
  }

  def size(): Int = {
    queue.size
  }

}
