package edu.uci.ics.amber.engine.architecture.controller

import edu.uci.ics.amber.engine.architecture.common.AmberProcessor
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.ambermessage.WorkflowFIFOMessage
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class ControllerProcessor(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig,
    actorId: ActorVirtualIdentity,
    outputHandler: WorkflowFIFOMessage => Unit
) extends AmberProcessor(actorId, outputHandler) {

  val executionState = new ExecutionState(workflow)
  val workflowScheduler =
    new WorkflowScheduler(
      workflow.regionPlan.regions.toBuffer,
      executionState,
      controllerConfig,
      asyncRPCClient
    )

  private val initializer = new ControllerAsyncRPCHandlerInitializer(this)

  @transient var controller: Controller = _
  def setupController(controller: Controller): Unit = {
    this.controller = controller
  }
}
