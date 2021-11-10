package edu.uci.ics.texera.workflow.operators.sink.managed

import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.LinkIdentity
import edu.uci.ics.amber.engine.common.{ISinkOperatorExecutor, InputExhausted}
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode._
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo
import edu.uci.ics.texera.workflow.common.{IncrementalOutputMode, ProgressiveUtils}

import scala.collection.mutable

class ProgressiveSinkOpExec(
    val operatorSchemaInfo: OperatorSchemaInfo,
    val outputMode: IncrementalOutputMode,
    val chartType: Option[String]
) extends ISinkOperatorExecutor {

  val results: mutable.ListBuffer[Tuple] = mutable.ListBuffer()

  def getResultTuples: List[ITuple] = {
    outputMode match {
      case SET_SNAPSHOT =>
        results.toList
      case SET_DELTA =>
        val ret = results.toList
        // clear the delta result buffer after every progressive output
        results.clear()
        ret
    }
  }

  def getOutputMode: IncrementalOutputMode = this.outputMode

  override def open(): Unit = {}

  override def close(): Unit = {}

  override def consume(
      tuple: Either[ITuple, InputExhausted],
      input: LinkIdentity
  ): Unit = {
    tuple match {
      case Left(t) =>
        outputMode match {
          case SET_SNAPSHOT =>
            updateSetSnapshot(t.asInstanceOf[Tuple])
          case SET_DELTA =>
            results += t.asInstanceOf[Tuple]
        }
      case Right(_) => // skip
    }
  }

  private def updateSetSnapshot(deltaUpdate: Tuple): Unit = {
    val (isInsertion, tupleValue) =
      ProgressiveUtils.getTupleFlagAndValue(deltaUpdate, operatorSchemaInfo)
    if (isInsertion) {
      results += tupleValue
    } else {
      results -= tupleValue
    }
  }

}
