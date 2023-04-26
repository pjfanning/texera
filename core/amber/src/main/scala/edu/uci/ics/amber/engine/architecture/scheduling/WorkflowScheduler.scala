package edu.uci.ics.amber.engine.architecture.scheduling

import akka.actor.{ActorContext, ActorRef, Address}
import com.twitter.util.Future
import com.typesafe.scalalogging.Logger
import edu.uci.ics.amber.engine.architecture.common.{VirtualIdentityUtils, WorkflowActorService}
import edu.uci.ics.amber.engine.common.ambermessage.ClientEvent.{FatalErrorToClient, WorkerAssignmentUpdate, WorkflowStatusUpdate}
import edu.uci.ics.amber.engine.architecture.controller.processing.ControlProcessor
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.LinkWorkersHandler.LinkWorkers
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow, WorkflowReplayConfig}
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.linksemantics.LinkStrategy
import edu.uci.ics.amber.engine.architecture.pythonworker.promisehandlers.InitializeOperatorLogicHandler.InitializeOperatorLogic
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
import edu.uci.ics.texera.workflow.common.workflow.PipelinedRegionPlan
import edu.uci.ics.texera.workflow.operators.udf.pythonV2.PythonUDFOpExecV2

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class WorkflowScheduler(
    logger: Logger,
    controlProcessor: ControlProcessor
) {

  private def getSchedulingPolicy = controlProcessor.schedulingPolicy

  private def getPipelinedRegionPlan = controlProcessor.pipelinedRegionPlan

  private def getAvailableNodes: Array[Address] = controlProcessor.getAvailableNodes()

  private def getActorService: WorkflowActorService = controlProcessor.actorService

  private def getAsyncRPCClient: AsyncRPCClient = controlProcessor.asyncRPCClient

  private def getWorkflow: Workflow = controlProcessor.workflow

  private def getExecution: WorkflowExecution = controlProcessor.execution

  private def getReplayConf: WorkflowReplayConfig = controlProcessor.replayPlan

  def startWorkflow(): Future[Seq[Unit]] = {
    doSchedulingWork(getSchedulingPolicy.startWorkflow(getPipelinedRegionPlan))
  }

  def onWorkerCompletion(workerId: ActorVirtualIdentity): Future[Seq[Unit]] = {
    doSchedulingWork(getSchedulingPolicy.onWorkerCompletion(getPipelinedRegionPlan, workerId))
  }

  def onLinkCompletion(linkId: LinkIdentity): Future[Seq[Unit]] = {
    doSchedulingWork(getSchedulingPolicy.onLinkCompletion(getPipelinedRegionPlan, linkId))
  }

  def onTimeSlotExpired(
      timeExpiredRegions: Set[PipelinedRegion]
  ): Future[Seq[Unit]] = {
    val nextRegions = getSchedulingPolicy.onTimeSlotExpired(getPipelinedRegionPlan)
    var regionsToPause: Set[PipelinedRegion] = Set()
    if (nextRegions.nonEmpty) {
      regionsToPause = timeExpiredRegions
    }

    doSchedulingWork(nextRegions)
      .flatMap(_ => {
        val pauseFutures = new ArrayBuffer[Future[Unit]]()
        regionsToPause.foreach(stoppingRegion => {
          getSchedulingPolicy.removeFromRunningRegion(Set(stoppingRegion).map(_.id))
          getWorkflow
            .getAllWorkersOfRegion(stoppingRegion)
            .foreach(wid => {
              pauseFutures.append(
                getAsyncRPCClient
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
      regions: Set[PipelinedRegion]
  ): Future[Seq[Unit]] = {
    if (regions.nonEmpty) {
      Future.collect(regions.toArray.map(r => scheduleRegion(r)))
    } else {
      Future(Seq())
    }
  }

  private def constructRegion(region: PipelinedRegion): Unit = {
    val builtOpsInRegion = new mutable.HashSet[LayerIdentity]()
    var frontier: Iterable[LayerIdentity] = getWorkflow.getSourcesOfRegion(region)
    while (frontier.nonEmpty) {
      frontier.foreach { (op: LayerIdentity) =>
        val prev: Array[(LayerIdentity, OpExecConfig)] =
          getWorkflow.physicalPlan
            .getUpstream(op)
            .filter(upStreamOp =>
              getExecution.builtOperators
                .contains(upStreamOp) && region.getOperators().contains(upStreamOp)
            )
            .map(upStreamOp => (upStreamOp, getWorkflow.getOperator(upStreamOp)))
            .toArray // Last layer of upstream operators in the same region.
        if (!getExecution.builtOperators.contains(op)) {
          buildOperator(op)
          getExecution.builtOperators.add(op)
        }
        builtOpsInRegion.add(op)
      }

      frontier = (region
        .getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions)
        .filter(opId => {
          !builtOpsInRegion.contains(opId) && getWorkflow.physicalPlan
            .getUpstream(opId)
            .filter(region.getOperators().contains)
            .forall(getExecution.builtOperators.contains)
        })
    }
  }

  private def buildOperator(
      operatorIdentity: LayerIdentity
  ): Unit = {
    val workerLayer = getWorkflow.getOperator(operatorIdentity)
    workerLayer.build(
      AddressInfo(getAvailableNodes, getActorService.self.path.address),
      getActorService,
      getExecution.getOperatorExecution(operatorIdentity),
      getReplayConf
    )
  }
  private def initializePythonOperators(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    val uninitializedPythonOperators = getWorkflow.getPythonOperators(
      allOperatorsInRegion.filter(opId => !getExecution.initializedPythonOperators.contains(opId))
    )
    Future
      .collect(
        // initialize python operator code
        getWorkflow
          .getPythonWorkerToOperatorExec(uninitializedPythonOperators)
          .map(p => {
            val workerID = p._1
            val pythonUDFOpExecConfig = p._2
            val pythonUDFOpExec = pythonUDFOpExecConfig
              .initIOperatorExecutor((0, pythonUDFOpExecConfig))
              .asInstanceOf[PythonUDFOpExecV2]
            getAsyncRPCClient.send(
              InitializeOperatorLogic(
                pythonUDFOpExec.getCode,
                getWorkflow
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
        uninitializedPythonOperators.foreach(opId => getExecution.initializedPythonOperators.add(opId))
      )
      .onFailure((err: Throwable) => {
        logger.error("Failure when sending Python UDF code", err)
        // report error to frontend
        getAsyncRPCClient.sendToClient(FatalErrorToClient(err))
      })
  }

  private def activateAllLinks(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    Future.collect(
      // activate all links
      getWorkflow.physicalPlan.linkStrategies.values
        .filter(link => {
          !getExecution.activatedLink.contains(link.id) &&
            allOperatorsInRegion.contains(link.from.id) &&
            allOperatorsInRegion.contains(link.to.id)
        })
        .map { link: LinkStrategy =>
          getAsyncRPCClient
            .send(LinkWorkers(link.id), CONTROLLER)
            .onSuccess(_ => getExecution.activatedLink.add(link.id))
        }
        .toSeq
    )
  }

  private def openAllOperators(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions
    val allNotOpenedOperators =
      allOperatorsInRegion.filter(opId => !getExecution.openedOperators.contains(opId))
    Future
      .collect(
        getWorkflow
          .getAllWorkersForOperators(allNotOpenedOperators)
          .map { workerID =>
            getAsyncRPCClient.send(OpenOperator(), workerID)
          }
          .toSeq
      )
      .onSuccess(_ => allNotOpenedOperators.foreach(opId => getExecution.openedOperators.add(opId)))
  }

  private def startRegion(region: PipelinedRegion): Future[Seq[Unit]] = {
    val allOperatorsInRegion =
      region.getOperators() ++ region.blockingDownstreamOperatorsInOtherRegions

    allOperatorsInRegion
      .filter(opId =>
        getExecution.getOperatorExecution(opId).getState == WorkflowAggregatedState.UNINITIALIZED
      )
      .foreach(opId => getExecution.getOperatorExecution(opId).setAllWorkerState(READY))
    getAsyncRPCClient.sendToClient(WorkflowStatusUpdate(getExecution.getWorkflowStatus))

    if (!getSchedulingPolicy.getRunningRegions().contains(region.id)) {
      Future
        .collect(
          getWorkflow
            .getAllWorkersForOperators(getWorkflow.getSourcesOfRegion(region))
            .map(worker =>
              getAsyncRPCClient
                .send(StartWorker(), worker)
                .map(ret =>
                  // update worker state
                  getExecution.getOperatorExecution(worker).getWorkerInfo(worker).state = ret
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
    getAsyncRPCClient.sendToClient(WorkflowStatusUpdate(getExecution.getWorkflowStatus))
    getAsyncRPCClient.sendToClient(
      WorkerAssignmentUpdate(
        getWorkflow.getOperatorToWorkers
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
        getExecution.constructingRegions.remove(region.getId())
        getSchedulingPolicy.addToRunningRegions(Set(region.id), getPipelinedRegionPlan, getActorService)
        getExecution.startedRegions.add(region.getId())
      })
  }

  private def resumeRegion(region: PipelinedRegion): Future[Unit] = {
    if (!getSchedulingPolicy.getRunningRegions().contains(region.id)) {
      Future
        .collect(
          getWorkflow
            .getAllWorkersOfRegion(region)
            .map(worker =>
              getAsyncRPCClient
                .send(SchedulerTimeSlotEvent(false), worker)
            )
            .toSeq
        )
        .map { _ =>
          getSchedulingPolicy.addToRunningRegions(Set(region.id), getPipelinedRegionPlan, getActorService)
        }
    } else {
      throw new WorkflowRuntimeException(
        s"Resume region called on an already running region: ${region.getOperators().mkString(",")}"
      )
    }

  }

  private def scheduleRegion(
      region: PipelinedRegion
  ): Future[Unit] = {
    if (getExecution.constructingRegions.contains(region.getId())) {
      return Future(())
    }
    if (!getExecution.startedRegions.contains(region.getId())) {
      getExecution.constructingRegions.add(region.getId())
      constructRegion(region)
      prepareAndStartRegion(region)
    } else {
      // region has already been constructed. Just needs to resume
      resumeRegion(region)
    }

  }

}
