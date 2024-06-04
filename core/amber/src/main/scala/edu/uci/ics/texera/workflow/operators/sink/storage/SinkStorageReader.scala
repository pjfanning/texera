package edu.uci.ics.texera.workflow.operators.sink.storage

import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema

trait SinkStorageReader {
  var schema: Schema = _
  def getSchema: Schema

  def setSchema(schema: Schema): Unit

  def getAll: Iterable[Tuple]

  def getRange(from: Int, to: Int): Iterable[Tuple]

  def getAllAfter(offset: Int): Iterable[Tuple]

  def getCount: Long

  def getStorageWriter: SinkStorageWriter

  def clear(): Unit

  def getAllNumericFields(): Iterable[String] = {
    Array[String]()
  }

  def getStats(fieldName: String): Option[(Any, Any, Any)] = {
    None
  }

  def getNumericColStats(fields: Iterable[String]): Map[String, Map[String, Float]] = {
    Map[String, Map[String, Float]]()
  }
}