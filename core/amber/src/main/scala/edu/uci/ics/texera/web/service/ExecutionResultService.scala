package edu.uci.ics.texera.web.service

import akka.actor.Cancellable
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.core.storage.StorageConfig
import edu.uci.ics.amber.core.storage.result.{
  MongoDocument,
  OperatorResultMetadata,
  ResultStorage,
  WorkflowResultStore
}
import edu.uci.ics.amber.core.tuple.Tuple
import edu.uci.ics.amber.engine.architecture.controller.{ExecutionStateUpdate, FatalError}
import edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkflowAggregatedState.{
  COMPLETED,
  FAILED,
  KILLED,
  RUNNING
}
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.executionruntimestate.ExecutionMetadataStore
import edu.uci.ics.amber.engine.common.{AmberConfig, AmberRuntime}
import edu.uci.ics.amber.operator.sink.managed.ProgressiveSinkOpDesc
import edu.uci.ics.amber.virtualidentity.OperatorIdentity
import edu.uci.ics.amber.workflow.OutputPort.OutputMode
import edu.uci.ics.texera.web.SubscriptionManager
import edu.uci.ics.texera.web.model.websocket.event.{
  PaginatedResultEvent,
  TexeraWebSocketEvent,
  WebResultUpdateEvent
}
import edu.uci.ics.texera.web.model.websocket.request.ResultPaginationRequest
import edu.uci.ics.texera.web.storage.{ExecutionStateStore, WorkflowStateStore}
import edu.uci.ics.texera.workflow.LogicalPlan

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object ExecutionResultService {

  private val defaultPageSize: Int = 5

  /**
    * convert Tuple from engine's format to JSON format
    */
  private def tuplesToWebData(
      mode: WebOutputMode,
      table: List[Tuple]
  ): WebDataUpdate = {
    val tableInJson = table.map(t => t.asKeyValuePairJson())
    WebDataUpdate(mode, tableInJson)
  }

  /**
    * For SET_SNAPSHOT output mode: result is the latest snapshot
    * FOR SET_DELTA output mode:
    *   - for insert-only delta: effectively the same as latest snapshot
    *   - for insert-retract delta: the union of all delta outputs, not compacted to a snapshot
    *
    * Produces the WebResultUpdate to send to frontend from a result update from the engine.
    */
  private def convertWebResultUpdate(
      sink: ProgressiveSinkOpDesc,
      oldTupleCount: Int,
      newTupleCount: Int
  ): WebResultUpdate = {
    val webOutputMode: WebOutputMode = {
      sink.getOutputMode match {
        // currently, only table outputs are using these modes
        case OutputMode.SET_DELTA    => SetDeltaMode()
        case OutputMode.SET_SNAPSHOT => PaginationMode()

        // currently, only visualizations are using single snapshot mode
        case OutputMode.SINGLE_SNAPSHOT => SetSnapshotMode()
      }
    }

    val storage =
      ResultStorage.getOpResultStorage(sink.getContext.workflowId).get(sink.getUpstreamId.get)
    val webUpdate = webOutputMode match {
      case PaginationMode() =>
        val numTuples = storage.getCount
        val maxPageIndex =
          Math.ceil(numTuples / defaultPageSize.toDouble).toInt
        WebPaginationUpdate(
          PaginationMode(),
          newTupleCount,
          (1 to maxPageIndex).toList
        )
      case SetSnapshotMode() =>
        tuplesToWebData(webOutputMode, storage.get().toList)
      case SetDeltaMode() =>
        val deltaList = storage.getAfter(oldTupleCount).toList
        tuplesToWebData(webOutputMode, deltaList)

      case _ =>
        throw new RuntimeException(
          "update mode combination not supported: " + (webOutputMode, sink.getOutputMode)
        )
    }
    webUpdate
  }

  /**
    * Behavior for different web output modes:
    *  - PaginationMode   (used by view result operator)
    *     - send new number of tuples and dirty page index
    *  - SetSnapshotMode  (used by visualization in snapshot mode)
    *     - send entire snapshot result to frontend
    *  - SetDeltaMode     (used by visualization in delta mode)
    *     - send incremental delta result to frontend
    */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
  sealed abstract class WebOutputMode extends Product with Serializable

  /**
    * The result update of one operator that will be sent to the frontend.
    * Can be either WebPaginationUpdate (for PaginationMode)
    * or WebDataUpdate (for SetSnapshotMode or SetDeltaMode)
    */
  sealed abstract class WebResultUpdate extends Product with Serializable

  @JsonTypeName("PaginationMode")
  final case class PaginationMode() extends WebOutputMode

  @JsonTypeName("SetSnapshotMode")
  final case class SetSnapshotMode() extends WebOutputMode

  @JsonTypeName("SetDeltaMode")
  final case class SetDeltaMode() extends WebOutputMode

  case class WebPaginationUpdate(
      mode: PaginationMode,
      totalNumTuples: Long,
      dirtyPageIndices: List[Int]
  ) extends WebResultUpdate

  case class WebDataUpdate(mode: WebOutputMode, table: List[ObjectNode]) extends WebResultUpdate

}

/**
  * ExecutionResultService manages the materialized result of all sink operators in one workflow execution.
  *
  * On each result update from the engine, WorkflowResultService
  *  - update the result data for each operator,
  *  - send result update event to the frontend
  */
class ExecutionResultService(
    val workflowStateStore: WorkflowStateStore
) extends SubscriptionManager
    with LazyLogging {

  var sinkOperators: mutable.HashMap[OperatorIdentity, ProgressiveSinkOpDesc] =
    mutable.HashMap[OperatorIdentity, ProgressiveSinkOpDesc]()
  private val resultPullingFrequency = AmberConfig.executionResultPollingInSecs
  private var resultUpdateCancellable: Cancellable = _

  def attachToExecution(
      stateStore: ExecutionStateStore,
      logicalPlan: LogicalPlan,
      client: AmberClient
  ): Unit = {

    if (resultUpdateCancellable != null && !resultUpdateCancellable.isCancelled) {
      resultUpdateCancellable.cancel()
    }

    unsubscribeAll()

    addSubscription(stateStore.metadataStore.getStateObservable.subscribe {
      newState: ExecutionMetadataStore =>
        {
          if (newState.state == RUNNING) {
            if (resultUpdateCancellable == null || resultUpdateCancellable.isCancelled) {
              resultUpdateCancellable = AmberRuntime
                .scheduleRecurringCallThroughActorSystem(
                  2.seconds,
                  resultPullingFrequency.seconds
                ) {
                  onResultUpdate()
                }
            }
          } else {
            if (resultUpdateCancellable != null) resultUpdateCancellable.cancel()
          }
        }
    })

    addSubscription(
      client
        .registerCallback[ExecutionStateUpdate](evt => {
          if (evt.state == COMPLETED || evt.state == FAILED || evt.state == KILLED) {
            logger.info("Workflow execution terminated. Stop update results.")
            if (resultUpdateCancellable.cancel() || resultUpdateCancellable.isCancelled) {
              // immediately perform final update
              onResultUpdate()
            }
          }
        })
    )

    addSubscription(
      client.registerCallback[FatalError](_ =>
        if (resultUpdateCancellable != null) {
          resultUpdateCancellable.cancel()
        }
      )
    )

    addSubscription(
      workflowStateStore.resultStore.registerDiffHandler((oldState, newState) => {
        val buf = mutable.HashMap[String, ExecutionResultService.WebResultUpdate]()
        val allTableStats = mutable.Map[String, Map[String, Map[String, Any]]]()
        newState.resultInfo
          .filter(info => {
            // only update those operators with changing tuple count.
            !oldState.resultInfo
              .contains(info._1) || oldState.resultInfo(info._1).tupleCount != info._2.tupleCount
          })
          .foreach {
            case (opId, info) =>
              val oldInfo = oldState.resultInfo.getOrElse(opId, OperatorResultMetadata())
              buf(opId.id) = ExecutionResultService.convertWebResultUpdate(
                sinkOperators(opId),
                oldInfo.tupleCount,
                info.tupleCount
              )
              if (
                StorageConfig.resultStorageMode.toLowerCase == "mongodb"
                && !opId.id.startsWith("sink")
              ) {
                val sinkOp = sinkOperators(opId)
                val opStorage = ResultStorage
                  .getOpResultStorage(sinkOp.getContext.workflowId)
                  .get(sinkOp.getUpstreamId.get)
                opStorage match {
                  case mongoDocument: MongoDocument[Tuple] =>
                    val tableCatStats = mongoDocument.getCategoricalStats
                    val tableDateStats = mongoDocument.getDateColStats
                    val tableNumericStats = mongoDocument.getNumericColStats

                    if (
                      tableNumericStats.nonEmpty || tableCatStats.nonEmpty || tableDateStats.nonEmpty
                    ) {
                      allTableStats(opId.id) = tableNumericStats ++ tableCatStats ++ tableDateStats
                    }
                  case _ =>
                }
              }
          }
        Iterable(
          WebResultUpdateEvent(
            buf.toMap,
            allTableStats.toMap,
            StorageConfig.resultStorageMode.toLowerCase
          )
        )
      })
    )

    // first clear all the results
    sinkOperators.clear()
    workflowStateStore.resultStore.updateState { _ =>
      WorkflowResultStore() // empty result store
    }

    // For operators connected to a sink and sinks,
    // create result service so that the results can be displayed.
    logicalPlan.getTerminalOperatorIds.map(sink => {
      logicalPlan.getOperator(sink) match {
        case sinkOp: ProgressiveSinkOpDesc =>
          sinkOperators += ((sinkOp.getUpstreamId.get, sinkOp))
          sinkOperators += ((sink, sinkOp))
        case other => // skip other non-texera-managed sinks, if any
      }
    })
  }

  def handleResultPagination(request: ResultPaginationRequest): TexeraWebSocketEvent = {
    // calculate from index (pageIndex starts from 1 instead of 0)
    val from = request.pageSize * (request.pageIndex - 1)
    val opId = OperatorIdentity(request.operatorID)
    val paginationIterable = {

      if (sinkOperators.contains(opId)) {
        val sinkOp = sinkOperators(opId)
        ResultStorage
          .getOpResultStorage(sinkOp.getContext.workflowId)
          .get(sinkOp.getUpstreamId.get)
          .getRange(from, from + request.pageSize)
          .to(Iterable)
      } else {
        Iterable.empty
      }
    }
    val mappedResults = paginationIterable
      .map(tuple => tuple.asKeyValuePairJson())
      .toList
    val attributes = paginationIterable.headOption
      .map(_.getSchema.getAttributes)
      .getOrElse(List.empty)
    PaginatedResultEvent.apply(request, mappedResults, attributes)
  }

  private def onResultUpdate(): Unit = {
    workflowStateStore.resultStore.updateState { _ =>
      val newInfo: Map[OperatorIdentity, OperatorResultMetadata] = sinkOperators.map {

        case (id, sink) =>
          val count = ResultStorage
            .getOpResultStorage(sink.getContext.workflowId)
            .get(sink.getUpstreamId.get)
            .getCount
            .toInt
          val mode = sink.getOutputMode
          val changeDetector =
            if (mode == OutputMode.SET_SNAPSHOT) {
              UUID.randomUUID.toString
            } else ""
          (id, OperatorResultMetadata(count, changeDetector))
      }.toMap
      WorkflowResultStore(newInfo)
    }
  }

}
