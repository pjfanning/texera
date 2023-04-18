package edu.uci.ics.amber.engine.common.ambermessage

import java.util.concurrent.CompletableFuture

trait WorkflowDPMessagePayload

case class FuncDelegate[T](func: () => T, future: CompletableFuture[T]) extends WorkflowDPMessagePayload{
  type returnType = T
}
