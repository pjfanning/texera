package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Address, Deploy}
import akka.remote.RemoteScope
import edu.uci.ics.amber.core.workflow.{PhysicalOp, PreferController, RoundRobinPrefnpuerence, GoToSpecificNode}
import edu.uci.ics.amber.engine.architecture.controller.execution.OperatorExecution
import edu.uci.ics.amber.engine.architecture.deploysemantics.AddressInfo
import edu.uci.ics.amber.engine.architecture.pythonworker.PythonWorkflowWorker
import edu.uci.ics.amber.engine.architecture.scheduling.config.OperatorConfig
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  FaultToleranceConfig,
  StateRestoreConfig,
  WorkerReplayInitialization
}
import edu.uci.ics.amber.util.VirtualIdentityUtils

object ExecutorDeployment {

  def createWorkers(
      op: PhysicalOp,
      controllerActorService: AkkaActorService,
      operatorExecution: OperatorExecution,
      operatorConfig: OperatorConfig,
      stateRestoreConfig: Option[StateRestoreConfig],
      replayLoggingConfig: Option[FaultToleranceConfig]
  ): Unit = {

    val addressInfo = AddressInfo(
      controllerActorService.getClusterNodeAddresses,
      controllerActorService.self.path.address
    )

    operatorConfig.workerConfigs.foreach(workerConfig => {
      val workerId = workerConfig.workerId
      val workerIndex = VirtualIdentityUtils.getWorkerIndex(workerId)
      val locationPreference = op.locationPreference.getOrElse(RoundRobinPreference)
      val preferredAddress: Address = locationPreference match {
        case PreferController =>
          addressInfo.controllerAddress
        case node: GoToSpecificNode =>
          println("----------")
          addressInfo.allAddresses.foreach(address => println("Address: " + address + "Address host: " + address.host.get))
          println("----------")
          addressInfo.allAddresses.find(addr => addr.host.get == node.nodeAddr).get
        case RoundRobinPreference =>
          println("++++++++++++")
          println("worker id: " + workerId)
          println("worker index: " + workerIndex)
          addressInfo.allAddresses.foreach(address => println("Address: " + address + "Address host: " + address.host.get))
          println("++++++++++++")
          assert(
            addressInfo.allAddresses.nonEmpty,
            "Execution failed to start, no available computation nodes"
          )
          addressInfo.allAddresses(workerIndex % addressInfo.allAddresses.length)
      }

      val workflowWorker = if (op.isPythonBased) {
        PythonWorkflowWorker.props(workerConfig)
      } else {
        WorkflowWorker.props(
          workerConfig,
          WorkerReplayInitialization(
            stateRestoreConfig,
            replayLoggingConfig
          )
        )
      }
      // Note: At this point, we don't know if the actor is fully initialized.
      // Thus, the ActorRef returned from `controllerActorService.actorOf` is ignored.
      controllerActorService.actorOf(
        workflowWorker.withDeploy(Deploy(scope = RemoteScope(preferredAddress)))
      )
      operatorExecution.initWorkerExecution(workerId)
    })
  }

}
