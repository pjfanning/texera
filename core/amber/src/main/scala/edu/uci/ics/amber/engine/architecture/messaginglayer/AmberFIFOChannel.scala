package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowMessage.getInMemSize

import scala.collection.mutable

/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel() {

  private val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessage]
  private var current = 0L
  private var enabled = true
  private val fifoQueue = new mutable.Queue[WorkflowFIFOMessage]
  private var consumedCredit = 0L

  def acceptMessage(msg: WorkflowFIFOMessage): Unit = {
    val seq = msg.sequenceNumber
    val payload = msg.payload
    if (isDuplicated(seq)) {
      println(s"received duplicated message $payload with seq = $seq while current seq = $current")
    } else if (isAhead(seq)) {
      println(s"received ahead message $payload with seq = $seq while current seq = $current")
      stash(seq, msg)
    } else {
      enforceFIFO(msg)
    }
  }

  private def isDuplicated(sequenceNumber: Long): Boolean =
    sequenceNumber < current || ofoMap.contains(sequenceNumber)

  private def isAhead(sequenceNumber: Long): Boolean = sequenceNumber > current

  private def stash(sequenceNumber: Long, data: WorkflowFIFOMessage): Unit = {
    ofoMap(sequenceNumber) = data
  }

  private def enforceFIFO(data: WorkflowFIFOMessage): Unit = {
    fifoQueue.enqueue(data)
    current += 1
    while (ofoMap.contains(current)) {
      fifoQueue.enqueue(ofoMap(current))
      ofoMap.remove(current)
      current += 1
    }
  }

  def take: WorkflowFIFOMessage = {
    val msg = fifoQueue.dequeue()
    synchronized {
      consumedCredit += getInMemSize(msg)
    }
    msg
  }

  def hasMessage: Boolean = fifoQueue.nonEmpty

  def enable(isEnabled: Boolean): Unit = {
    this.enabled = isEnabled
  }

  def isEnabled: Boolean = enabled

  def getTotalMessageSize: Long = {
    if (fifoQueue.nonEmpty) {
      fifoQueue.map(getInMemSize(_)).sum
    } else {
      0
    }
  }

  def getTotalStashedSize: Long =
    if (ofoMap.nonEmpty) {
      ofoMap.values.map(getInMemSize(_)).sum
    } else {
      0
    }

  def getConsumedCredits: Long = {
    var result = 0L
    synchronized {
      result = consumedCredit
      consumedCredit = 0L
    }
    result
  }
}
