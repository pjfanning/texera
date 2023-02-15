package edu.uci.ics.texera.workflow.common.operators.mlmodel

import akka.serialization.Serialization
import edu.uci.ics.amber.engine.architecture.checkpoint.{SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.worker.processing.PauseManager
import edu.uci.ics.amber.engine.common.{CheckpointSupport, InputExhausted}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.tuple.Tuple

import scala.collection.mutable.ListBuffer

abstract class MLModelOpExec() extends OperatorExecutor with Serializable with CheckpointSupport {

  var allData: ListBuffer[Tuple] = ListBuffer()

  var currentEpoch: Int = 0
  var nextMiniBatchStartIdx: Int = 0
  var miniBatch: Array[Tuple] = _
  var MINI_BATCH_SIZE: Int = 1000
  var nextOperation: String = "predict"
  var hasMoreIterations: Boolean = true
  var receivedAll = false
  var outputIterator:Iterator[Tuple] = Iterator()

  def getTotalEpochsCount: Int

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = {
    tuple match {
      case Left(t) =>
        allData += t
        Iterator()
      case Right(_) =>
        receivedAll = true
        getIterativeTrainingIterator
    }
  }

  def getIterativeTrainingIterator: Iterator[Tuple] = {
    new Iterator[Tuple] {
      override def hasNext(): Boolean = {
        hasMoreIterations || outputIterator.hasNext
      }

      override def next(): Tuple = {
        if(!hasMoreIterations){
          outputIterator.next()
        }else{
          performTrainingStep()
          null
        }
      }
    }
  }

  def performTrainingStep(): Unit ={
    if (nextOperation.equalsIgnoreCase("predict")) {
      // set the miniBatch
      if (nextMiniBatchStartIdx + MINI_BATCH_SIZE <= allData.size) {
        miniBatch =
          allData.slice(nextMiniBatchStartIdx, nextMiniBatchStartIdx + MINI_BATCH_SIZE).toArray
        nextMiniBatchStartIdx = nextMiniBatchStartIdx + MINI_BATCH_SIZE
      } else if (nextMiniBatchStartIdx < allData.size) {
        // remaining data is less than MINI_BATCH_SIZE
        miniBatch = allData.slice(nextMiniBatchStartIdx, allData.size).toArray
        nextMiniBatchStartIdx = 0
      } else {
        // will reach if no data present in allData
        hasMoreIterations = false
        outputIterator = outputPrediction(allData.toArray).toIterator
        return
      }

      predict(miniBatch)
      nextOperation = "calculateLossGradient"
    } else if (nextOperation.equalsIgnoreCase("calculateLossGradient")) {
      calculateLossGradient(miniBatch)
      nextOperation = "readjustWeight"
    } else if (nextOperation.equalsIgnoreCase("readjustWeight")) {
      readjustWeight()
      nextOperation = "predict"

      if (nextMiniBatchStartIdx == 0) {
        // current epoch is over
        currentEpoch += 1
      }
      if (currentEpoch == getTotalEpochsCount) {
        hasMoreIterations = false
        outputIterator = outputPrediction(allData.toArray).toIterator
      }
    }
  }

  def predict(miniBatch: Array[Tuple]): Unit
  def calculateLossGradient(miniBatch: Array[Tuple]): Unit
  def readjustWeight(): Unit
  def outputPrediction(allData: Array[Tuple]): Array[Tuple]

  override def serializeState(currentIteratorState: Iterator[(ITuple, Option[Int])], checkpoint: SavedCheckpoint, serializer: Serialization): Unit = {
    if(receivedAll){
      checkpoint.save("currentEpoch", SerializedState.fromObject(Int.box(currentEpoch), serializer))
      checkpoint.save("nextMiniBatchStartIdx", SerializedState.fromObject(Int.box(nextMiniBatchStartIdx), serializer))
      checkpoint.save("miniBatch", SerializedState.fromObject(miniBatch, serializer))
      checkpoint.save("hasMoreIterations", SerializedState.fromObject(Boolean.box(hasMoreIterations), serializer))
      checkpoint.save("outputIterator", SerializedState.fromObject(outputIterator, serializer))
    }
    checkpoint.save("allData", SerializedState.fromObject(allData, serializer))
    checkpoint.save("receivedAll", SerializedState.fromObject(Boolean.box(receivedAll), serializer))
  }

  override def deserializeState(checkpoint: SavedCheckpoint, deserializer: Serialization): Iterator[(ITuple, Option[Int])] = {
    receivedAll = checkpoint.load("receivedALl").toObject(deserializer)
    allData = checkpoint.load("allData").toObject(deserializer)
    if(receivedAll){
      currentEpoch = checkpoint.load("currentEpoch").toObject(deserializer)
      nextMiniBatchStartIdx = checkpoint.load("nextMiniBatchStartIdx").toObject(deserializer)
      miniBatch = checkpoint.load("miniBatch").toObject(deserializer)
      hasMoreIterations = checkpoint.load("hasMoreIterations").toObject(deserializer)
      outputIterator = checkpoint.load("outputIterator").toObject(deserializer)
      getIterativeTrainingIterator.map(x => (x, None))
    }else{
      Iterator()
    }
  }

}
