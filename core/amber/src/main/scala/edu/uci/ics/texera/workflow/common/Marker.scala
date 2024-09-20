package edu.uci.ics.texera.workflow.common

import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.tuple.schema.{AttributeType, Schema, Attribute}
import scala.collection.mutable

sealed trait Marker

final case class StartOfInputChannel() extends Marker
final case class EndOfInputChannel() extends Marker

final case class State(tuple: Option[Tuple] = None, passToAllDownstream: Boolean = false)
    extends Marker {
  val list: mutable.Map[String, (AttributeType, Any)] = mutable.HashMap()
  if (tuple.isEmpty) {
    add("passToAllDownstream", passToAllDownstream, AttributeType.BOOLEAN)
  } else {
    tuple.get.getSchema.getAttributes.foreach { attribute =>
      add(attribute.getName, tuple.get.getField(attribute.getName), attribute.getType)
    }
  }

  def add(key: String, value: Any, valueType: AttributeType): Unit =
    list.put(key, (valueType, value))

  def get(key: String): Any = list(key)._2

  def isPassToAllDownstream: Boolean = get("passToAllDownstream").asInstanceOf[Boolean]

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

  override def toString: String =
    list.map { case (key, (_, value)) => s"$key: $value" }.mkString(", ")
}
