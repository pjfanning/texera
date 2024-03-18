package edu.uci.ics.texera.workflow.common

sealed trait Marker {}

final case class EndOfUpstream() extends Marker
final case class EndOfIteration() extends Marker

final case class State() extends Marker
