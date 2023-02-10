package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.worker.StateRestoreConfig
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object WorkflowStateRestoreConfig {
  def empty: WorkflowStateRestoreConfig =
    WorkflowStateRestoreConfig(StateRestoreConfig(None, None), Map.empty)
}

case class WorkflowStateRestoreConfig(
    controllerConf: StateRestoreConfig,
    workerConfs: Map[ActorVirtualIdentity, StateRestoreConfig]
)
