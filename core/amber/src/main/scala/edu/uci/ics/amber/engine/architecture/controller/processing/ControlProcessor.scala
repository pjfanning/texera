package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.{ActorContext, Address}
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.scheduling.policies.SchedulingPolicy
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan

class ControlProcessor(@transient var controller:Controller) extends AmberProcessor(controller) {

  def workflow: Workflow = controller.workflow

  def getAvailableNodes: () => Array[Address] = controller.getAvailableNodes

  def inputPort:NetworkInputPort = controller.inputPort

  def pipelinedRegionPlan:PipelinedRegionPlan = controller.pipelinedRegionPlan

  val schedulingPolicy: SchedulingPolicy = SchedulingPolicy.createPolicy(Constants.schedulingPolicyName, this)

  val scheduler: WorkflowScheduler = new WorkflowScheduler(logger, this)

  val execution = new WorkflowExecution(workflow)

  var replayPlan:WorkflowReplayConfig = WorkflowReplayConfig.empty

  private val initializer = new ControllerAsyncRPCHandlerInitializer(this)

  def initCP(controller:Controller): Unit = {
    initAP(controller)
    this.controller = controller
  }
}
