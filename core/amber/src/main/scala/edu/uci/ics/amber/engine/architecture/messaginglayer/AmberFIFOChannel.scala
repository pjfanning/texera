package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.checkpoint.{PlannedCheckpoint, SavedCheckpoint}
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, DataPayload, WorkflowFIFOMessagePayload}

import scala.collection.mutable


/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel(channel:ChannelEndpointID) {

  val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessagePayload]
  var current = 0L

  var recordingRanges:mutable.HashMap[Long, (Long, PlannedCheckpoint)] = mutable.HashMap.empty
  var isRecording = false
  var recordingInfo: (Long, PlannedCheckpoint) = _

  def addRecording(from:Long, to:Long, chkpt:PlannedCheckpoint): Unit ={
    recordingRanges(from) = (to, chkpt)
  }

  def recordPayload(payload:WorkflowFIFOMessagePayload): Unit ={
    if(isRecording){
      recordingInfo._2.chkpt.addInputData(channel, payload)
      if(recordingInfo._1 == current){
        isRecording = false
        recordingInfo._2.decreaseCompletionCount()
      }
    }else if(recordingRanges.contains(current)){
      isRecording = true
      recordingInfo = recordingRanges(current)
      recordingRanges.remove(current)
      recordingInfo._2.chkpt.addInputData(channel, payload)
    }
  }

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
      recordPayload(data)
      current += 1
      override def hasNext: Boolean = {
        if(outputBuffer.isEmpty && ofoMap.contains(current)){
          val currentPayload = ofoMap(current)
          outputBuffer.enqueue(currentPayload)
          ofoMap.remove(current)
          recordPayload(currentPayload)
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
