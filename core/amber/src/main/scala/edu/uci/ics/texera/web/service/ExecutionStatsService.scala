package edu.uci.ics.texera.web.service

import com.google.protobuf.timestamp.Timestamp
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.core.storage.model.BufferedItemWriter
import edu.uci.ics.amber.core.storage.{DocumentFactory, VFSURIFactory}
import edu.uci.ics.amber.core.tuple.{Attribute, AttributeType, Schema, Tuple}
import edu.uci.ics.amber.engine.architecture.controller.{
  ExecutionStatsUpdate,
  FatalError,
  WorkerAssignmentUpdate,
  WorkflowRecoveryStatus
}
import edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkflowAggregatedState
import edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkflowAggregatedState.FAILED
import edu.uci.ics.amber.engine.architecture.worker.statistics.PortTupleCountMapping
import edu.uci.ics.amber.engine.common.Utils.maptoStatusCode
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.executionruntimestate.{
  OperatorMetrics,
  OperatorStatistics,
  OperatorWorkerMapping
}
import edu.uci.ics.amber.engine.common.{AmberConfig, Utils}
import edu.uci.ics.amber.error.ErrorUtils.{getOperatorFromActorIdOpt, getStackTraceWithAllCauses}
import edu.uci.ics.amber.core.workflowruntimestate.FatalErrorType.EXECUTION_FAILURE
import edu.uci.ics.amber.core.workflowruntimestate.WorkflowFatalError
import edu.uci.ics.texera.web.SubscriptionManager
import edu.uci.ics.texera.web.model.websocket.event.{
  ExecutionDurationUpdateEvent,
  OperatorAggregatedMetrics,
  OperatorStatisticsUpdateEvent,
  WorkerAssignmentUpdateEvent
}
import edu.uci.ics.texera.web.storage.ExecutionStateStore
import edu.uci.ics.texera.web.storage.ExecutionStateStore.updateWorkflowState
import edu.uci.ics.amber.core.workflow.WorkflowContext

import java.time.Instant
import java.util.concurrent.Executors

class ExecutionStatsService(
    client: AmberClient,
    stateStore: ExecutionStateStore,
    workflowContext: WorkflowContext
) extends SubscriptionManager
    with LazyLogging {
  private val metricsPersistThread = if (AmberConfig.isUserSystemEnabled) {
    Some(Executors.newSingleThreadExecutor())
  } else {
    None
  }

  private var lastPersistedMetrics: Option[Map[String, OperatorMetrics]] =
    if (AmberConfig.isUserSystemEnabled) {
      Some(Map())
    } else {
      None
    }

  private val runtimeStatisticsSchema = if (AmberConfig.isUserSystemEnabled) {
    Some(
      new Schema(
        new Attribute("operatorId", AttributeType.STRING),
        new Attribute("time", AttributeType.TIMESTAMP),
        new Attribute("inputTupleCnt", AttributeType.LONG),
        new Attribute("outputTupleCnt", AttributeType.LONG),
        new Attribute("dataProcessingTime", AttributeType.LONG),
        new Attribute("controlProcessingTime", AttributeType.LONG),
        new Attribute("idleTime", AttributeType.LONG),
        new Attribute("numWorkers", AttributeType.INTEGER),
        new Attribute("status", AttributeType.INTEGER)
      )
    )
  } else {
    None
  }

  private val identifier = if (AmberConfig.isUserSystemEnabled) {
    Some(s"runtime_statistics_${workflowContext.executionId.id}")
  } else {
    None
  }

  private val uri = if (AmberConfig.isUserSystemEnabled) {
    Some(
      VFSURIFactory.createRuntimeStatisticsURI(
        workflowContext.workflowId,
        workflowContext.executionId
      )
    )
  } else {
    None
  }

  private val writer = if (AmberConfig.isUserSystemEnabled) {
    val w = DocumentFactory
      .createDocument(uri.get, runtimeStatisticsSchema.get)
      .writer(identifier.get)
      .asInstanceOf[BufferedItemWriter[Tuple]]
    w.open()
    Some(w)
  } else {
    None
  }

  registerCallbacks()

  addSubscription(
    stateStore.statsStore.registerDiffHandler((oldState, newState) => {
      // Update operator stats if any operator updates its stat
      if (newState.operatorInfo.toSet != oldState.operatorInfo.toSet) {
        Iterable(
          OperatorStatisticsUpdateEvent(newState.operatorInfo.collect {
            case x =>
              val metrics = x._2
              val res = OperatorAggregatedMetrics(
                Utils.aggregatedStateToString(metrics.operatorState),
                metrics.operatorStatistics.inputCount.map(_.tupleCount).sum,
                metrics.operatorStatistics.outputCount.map(_.tupleCount).sum,
                metrics.operatorStatistics.numWorkers,
                metrics.operatorStatistics.dataProcessingTime,
                metrics.operatorStatistics.controlProcessingTime,
                metrics.operatorStatistics.idleTime
              )
              (x._1, res)
          })
        )
      } else {
        Iterable.empty
      }
    })
  )

  addSubscription(
    stateStore.statsStore.registerDiffHandler((oldState, newState) => {
      // update operators' workers.
      if (newState.operatorWorkerMapping != oldState.operatorWorkerMapping) {
        newState.operatorWorkerMapping
          .map { opToWorkers =>
            WorkerAssignmentUpdateEvent(opToWorkers.operatorId, opToWorkers.workerIds)
          }
      } else {
        Iterable()
      }
    })
  )

  addSubscription(
    stateStore.statsStore.registerDiffHandler((oldState, newState) => {
      // update execution duration.
      if (
        newState.startTimeStamp != oldState.startTimeStamp || newState.endTimeStamp != oldState.endTimeStamp
      ) {
        if (newState.endTimeStamp != 0) {
          Iterable(
            ExecutionDurationUpdateEvent(
              newState.endTimeStamp - newState.startTimeStamp,
              isRunning = false
            )
          )
        } else {
          val currentTime = System.currentTimeMillis()
          Iterable(
            ExecutionDurationUpdateEvent(currentTime - newState.startTimeStamp, isRunning = true)
          )
        }
      } else {
        Iterable()
      }
    })
  )

  private[this] def registerCallbacks(): Unit = {
    registerCallbackOnWorkflowStatsUpdate()
    registerCallbackOnWorkerAssignedUpdate()
    registerCallbackOnWorkflowRecoveryUpdate()
    registerCallbackOnFatalError()
  }

  private[this] def registerCallbackOnWorkflowStatsUpdate(): Unit = {
    addSubscription(
      client
        .registerCallback[ExecutionStatsUpdate]((evt: ExecutionStatsUpdate) => {
          stateStore.statsStore.updateState { statsStore =>
            statsStore.withOperatorInfo(evt.operatorMetrics)
          }
          if (AmberConfig.isUserSystemEnabled) {
            metricsPersistThread.foreach { thread =>
              thread.execute(() => {
                storeRuntimeStatistics(computeStatsDiff(evt.operatorMetrics))
                lastPersistedMetrics = Some(evt.operatorMetrics)
              })
            }
          }
        })
    )
  }

  private def computeStatsDiff(
      newMetrics: Map[String, OperatorMetrics]
  ): Map[String, OperatorMetrics] = {
    val defaultMetrics =
      OperatorMetrics(
        WorkflowAggregatedState.UNINITIALIZED,
        OperatorStatistics(Seq(), Seq(), 0, 0, 0, 0)
      )

    var metricsMap = newMetrics

    // Find keys present in newState.operatorInfo but not in oldState.operatorInfo
    val newKeys = newMetrics.keys.toSet diff lastPersistedMetrics.getOrElse(Map()).keys.toSet
    for (key <- newKeys) {
      lastPersistedMetrics = Some(lastPersistedMetrics.getOrElse(Map()) + (key -> defaultMetrics))
    }

    // Find keys present in oldState.operatorInfo but not in newState.operatorInfo
    val oldKeys = lastPersistedMetrics.getOrElse(Map()).keys.toSet diff newMetrics.keys.toSet
    for (key <- oldKeys) {
      metricsMap = metricsMap + (key -> lastPersistedMetrics.getOrElse(Map())(key))
    }

    metricsMap.keys.map { key =>
      val newMetrics = metricsMap(key)
      val oldMetrics = lastPersistedMetrics.getOrElse(Map())(key)
      val res = OperatorMetrics(
        newMetrics.operatorState,
        OperatorStatistics(
          newMetrics.operatorStatistics.inputCount.map {
            case PortTupleCountMapping(k, v) =>
              PortTupleCountMapping(
                k,
                v - oldMetrics.operatorStatistics.inputCount
                  .find(_.portId == k)
                  .map(_.tupleCount)
                  .getOrElse(0L)
              )
          },
          newMetrics.operatorStatistics.outputCount.map {
            case PortTupleCountMapping(k, v) =>
              PortTupleCountMapping(
                k,
                v - oldMetrics.operatorStatistics.outputCount
                  .find(_.portId == k)
                  .map(_.tupleCount)
                  .getOrElse(0L)
              )
          },
          newMetrics.operatorStatistics.numWorkers,
          newMetrics.operatorStatistics.dataProcessingTime - oldMetrics.operatorStatistics.dataProcessingTime,
          newMetrics.operatorStatistics.controlProcessingTime - oldMetrics.operatorStatistics.controlProcessingTime,
          newMetrics.operatorStatistics.idleTime - oldMetrics.operatorStatistics.idleTime
        )
      )
      (key, res)
    }.toMap
  }

  private def storeRuntimeStatistics(
      operatorStatistics: scala.collection.immutable.Map[String, OperatorMetrics]
  ): Unit = {
    try {
      operatorStatistics.foreach {
        case (operatorId, stat) =>
          val runtimeStats = new Tuple(
            runtimeStatisticsSchema.get,
            Array(
              operatorId,
              new java.sql.Timestamp(System.currentTimeMillis()),
              stat.operatorStatistics.inputCount.map(_.tupleCount).sum,
              stat.operatorStatistics.outputCount.map(_.tupleCount).sum,
              stat.operatorStatistics.dataProcessingTime,
              stat.operatorStatistics.controlProcessingTime,
              stat.operatorStatistics.idleTime,
              stat.operatorStatistics.numWorkers,
              maptoStatusCode(stat.operatorState).toInt
            )
          )
          writer.foreach(_.putOne(runtimeStats))
      }
      writer.foreach(_.close())
    } catch {
      case err: Throwable => logger.error("error occurred when storing runtime statistics", err)
    }
  }

  private[this] def registerCallbackOnWorkerAssignedUpdate(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkerAssignmentUpdate]((evt: WorkerAssignmentUpdate) => {
          stateStore.statsStore.updateState { statsStore =>
            statsStore.withOperatorWorkerMapping(
              evt.workerMapping
                .map({
                  case (opId, workerIds) => OperatorWorkerMapping(opId, workerIds.toSeq)
                })
                .toSeq
            )
          }
        })
    )
  }

  private[this] def registerCallbackOnWorkflowRecoveryUpdate(): Unit = {
    addSubscription(
      client
        .registerCallback[WorkflowRecoveryStatus]((evt: WorkflowRecoveryStatus) => {
          stateStore.metadataStore.updateState { metadataStore =>
            metadataStore.withIsRecovering(evt.isRecovering)
          }
        })
    )
  }

  private[this] def registerCallbackOnFatalError(): Unit = {
    addSubscription(
      client
        .registerCallback[FatalError]((evt: FatalError) => {
          client.shutdown()
          val (operatorId, workerId) = getOperatorFromActorIdOpt(evt.fromActor)
          stateStore.statsStore.updateState(stats =>
            stats.withEndTimeStamp(System.currentTimeMillis())
          )
          stateStore.metadataStore.updateState { metadataStore =>
            logger.error("error occurred in execution", evt.e)
            updateWorkflowState(FAILED, metadataStore).addFatalErrors(
              WorkflowFatalError(
                EXECUTION_FAILURE,
                Timestamp(Instant.now),
                evt.e.toString,
                getStackTraceWithAllCauses(evt.e),
                operatorId,
                workerId
              )
            )
          }
        })
    )
  }
}
