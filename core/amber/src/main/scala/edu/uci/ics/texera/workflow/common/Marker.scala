package edu.uci.ics.texera.workflow.common

import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema}

import scala.collection.mutable

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
