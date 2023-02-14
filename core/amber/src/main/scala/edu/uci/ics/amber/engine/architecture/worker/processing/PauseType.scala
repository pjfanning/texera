package edu.uci.ics.amber.engine.architecture.worker.processing

object PauseType extends Enumeration {
  val UserPause, BackpressurePause, OperatorLogicPause, SchedulerTimeSlotExpiredPause = Value
}
