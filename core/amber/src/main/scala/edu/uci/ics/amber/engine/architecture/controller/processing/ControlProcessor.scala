package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.{ActorContext, Address}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.scheduling.policies.SchedulingPolicy
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

class ControlProcessor(actorId:ActorVirtualIdentity, val config:ControllerConfig, determinantLogger:DeterminantLogger) extends AmberProcessor(actorId, determinantLogger) {

  @transient var workflow: Workflow = _

  @transient var getAvailableNodes: () => Array[Address] = _

  @transient var inputPort:NetworkInputPort = _

  @transient var pipelinedRegionPlan:PipelinedRegionPlan = _

  lazy val schedulingPolicy: SchedulingPolicy = SchedulingPolicy.createPolicy(Constants.schedulingPolicyName, workflow, execution, pipelinedRegionPlan)

  lazy val scheduler: WorkflowScheduler = new WorkflowScheduler(logger, this)

  lazy val execution = new WorkflowExecution(workflow)

  def initCP(controller:Controller): Unit = {
    init(controller)
    this.workflow = controller.workflow
    this.pipelinedRegionPlan = controller.pipelinedRegionPlan
    this.getAvailableNodes = controller.getAvailableNodes
    this.inputPort = controller.inputPort
    new ControllerAsyncRPCHandlerInitializer(this)
  }
}
