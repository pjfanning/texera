package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.{ActorContext, Address}
import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
import edu.uci.ics.amber.engine.architecture.controller.{Controller, ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.worker.processing.AmberProcessor
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class ControlProcessor(actorId:ActorVirtualIdentity, determinantLogger:DeterminantLogger) extends AmberProcessor(actorId, determinantLogger) {

  @transient var workflow: Workflow = _

  @transient var config:ControllerConfig = _

  @transient var scheduler: WorkflowScheduler = _

  @transient var getAvailableNodes: () => Array[Address] = _

  @transient var globalRecoveryManager: GlobalRecoveryManager = _

  @transient var inputPort:NetworkInputPort = _

  @transient var actorContext:ActorContext = _

  var isReplaying = false

  val processingHistory = new ProcessingHistory()

  lazy private[processing] val execution = new WorkflowExecution(workflow)

  def initCP(workflow: Workflow,
             controllerConfig: ControllerConfig,
             scheduler: WorkflowScheduler,
             globalRecoveryManager: GlobalRecoveryManager,
             getAvailableNodes: () => Array[Address],
             inputPort:NetworkInputPort,
             actorContext: ActorContext,
             logManager: LogManager): Unit = {
    this.workflow = workflow
    this.config = controllerConfig
    this.scheduler = scheduler
    this.scheduler.attachToExecution(execution, asyncRPCClient, globalRecoveryManager)
    this.getAvailableNodes = getAvailableNodes
    this.inputPort = inputPort
    this.actorContext = actorContext
    init(logManager)
    new ControllerAsyncRPCHandlerInitializer(this)
  }
}
