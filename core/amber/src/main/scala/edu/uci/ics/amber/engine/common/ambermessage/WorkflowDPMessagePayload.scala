package edu.uci.ics.amber.engine.common.ambermessage

import java.util.concurrent.CompletableFuture

trait WorkflowDPMessagePayload

// internal system-level message
// not covered by fault-tolerance!

case class StartSync() extends WorkflowDPMessagePayload
