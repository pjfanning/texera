package edu.uci.ics.texera.workflow.common

import edu.uci.ics.amber.engine.common.tuple.amber.SpecialTupleLike

sealed trait Marker extends SpecialTupleLike

final case class StartOfUpstream() extends Marker {
  override def getFields: Array[Any] = Array("StartOfUpstream")
}

final case class EndOfUpstream() extends Marker {
  override def getFields: Array[Any] = Array("EndOfUpstream")
}

final case class EndOfIteration() extends Marker {
  override def getFields: Array[Any] = Array("EndOfIteration")
}

final case class State(value: String) extends Marker {
  override def getFields: Array[Any] = Array("State")
}

