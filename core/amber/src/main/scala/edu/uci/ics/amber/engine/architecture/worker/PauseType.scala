package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.virtualidentity.EmbeddedControlMessageIdentity

sealed trait PauseType

object UserPause extends PauseType

object BackpressurePause extends PauseType

object OperatorLogicPause extends PauseType

object SchedulerTimeSlotExpiredPause extends PauseType

case class EpochMarkerPause(id: EmbeddedControlMessageIdentity) extends PauseType
