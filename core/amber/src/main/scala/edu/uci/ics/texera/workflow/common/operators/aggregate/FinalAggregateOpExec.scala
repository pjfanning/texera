package edu.uci.ics.texera.workflow.common.operators.aggregate

import edu.uci.ics.amber.engine.architecture.worker.PauseManager
import edu.uci.ics.amber.engine.common.InputExhausted
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.texera.workflow.common.ProgressiveUtils.{addInsertionFlag, addRetractionFlag}
import edu.uci.ics.texera.workflow.common.operators.OperatorExecutor
import edu.uci.ics.texera.workflow.common.operators.aggregate.PartialAggregateOpExec.internalAggObjKey
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, OperatorSchemaInfo, Schema}

import scala.collection.mutable

class FinalAggregateOpExec(
    val aggFuncs: List[DistributedAggregation[Object]],
    val groupByKeys: List[String],
    val operatorSchemaInfo: OperatorSchemaInfo
) extends OperatorExecutor {

  var groupByKeyAttributes: Array[Attribute] = _
  var outputSchema: Schema = operatorSchemaInfo.outputSchemas(0)

  // each value in partialObjectsPerKey has the same length as aggFuncs
  // partialObjectsPerKey(key)[i] corresponds to aggFuncs[i]
  private var partialObjectsPerKey = Map[List[Object], List[Object]]()

  // for incremental computation
  val UPDATE_INTERVAL_MS = 1000
  private var lastUpdatedTime: Long = 0
  private var counterSinceLastUpdate: Long = 0

  private var previousAggResults = Map[List[Object], List[Object]]()

  override def open(): Unit = {}
  override def close(): Unit = {}

  override def processTexeraTuple(
      tuple: Either[Tuple, InputExhausted],
      input: Int,
      pauseManager: PauseManager,
      asyncRPCClient: AsyncRPCClient
  ): Iterator[Tuple] = {
    if (aggFuncs.isEmpty) {
      throw new UnsupportedOperationException("Aggregation Functions Cannot be Empty")
    }
    tuple match {
      case Left(t) =>
        counterSinceLastUpdate += 1
        insertPartialInput(t)

        val condition: Boolean = System.currentTimeMillis - lastUpdatedTime > UPDATE_INTERVAL_MS
        if (condition)
          outputDiff()
        else
          Iterator()

      case Right(_) =>
        outputDiff()
    }
  }

  private def outputDiff(): Iterator[Tuple] = {
    val resultIterator = calculateDiff()

    counterSinceLastUpdate = 0
    lastUpdatedTime = System.currentTimeMillis
    previousAggResults = partialObjectsPerKey
    resultIterator
  }

  private def calculateDiff(): Iterator[Tuple] = {
    // find differences

    val retractions = new mutable.ArrayBuffer[Tuple]()
    val insertions = new mutable.ArrayBuffer[Tuple]()

    partialObjectsPerKey.keySet.foreach(k => {
      if (!previousAggResults.contains(k)) {
        val newFields = finalAggregate(k, partialObjectsPerKey(k))
        insertions.append(addInsertionFlag(newFields, outputSchema))
      } else if (previousAggResults(k) != partialObjectsPerKey(k)) {
        val prevFields = finalAggregate(k, previousAggResults(k))
        retractions.append(addRetractionFlag(prevFields, outputSchema))
        val newFields = finalAggregate(k, partialObjectsPerKey(k))
        insertions.append(addInsertionFlag(newFields, outputSchema))
      }
    })

    val results = retractions ++ insertions

    results.iterator
  }

  private def insertPartialInput(t: Tuple): Unit = {
    val key =
      if (groupByKeys == null || groupByKeys.isEmpty) List()
      else groupByKeys.map(k => t.getField[Object](k))

    val partialObjects =
      aggFuncs.indices.map(i => t.getField[Object](internalAggObjKey(i))).toList
    if (!partialObjectsPerKey.contains(key)) {
      partialObjectsPerKey += (key -> partialObjects)
    } else {
      val updatedPartialObjects = aggFuncs.indices
        .map(i => {
          val aggFunc = aggFuncs(i)
          val partial1 = partialObjectsPerKey(key)(i)
          val partial2 = partialObjects(i)
          aggFunc.merge(partial1, partial2)
        })
        .toList
      partialObjectsPerKey += (key -> updatedPartialObjects)
    }
  }

  private def finalAggregate(key: List[Object], value: List[Object]): Array[Object] = {
    val finalAggValues = aggFuncs.indices.map(i => aggFuncs(i).finalAgg(value(i)))
    (key ++ finalAggValues).toArray
  }

}
