package edu.uci.ics.texera.web.service

import java.util.concurrent.ConcurrentHashMap
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerConfig
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  WorkerReplayLoggingConfig,
  WorkerStateRestoreConfig
}
import edu.uci.ics.amber.engine.common.AmberConfig

import scala.collection.JavaConverters._
import edu.uci.ics.texera.web.model.websocket.event.TexeraWebSocketEvent
import edu.uci.ics.texera.web.{SubscriptionManager, WorkflowLifecycleManager}
import edu.uci.ics.texera.web.model.websocket.request.WorkflowExecuteRequest
import edu.uci.ics.texera.web.resource.WorkflowWebsocketResource
import edu.uci.ics.texera.web.service.WorkflowService.mkWorkflowStateId
import edu.uci.ics.texera.web.storage.WorkflowStateStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.COMPLETED
import edu.uci.ics.texera.workflow.common.WorkflowContext
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.workflow.LogicalPlan
import io.reactivex.rxjava3.disposables.{CompositeDisposable, Disposable}
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.jooq.types.UInteger
import play.api.libs.json.Json

import java.net.URI

object WorkflowService {
  private val wIdToWorkflowState = new ConcurrentHashMap[String, WorkflowService]()
  val cleanUpDeadlineInSeconds: Int = AmberConfig.executionStateCleanUpInSecs

  def getAllWorkflowService: Iterable[WorkflowService] = wIdToWorkflowState.values().asScala

  def mkWorkflowStateId(wId: Int): String = {
    wId.toString
  }
  def getOrCreate(
      wId: Int,
      cleanupTimeout: Int = cleanUpDeadlineInSeconds
  ): WorkflowService = {
    wIdToWorkflowState.compute(
      mkWorkflowStateId(wId),
      (_, v) => {
        if (v == null) {
          new WorkflowService(wId, cleanupTimeout)
        } else {
          v
        }
      }
    )
  }
}

class WorkflowService(
    val wId: Int,
    cleanUpTimeout: Int
) extends SubscriptionManager
    with LazyLogging {
  // state across execution:
  var opResultStorage: OpResultStorage = new OpResultStorage()
  private val errorSubject = BehaviorSubject.create[TexeraWebSocketEvent]().toSerialized
  val stateStore = new WorkflowStateStore()
  var jobService: BehaviorSubject[WorkflowJobService] = BehaviorSubject.create()

  val resultService: JobResultService =
    new JobResultService(opResultStorage, stateStore)
  val exportService: ResultExportService =
    new ResultExportService(opResultStorage, UInteger.valueOf(wId))
  val lifeCycleManager: WorkflowLifecycleManager = new WorkflowLifecycleManager(
    s"wid=$wId",
    cleanUpTimeout,
    () => {
      opResultStorage.close()
      WorkflowService.wIdToWorkflowState.remove(mkWorkflowStateId(wId))
      if (jobService.getValue != null) {
        // shutdown client
        jobService.getValue.client.shutdown()
      }
      unsubscribeAll()
    }
  )

  var lastCompletedLogicalPlan: Option[LogicalPlan] = Option.empty

  jobService.subscribe { job: WorkflowJobService =>
    {
      job.jobStateStore.jobMetadataStore.registerDiffHandler { (oldState, newState) =>
        {
          if (oldState.state != COMPLETED && newState.state == COMPLETED) {
            lastCompletedLogicalPlan = Option.apply(job.workflow.originalLogicalPlan)
          }
          Iterable.empty
        }
      }
    }
  }

  def connect(onNext: TexeraWebSocketEvent => Unit): Disposable = {
    lifeCycleManager.increaseUserCount()
    val subscriptions = stateStore.getAllStores
      .map(_.getWebsocketEventObservable)
      .map(evtPub =>
        evtPub.subscribe { evts: Iterable[TexeraWebSocketEvent] => evts.foreach(onNext) }
      )
      .toSeq
    val errorSubscription = errorSubject.subscribe { evt: TexeraWebSocketEvent => onNext(evt) }
    new CompositeDisposable(subscriptions :+ errorSubscription: _*)
  }

  def connectToJob(onNext: TexeraWebSocketEvent => Unit): Disposable = {
    var localDisposable = Disposable.empty()
    jobService.subscribe { job: WorkflowJobService =>
      localDisposable.dispose()
      val subscriptions = job.jobStateStore.getAllStores
        .map(_.getWebsocketEventObservable)
        .map(evtPub =>
          evtPub.subscribe { evts: Iterable[TexeraWebSocketEvent] => evts.foreach(onNext) }
        )
        .toSeq
      localDisposable = new CompositeDisposable(subscriptions: _*)
    }
  }

  def disconnect(): Unit = {
    lifeCycleManager.decreaseUserCount(
      Option(jobService.getValue).map(_.jobStateStore.jobMetadataStore.getState.state)
    )
  }

  private[this] def createWorkflowContext(
      uidOpt: Option[UInteger]
  ): WorkflowContext = {
    val jobID: String = String.valueOf(WorkflowWebsocketResource.nextExecutionID.incrementAndGet)
    new WorkflowContext(jobID, uidOpt, UInteger.valueOf(wId))
  }

  def initJobService(req: WorkflowExecuteRequest, uidOpt: Option[UInteger]): Unit = {
    if (jobService.getValue != null) {
      //unsubscribe all
      jobService.getValue.unsubscribeAll()
    }
    val workflowContext: WorkflowContext = createWorkflowContext(uidOpt)
    var controllerConf = ControllerConfig.default

    workflowContext.executionId = ExecutionsMetadataPersistService.insertNewExecution(
      workflowContext.wid,
      workflowContext.userId,
      req.executionName,
      convertToJson(req.engineVersion)
    )

    if (AmberConfig.isUserSystemEnabled) {
      // enable only if we have mysql
      if (AmberConfig.faultToleranceLogRootFolder.isDefined) {
        val writeLocation = AmberConfig.faultToleranceLogRootFolder.get.resolve(
          workflowContext.wid + "/" + workflowContext.executionId
        )
        ExecutionsMetadataPersistService.tryUpdateExistingExecution(workflowContext.executionId) {
          execution => execution.setLogLocation(writeLocation.toString)
        }
        controllerConf = controllerConf.copy(workerLoggingConfMapping = { _ =>
          Some(WorkerReplayLoggingConfig(writeTo = writeLocation))
        })
      }
      if (req.replayFromExecution.isDefined) {
        val replayInfo = req.replayFromExecution.get
        ExecutionsMetadataPersistService
          .tryGetExistingExecution(replayInfo.eid)
          .foreach { execution =>
            val readLocation = new URI(execution.getLogLocation)
            controllerConf = controllerConf.copy(workerRestoreConfMapping = { _ =>
              Some(
                WorkerStateRestoreConfig(
                  readFrom = readLocation,
                  replayDestination = replayInfo.interaction
                )
              )
            })
          }
      }
    }

    val job = new WorkflowJobService(
      workflowContext,
      resultService,
      req,
      lastCompletedLogicalPlan
    )

    lifeCycleManager.registerCleanUpOnStateChange(job.jobStateStore)
    jobService.onNext(job)
    if (job.jobStateStore.jobMetadataStore.getState.fatalErrors.isEmpty) {
      job.startWorkflow(controllerConf)
    }
  }

  def convertToJson(frontendVersion: String): String = {
    val environmentVersionMap = Map(
      "engine_version" -> Json.toJson(frontendVersion)
    )
    Json.stringify(Json.toJson(environmentVersionMap))
  }

  override def unsubscribeAll(): Unit = {
    super.unsubscribeAll()
    Option(jobService.getValue).foreach(_.unsubscribeAll())
    resultService.unsubscribeAll()
  }

}
