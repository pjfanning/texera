package edu.uci.ics.amber.engine.architecture.messaginglayer

import edu.uci.ics.amber.engine.architecture.messaginglayer.AmberFIFOChannel.skipFaultTolerance
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, DataPayload, FIFOMarker, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipFaultTolerance

import scala.collection.mutable


object AmberFIFOChannel{

  def skipFaultTolerance(payload:WorkflowFIFOMessagePayload): Boolean ={
    payload match {
      case control: ControlInvocation => control.isInstanceOf[SkipFaultTolerance]
      case ret: ReturnInvocation => ret.skipFaultTolerance
      case _: DataPayload => false
      case FIFOMarker(id, markerCounts, controlIdMap, sharedCommand) => sharedCommand.isInstanceOf[SkipFaultTolerance]
      case _ => false
    }
  }
}


/* The abstracted FIFO/exactly-once logic */
class AmberFIFOChannel {

  val ofoMap = new mutable.HashMap[Long, WorkflowFIFOMessagePayload]
  var current = 0L
  var specialPayloads = new mutable.HashMap[Long, mutable.ArrayBuffer[WorkflowFIFOMessagePayload]]

  def setCurrent(value: Long): Unit = {
    current = value
  }

  def acceptMessage(seq:Long, payload:WorkflowFIFOMessagePayload):Iterator[WorkflowFIFOMessagePayload] = {
    if(skipFaultTolerance(payload)){
      addSpecialPayload(seq, payload)
    }else{
      if (isDuplicated(seq)) {
        Iterator.empty
      } else if (isAhead(seq)) {
        stash(seq, payload)
        Iterator.empty
      } else{
        enforceFIFO(payload)
      }
    }
  }

  def isDuplicated(sequenceNumber: Long): Boolean =
    sequenceNumber < current || ofoMap.contains(sequenceNumber)

  def isAhead(sequenceNumber: Long): Boolean = sequenceNumber > current

  private def stash(sequenceNumber: Long, data: WorkflowFIFOMessagePayload): Unit = {
    ofoMap(sequenceNumber) = data
  }

  private def addSpecialPayload(seq:Long, payload:WorkflowFIFOMessagePayload): Iterator[WorkflowFIFOMessagePayload] ={
    if(current == seq){
      Iterator(payload)
    }else{
      specialPayloads.getOrElseUpdate(seq, mutable.ArrayBuffer()).append(payload)
      Iterator.empty
    }
  }

  private def handleMarker(): Seq[WorkflowFIFOMessagePayload] ={
    if(specialPayloads.contains(current)){
      // do something
      val res = specialPayloads(current)
      specialPayloads.remove(current)
      Seq(res:_*)
    }else{
      Seq.empty
    }
  }

  private def enforceFIFO(data: WorkflowFIFOMessagePayload): Iterator[WorkflowFIFOMessagePayload] = {
    val resultIterator = new Iterator[WorkflowFIFOMessagePayload]{
      private val outputBuffer = mutable.Queue[WorkflowFIFOMessagePayload](data)
      outputBuffer.enqueue(handleMarker():_*)
      current += 1
      override def hasNext: Boolean = {
        if(outputBuffer.isEmpty && ofoMap.contains(current)){
          outputBuffer.enqueue(ofoMap(current))
          ofoMap.remove(current)
          outputBuffer.enqueue(handleMarker():_*)
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
