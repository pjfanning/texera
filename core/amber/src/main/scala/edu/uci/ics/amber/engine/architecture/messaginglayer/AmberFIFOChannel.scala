package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.{DataPayload, WorkflowFIFOMessagePayload}

import scala.collection.mutable


/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel {

  val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessagePayload]
  var current = 0L

  def setCurrent(value: Long): Unit = {
    current = value
  }

  def acceptMessage(seq:Long, payload:WorkflowFIFOMessagePayload):Iterator[WorkflowFIFOMessagePayload] = {
    if (isDuplicated(seq)) {
      Iterator.empty
    } else if (isAhead(seq)) {
      stash(seq, payload)
      Iterator.empty
    } else{
      enforceFIFO(payload)
    }
  }

  def isDuplicated(sequenceNumber: Long): Boolean =
    sequenceNumber < current || ofoMap.contains(sequenceNumber)

  def isAhead(sequenceNumber: Long): Boolean = sequenceNumber > current

  private def stash(sequenceNumber: Long, data: WorkflowFIFOMessagePayload): Unit = {
    ofoMap(sequenceNumber) = data
  }

  def enforceFIFO(data: WorkflowFIFOMessagePayload): Iterator[WorkflowFIFOMessagePayload] = {
    val resultIterator = new Iterator[WorkflowFIFOMessagePayload]{
      private val outputBuffer = mutable.Queue[WorkflowFIFOMessagePayload](data)
      current += 1
      override def hasNext: Boolean = {
        if(outputBuffer.isEmpty && ofoMap.contains(current)){
          outputBuffer.enqueue(ofoMap(current))
          ofoMap.remove(current)
          current += 1
        }
        outputBuffer.nonEmpty
      }

      override def next(): WorkflowFIFOMessagePayload = {
        outputBuffer.dequeue()
      }
    }
    resultIterator
  }
}
