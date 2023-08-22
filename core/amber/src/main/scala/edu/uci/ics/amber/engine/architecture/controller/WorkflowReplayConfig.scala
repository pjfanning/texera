package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.worker.ReplayConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object WorkflowReplayConfig {
  def empty: WorkflowReplayConfig =
    WorkflowReplayConfig(Map().withDefaultValue(ReplayConfig(None, None, Array.empty)))
}

case class WorkflowReplayConfig(var confs: Map[ActorVirtualIdentity, ReplayConfig]){
  override def toString: String = {
    s"${confs.mkString("\n")}"
  }
}
