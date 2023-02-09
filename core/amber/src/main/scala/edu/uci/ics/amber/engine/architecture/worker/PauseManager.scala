package edu.uci.ics.amber.engine.architecture.worker

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.collection.mutable

class PauseManager(dataProcessor: DataProcessor) {

  private val pauseInvocations = new mutable.HashMap[PauseType.Value, Boolean]()

  def pause(pauseType: PauseType.Value, inputs: Option[List[ActorVirtualIdentity]] = None): Unit = {

  }

  def resume(pauseType: PauseType.Value, inputs: Option[List[ActorVirtualIdentity]] = None): Unit = {

  }

  def recordRequest(pauseType: PauseType.Value, enablePause: Boolean): Unit = {
    pauseInvocations(pauseType) = enablePause
  }

  def getPauseStatusByType(pauseType: PauseType.Value): Boolean =
    pauseInvocations.getOrElse(pauseType, false)

  def isPaused(): Boolean = {
    pauseInvocations.values.exists(isPaused => isPaused)
  }

}
