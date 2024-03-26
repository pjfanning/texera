package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.{FinalizeExecutor, FinalizePort}
import edu.uci.ics.amber.engine.architecture.worker.TupleProcessingManager.DPOutputIterator
import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, ChannelIdentity}
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable

object TupleProcessingManager{
  class DPOutputIterator extends Iterator[(TupleLike, Option[PortIdentity])] {
    val queue = new mutable.ListBuffer[(TupleLike, Option[PortIdentity])]
    @transient var outputIter: Iterator[(TupleLike, Option[PortIdentity])] = Iterator.empty

    def setTupleOutput(outputIter: Iterator[(TupleLike, Option[PortIdentity])]): Unit = {
      if (outputIter != null) {
        this.outputIter = outputIter
      } else {
        this.outputIter = Iterator.empty
      }
    }

    override def hasNext: Boolean = outputIter.hasNext || queue.nonEmpty

    override def next(): (TupleLike, Option[PortIdentity]) = {
      if (outputIter.hasNext) {
        outputIter.next()
      } else {
        queue.remove(0)
      }
    }

    def appendSpecialTupleToEnd(tuple: TupleLike): Unit = {
      queue.append((tuple, None))
    }
  }
}
class TupleProcessingManager( val actorId: ActorVirtualIdentity) {
  private var inputBatch: Array[Tuple] = _
  private var currentInputIdx: Int = -1
  var currentChannelId: ChannelIdentity = _
  val outputIterator: DPOutputIterator = new DPOutputIterator()
  def hasUnfinishedInput: Boolean = inputBatch != null && currentInputIdx + 1 < inputBatch.length

  def getNextTuple: Tuple = {
    currentInputIdx += 1
    inputBatch(currentInputIdx)
  }
  def getCurrentTuple: Tuple = {
    if (inputBatch == null) {
      null
    } else if (inputBatch.isEmpty) {
      null // TODO: create input exhausted
    } else {
      inputBatch(currentInputIdx)
    }
  }

  def initBatch(channelId: ChannelIdentity, batch: Array[Tuple]): Unit = {
    currentChannelId = channelId
    inputBatch = batch
    currentInputIdx = -1
  }


  def hasUnfinishedOutput: Boolean = outputIterator.hasNext

  def finalizeOutput(portIds: Set[PortIdentity]): Unit = {
    portIds
      .foreach(outputPortId =>
        outputIterator.appendSpecialTupleToEnd(FinalizePort(outputPortId, input = false))
      )
    outputIterator.appendSpecialTupleToEnd(FinalizeExecutor())
  }
}
