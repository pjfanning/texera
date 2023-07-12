package edu.uci.ics.amber.engine.common.ambermessage

trait WorkflowDPMessagePayload

// two kinds of internal system-level message
// not covered by fault-tolerance!

case class StartSync() extends WorkflowDPMessagePayload
