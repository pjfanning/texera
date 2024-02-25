package edu.uci.ics.amber.engine.common.tuple.amber

trait TupleLike

trait MapTupleLike extends TupleLike {
  def fieldMappings: Map[String, Any]
}

trait SeqTupleLike extends TupleLike {
  def fieldArray: Seq[Any]
}

object TupleLike {

  def apply(fields: Map[String, Any]): MapTupleLike = {
    new MapTupleLike {
      override def fieldMappings: Map[String, Any] = fields
    }
  }
  def apply(fields: (String, Any)*): MapTupleLike = {
    new MapTupleLike {
      override def fieldMappings: Map[String, Any] = fields.toMap
    }
  }

  def apply(fields: Any*): SeqTupleLike = {
    new SeqTupleLike {
      override def fieldArray: Seq[Any] = fields
    }
  }
}
