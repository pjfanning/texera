package edu.uci.ics.amber.engine.common.ambermessage

import java.util.concurrent.CompletableFuture

trait WorkflowDPMessagePayload

// two kinds of internal system-level message
// not covered by fault-tolerance!

case class StartSync() extends WorkflowDPMessagePayload
