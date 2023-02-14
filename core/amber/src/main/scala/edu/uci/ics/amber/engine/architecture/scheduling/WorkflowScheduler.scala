package edu.uci.ics.amber.engine.architecture.scheduling

import akka.actor.{ActorContext, Address}
import com.twitter.util.Future
import com.typesafe.scalalogging.Logger
import edu.uci.ics.amber.engine.architecture.common.VirtualIdentityUtils
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{WorkerAssignmentUpdate, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.LinkWorkersHandler.LinkWorkers
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.linksemantics.LinkStrategy
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkSenderActorRef
import edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers.InitializeOperatorLogicHandler.InitializeOperatorLogic
import edu.uci.ics.amber.engine.architecture.scheduling.policies.SchedulingPolicy
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.OpenOperatorHandler.OpenOperator
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.SchedulerTimeSlotEventHandler.SchedulerTimeSlotEvent
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.StartHandler.StartWorker
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.READY
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LayerIdentity, LinkIdentity}
import edu.uci.ics.amber.engine.common.{Constants, ISourceOperatorExecutor}
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.workflow.operators.udf.pythonV2.PythonUDFOpExecV2

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class WorkflowScheduler(
    networkCommunicationActor: NetworkSenderActorRef,
    ctx: ActorContext,
    logger: Logger,
    workflow: Workflow,
    controllerConf: ControllerConfig
) {
  val schedulingPolicy: SchedulingPolicy =
    SchedulingPolicy.createPolicy(Constants.schedulingPolicyName, workflow, ctx)

  @transient
  private var asyncRPCClient: AsyncRPCClient = _
  @transient
  private var execution: WorkflowExecution = _

  def attachToExecution(execution: WorkflowExecution, asyncRPCClient: AsyncRPCClient): Unit = {
    this.execution = execution
    this.asyncRPCClient = asyncRPCClient
    schedulingPolicy.attachToExecution(execution)
  }

  def startWorkflow(availableNodes: Array[Address]): Future[Seq[Unit]] = {
    doSchedulingWork(schedulingPolicy.startWorkflow(), availableNodes)
  }

  def onWorkerCompletion(
      workerId: ActorVirtualIdentity,
      availableNodes: Array[Address]
  ): Future[Seq[Unit]] = {
    doSchedulingWork(schedulingPolicy.onWorkerCompletion(workerId), availableNodes)
  }

  def onLinkCompletion(linkId: LinkIdentity, availableNodes: Array[Address]): Future[Seq[Unit]] = {
    doSchedulingWork(schedulingPolicy.onLinkCompletion(linkId), availableNodes)
  }

  def onTimeSlotExpired(
      timeExpiredRegions: Set[PipelinedRegion],
      availableNodes: Array[Address]
  ): Future[Seq[Unit]] = {
    val nextRegions = schedulingPolicy.onTimeSlotExpired()
    var regionsToPause: Set[PipelinedRegion] = Set()
    if (nextRegions.nonEmpty) {
      regionsToPause = timeExpiredRegions
    }

    doSchedulingWork(nextRegions, availableNodes)
      .flatMap(_ => {
        val pauseFutures = new ArrayBuffer[Future[Unit]]()
        regionsToPause.foreach(stoppingRegion => {
          schedulingPolicy.removeFromRunningRegion(Set(stoppingRegion).map(_.id))
          workflow
            .getAllWorkersOfRegion(stoppingRegion)
            .foreach(wid => {
              pauseFutures.append(
                asyncRPCClient
                  .send(SchedulerTimeSlotEvent(true), wid)
              )
            })
        })
        Future.collect(pauseFutures)
      })
      .map(_ => {
        Seq()
      })
  }

  private def doSchedulingWork(
      regions: Set[PipelinedRegion],
      availableNodes: Array[Address]
  ): Future[Seq[Unit]] = {
    if (regions.nonEmpty) {
      Future.collect(regions.toArray.map(r => scheduleRegion(r, availableNodes)))
    } else {
      Future(Seq())
    }
  }

  private def constructRegion(region: PipelinedRegion, availableNodes: Array[Address]): Unit = {
    val builtOpsInRegion = new mutable.HashSet[LayerIdentity]()
    var frontier: Iterable[LayerIdentity] = workflow.getSourcesOfRegion(region)
    while (frontier.nonEmpty) {
      frontier.foreach { (op: LayerIdentity) =>
        val prev: Array[(LayerIdentity, OpExecConfig)] =
          workflow.physicalPlan
            .getUpstream(op)
            .filter(upStreamOp =>
              execution.builtOperators
                .contains(upStreamOp) && region.getOperators().contains(upStreamOp)
            )
            .map(upStreamOp => (upStreamOp, workflow.getOperator(upStreamOp)))
            .toArray // Last layer of upstream operators in the same region.
        if (!execution.builtOperators.contains(op)) {
          buildOperator(op, controllerConf, availableNodes)
          execution.builtOperators.add(op)
        }
        builtOpsInRegion.add(op)
      }

      frontier = (region
        .getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions)
        .filter(opId => {
          !builtOpsInRegion.contains(opId) && workflow.physicalPlan
            .getUpstream(opId)
            .filter(region.getOperators().contains)
            .forall(execution.builtOperators.contains)
        })
    }
  }

  private def buildOperator(
      operatorIdentity: LayerIdentity,
      controllerConf: ControllerConfig,
      availableNodes: Array[Address]
  ): Unit = {
    val workerLayer = workflow.getOperator(operatorIdentity)
    workerLayer.build(
      AddressInfo(availableNodes, ctx.self.path.address),
      networkCommunicationActor,
      ctx,
      execution.getOperatorExecution(operatorIdentity),
      controllerConf
    )
  }
  private def initializePythonOperators(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    val uninitializedPythonOperators = workflow.getPythonOperators(
      allOperatorsInRegion.filter(opId => !execution.initializedPythonOperators.contains(opId))
    )
    Future
      .collect(
        // initialize python operator code
        workflow
          .getPythonWorkerToOperatorExec(uninitializedPythonOperators)
          .map(p => {
            val workerID = p._1
            val pythonUDFOpExecConfig = p._2
            val pythonUDFOpExec = pythonUDFOpExecConfig
              .initIOperatorExecutor((0, pythonUDFOpExecConfig))
              .asInstanceOf[PythonUDFOpExecV2]
            asyncRPCClient.send(
              InitializeOperatorLogic(
                pythonUDFOpExec.getCode,
                workflow
                  .getInlinksIdsToWorkerLayer(VirtualIdentityUtils.getOperator(workerID))
                  .toArray,
                pythonUDFOpExec.isInstanceOf[ISourceOperatorExecutor],
                pythonUDFOpExec.getOutputSchema
              ),
              workerID
            )
          })
          .toSeq
      )
      .onSuccess(_ =>
        uninitializedPythonOperators.foreach(opId => execution.initializedPythonOperators.add(opId))
      )
      .onFailure((err: Throwable) => {
        logger.error("Failure when sending Python UDF code", err)
        // report error to frontend
        asyncRPCClient.sendToClient(FatalError(err))
      })
  }

  private def activateAllLinks(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    Future.collect(
      // activate all links
      workflow.physicalPlan.linkStrategies.values
        .filter(link => {
          !execution.activatedLink.contains(link.id) &&
            allOperatorsInRegion.contains(link.from.id) &&
            allOperatorsInRegion.contains(link.to.id)
        })
        .map { link: LinkStrategy =>
          asyncRPCClient
            .send(LinkWorkers(link.id), CONTROLLER)
            .onSuccess(_ => execution.activatedLink.add(link.id))
        }
        .toSeq
    )
  }

  private def openAllOperators(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    val allNotOpenedOperators =
      allOperatorsInRegion.filter(opId => !execution.openedOperators.contains(opId))
    Future
      .collect(
        workflow
          .getAllWorkersForOperators(allNotOpenedOperators)
          .map { workerID =>
            asyncRPCClient.send(OpenOperator(), workerID)
          }
          .toSeq
      )
      .onSuccess(_ => allNotOpenedOperators.foreach(opId => execution.openedOperators.add(opId)))
  }

  private def startRegion(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions

    allOperatorsInRegion
      .filter(opId =>
        execution.getOperatorExecution(opId).getState == WorkflowAggregatedState.UNINITIALIZED
      )
      .foreach(opId => execution.getOperatorExecution(opId).setAllWorkerState(READY))
    asyncRPCClient.sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))

    if (!schedulingPolicy.getRunningRegions().contains(region.id)) {
      Future
        .collect(
          workflow
            .getAllWorkersForOperators(workflow.getSourcesOfRegion(region))
            .map(worker =>
              asyncRPCClient
                .send(StartWorker(), worker)
                .map(ret =>
                  // update worker state
                  execution.getOperatorExecution(worker).getWorkerInfo(worker).state = ret
                )
            )
        )
    } else {
      throw new WorkflowRuntimeException(
        s"Start region called on an already running region: ${region.getOperators().mkString(",")}"
      )
    }
  }

  private def prepareAndStartRegion(region: PipelinedRegion): Future[Unit] = {
    asyncRPCClient.sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))
    asyncRPCClient.sendToClient(
      WorkerAssignmentUpdate(
        workflow.getOperatorToWorkers
          .map({
            case (opId: LayerIdentity, workerIds: Seq[ActorVirtualIdentity]) =>
              opId.operator -> workerIds.map(_.name)
          })
          .toMap
      )
    )
    Future(())
      .flatMap(_ => initializePythonOperators(region))
      .flatMap(_ => activateAllLinks(region))
      .flatMap(_ => openAllOperators(region))
      .flatMap(_ => startRegion(region))
      .map(_ => {
        execution.constructingRegions.remove(region.getId())
        schedulingPolicy.addToRunningRegions(Set(region.id))
        execution.startedRegions.add(region.getId())
      })
  }

  private def resumeRegion(region: PipelinedRegion): Future[Unit] = {
    if (!schedulingPolicy.getRunningRegions().contains(region.id)) {
      Future
        .collect(
          workflow
            .getAllWorkersOfRegion(region)
            .map(worker =>
              asyncRPCClient
                .send(SchedulerTimeSlotEvent(false), worker)
            )
            .toSeq
        )
        .map { _ =>
          schedulingPolicy.addToRunningRegions(Set(region.id))
        }
    } else {
      throw new WorkflowRuntimeException(
        s"Resume region called on an already running region: ${region.getOperators().mkString(",")}"
      )
    }

  }

  private def scheduleRegion(
      region: PipelinedRegion,
      availableNodes: Array[Address]
  ): Future[Unit] = {
    if (execution.constructingRegions.contains(region.getId())) {
      return Future(())
    }
    if (!execution.startedRegions.contains(region.getId())) {
      execution.constructingRegions.add(region.getId())
      constructRegion(region, availableNodes)
      prepareAndStartRegion(region)
    } else {
      // region has already been constructed. Just needs to resume
      resumeRegion(region)
    }

  }

}
