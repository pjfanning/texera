package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.{FinalizeExecutor, FinalizePort}
import edu.uci.ics.amber.engine.architecture.worker.TupleProcessingManager.DPOutputIterator
import edu.uci.ics.amber.engine.common.tuple.amber.TupleLike
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.workflow.PortIdentity
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable

object TupleProcessingManager {
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
class TupleProcessingManager(val actorId: ActorVirtualIdentity) {
  // Holds the current batch of tuples for processing.
  private var inputBatch: Option[Array[Tuple]] = None

  // Tracks the index of the currently processed tuple in the input batch.
  private var currentInputIdx: Int = -1

  // Identifier for the current port being processed.
  private var currentPortId: Option[PortIdentity] = None

  // Checks if there are more tuples in the batch to process.
  def hasUnfinishedInput: Boolean =
    inputBatch match {
      case Some(batch) => currentInputIdx < batch.length - 1
      case None        => false
    }

  // Advances to the next tuple in the batch and returns it.
  def next: (Tuple, PortIdentity) = {
    if (hasUnfinishedInput) {
      currentInputIdx += 1
      (inputBatch.get(currentInputIdx), currentPortId.get)
    } else {
      throw new NoSuchElementException()
    }
  }

  // Retrieves the current tuple without advancing the index.
  def getCurrentTuple: Option[Tuple] = {
    inputBatch match {
      case Some(batch)
          if batch.nonEmpty && currentInputIdx >= 0 && currentInputIdx < batch.length =>
        Some(batch(currentInputIdx))
      case _ =>
        None // Input batch is not initialized, empty, or index is out of bounds.
    }
  }

  // Set the batch of tuples for processing and resets the index.
  def setBatch(portId: PortIdentity, batch: Array[Tuple]): Unit = {
    currentPortId = Some(portId)
    inputBatch = Some(batch)
    currentInputIdx = -1
  }

  val outputIterator: DPOutputIterator = new DPOutputIterator()
  def hasUnfinishedOutput: Boolean = outputIterator.hasNext

  def finalizeOutput(portIds: Set[PortIdentity]): Unit = {
    portIds
      .foreach(outputPortId =>
        outputIterator.appendSpecialTupleToEnd(FinalizePort(outputPortId, input = false))
      )
    outputIterator.appendSpecialTupleToEnd(FinalizeExecutor())
  }
}
