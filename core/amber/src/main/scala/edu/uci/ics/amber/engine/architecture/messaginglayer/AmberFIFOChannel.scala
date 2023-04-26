package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DataFrame, DataPayload, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable


/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel(id:ActorVirtualIdentity) {

  val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessagePayload]
  var current = 0L

  def acceptMessage(seq:Long, payload:WorkflowFIFOMessagePayload):Iterator[WorkflowFIFOMessagePayload] = {
    if (isDuplicated(seq)) {
      println(s"received duplicated message $payload with seq = $seq while current seq = $current")
      Iterator.empty
    } else if (isAhead(seq)) {
      println(s"received ahead message $payload with seq = $seq while current seq = $current")
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
      if(data.isInstanceOf[DataFrame] && id.name.contains("Projection-operator-6d254f9f-523a-4426-8f80-4f5fa030b441-main-0")){
        val frame = data.asInstanceOf[DataFrame].frame
        println(s"received batch ${frame.headOption} seq = $current")
      }
      private val outputBuffer = mutable.Queue[WorkflowFIFOMessagePayload](data)
      current += 1
      override def hasNext: Boolean = {
        if(outputBuffer.isEmpty && ofoMap.contains(current)){
          val currentPayload = ofoMap(current)
          outputBuffer.enqueue(currentPayload)
          ofoMap.remove(current)
          if(currentPayload.isInstanceOf[DataFrame] && id.name.contains("Projection-operator-6d254f9f-523a-4426-8f80-4f5fa030b441-main-0")){
            val frame = currentPayload.asInstanceOf[DataFrame].frame
            println(s"received batch ${frame.headOption} seq = $current")
          }
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
