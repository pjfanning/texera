package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.amber.engine.common.virtualidentity.VirtualIdentity

trait WorkflowMessage extends Serializable

trait WorkflowFIFOMessage extends WorkflowMessage {
  val from: VirtualIdentity
  val sequenceNumber: Long
}
