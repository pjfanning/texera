package edu.uci.ics.amber.engine.architecture.logreplay

import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  StateRestoreConfig,
  StepLoggingConfig
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object LogReplayUtils {

  private def getLogConf(
      workflowId: Int,
      executionId: String,
      logStorage: String,
      actorId: ActorVirtualIdentity
  ): StepLoggingConfig = {
    StepLoggingConfig(
      logStorage,
      s"${workflowId}/${executionId}/${actorId.name.replace("Worker:", "")}"
    )
  }

  def logConfGen(
      workflowId: Int,
      executionId: String,
      logStorage: String
  ): ActorVirtualIdentity => Option[StepLoggingConfig] = { x =>
    Some(getLogConf(workflowId, executionId, logStorage, x))
  }

  def recoveryConfGen(
      workflowId: Int,
      executionId: String,
      logStorage: String
  ): ActorVirtualIdentity => Option[StateRestoreConfig] = { x =>
    val logConf = getLogConf(workflowId, executionId, logStorage, x)
    Some(StateRestoreConfig(logConf, Long.MaxValue))
  }

  def replayConfGen(
      workflowId: Int,
      executionId: String,
      logStorage: String,
      replayMap: Map[ActorVirtualIdentity, Long]
  ): ActorVirtualIdentity => Option[StateRestoreConfig] = { x =>
    val logConf = getLogConf(workflowId, executionId, logStorage, x)
    Some(StateRestoreConfig(logConf, replayMap.getOrElse(x, Long.MaxValue)))
  }

}
