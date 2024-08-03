package edu.uci.ics.texera.workflow.common

import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType

import scala.collection.mutable

sealed trait Marker

final case class EndOfUpstream() extends Marker {
  override def toString: String = "EndOfUpstream"
}

final case class State() extends Marker {
  val list: mutable.Map[String, (AttributeType, Any)] = mutable.HashMap()

  def add(attributeName: String, attributeType: AttributeType, field: Any): Unit = {
    list.put(attributeName, (attributeType, field))
  }

  def get(key: String): Any = {
    list(key)._2
  }
}
