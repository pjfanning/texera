package edu.uci.ics.amber.engine.architecture.worker

import java.util
import java.util.concurrent.CompletableFuture

class ProactiveDeque[T] {

  val queue = new util.ArrayDeque[T]()
  var availableFuture:CompletableFuture[Unit] = new CompletableFuture[Unit]()

  def addFirst(t:T): Unit ={
    synchronized {
      queue.addFirst(t)
      availableFuture.complete()
    }
  }

  def dequeue(): T ={
    queue.pollFirst()
  }

  def enqueue(t:T):Unit = {
    synchronized{
      queue.addLast(t)
      availableFuture.complete()
    }
  }

  def availableNotification():CompletableFuture[Unit] = {
    synchronized{
      if(queue.isEmpty && availableFuture.isDone){
        availableFuture = new CompletableFuture[Unit]()
      }
      availableFuture
    }
  }

  def size(): Int ={
    queue.size
  }

}
