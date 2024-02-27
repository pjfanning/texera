package edu.uci.ics.texera.workflow.common.tuple.schema



object Attribute{
  def apply[T](name:String):Attribute[T] = {
    new Attribute[T] {
      override val attributeName: String = name
    }
  }
}

trait Attribute[T] {
  type attributeType = T
  val attributeName:String
}
