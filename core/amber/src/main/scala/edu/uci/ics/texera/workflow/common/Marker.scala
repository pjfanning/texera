package edu.uci.ics.texera.workflow.common

import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema, Attribute}
import scala.collection.mutable

sealed trait Marker

final case class StartOfUpstream() extends Marker
final case class EndOfUpstream() extends Marker

final case class State() extends Marker {
  val list: mutable.Map[String, (AttributeType, Any)] = mutable.HashMap()

  def add(key: String, value: Any, valueType: AttributeType): Unit = {
    list.put(key, (valueType, value))
  }

  def get(key: String): Any = list(key)._2

  def apply(key: String): Any = get(key)

  def toTuple: Tuple =
    Tuple
      .builder(
        Schema
          .builder()
          .add(list.map {
            case (name, (attrType, _)) =>
              new Attribute(name, attrType)
          })
          .build()
      )
      .addSequentially(list.values.map(_._2).toArray)
      .build()

  def fromTuple(tuple: Tuple): State = {
    tuple.getSchema.getAttributes.foreach { attribute =>
      add(attribute.getName, tuple.getField(attribute.getName), attribute.getType)
    }
    this
  }

  def size: Int = list.size

  override def toString: String =
    list.map { case (key, (_, value)) => s"$key: $value" }.mkString(", ")
}