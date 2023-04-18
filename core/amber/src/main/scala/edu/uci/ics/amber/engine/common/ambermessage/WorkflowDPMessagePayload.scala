package edu.uci.ics.amber.engine.common.ambermessage

import java.util.concurrent.CompletableFuture

trait WorkflowDPMessagePayload

// two kinds of internal system-level message
// not covered by fault-tolerance!

case class FuncDelegate[T](func: () => T, future: CompletableFuture[T]) extends WorkflowDPMessagePayload{
  type returnType = T
}

case class FuncDelegateNoReturn(func: () => Unit) extends WorkflowDPMessagePayload
