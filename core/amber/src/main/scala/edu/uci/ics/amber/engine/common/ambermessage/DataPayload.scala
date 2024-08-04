package edu.uci.ics.amber.engine.common.ambermessage

import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema}

import scala.collection.mutable

sealed trait DataPayload extends WorkflowFIFOMessagePayload {}

final case class DataFrame(frame: Array[Tuple]) extends DataPayload {
  val inMemSize: Long = {
    frame.map(_.inMemSize).sum
  }

  override def equals(obj: Any): Boolean = {
    if (!obj.isInstanceOf[DataFrame]) return false
    val other = obj.asInstanceOf[DataFrame]
    if (other eq null) return false
    if (frame.length != other.frame.length) {
      return false
    }
    var i = 0
    while (i < frame.length) {
      if (frame(i) != other.frame(i)) {
        return false
      }
      i += 1
    }
    true
  }
}

final case class MarkerFrame(frame: Marker) extends DataPayload

sealed trait Marker

final case class EndOfUpstream() extends Marker

final case class State() extends Marker {
  val list: mutable.Map[String, (AttributeType, Any)] = mutable.HashMap()

  def add(attributeName: String, attributeType: AttributeType, field: Any): Unit = {
    list.put(attributeName, (attributeType, field))
  }

  def get(key: String): Any = {
    list(key)._2
  }

  def toTuple: Tuple = {
    val schemaBuilder = Schema.builder()
    for ((name, (attributeType, _)) <- list) {
      schemaBuilder.add(name, attributeType)
    }
    val tupleBuilder = Tuple.builder(schemaBuilder.build())
    tupleBuilder.addSequentially(list.values.map(_._2).toArray)
    tupleBuilder.build()
  }
}
