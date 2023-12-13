package edu.uci.ics.texera.web.service

import akka.actor.Cancellable
import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowCompleted
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.amber.engine.common.client.AmberClient
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode.{SET_DELTA, SET_SNAPSHOT}
import edu.uci.ics.texera.web.model.websocket.event.{PaginatedResultEvent, TexeraWebSocketEvent, WebResultUpdateEvent}
import edu.uci.ics.texera.web.model.websocket.request.ResultPaginationRequest
import edu.uci.ics.texera.web.service.JobResultService.WebResultUpdate
import edu.uci.ics.texera.web.storage.{JobResultMetadataStore, JobStateStore, OperatorResultMetadata}
import edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.RUNNING
import edu.uci.ics.texera.web.{SubscriptionManager, TexeraWebApplication}
import edu.uci.ics.texera.workflow.common.IncrementalOutputMode
import edu.uci.ics.texera.workflow.common.operators.{LogicalOp, OutputDescriptor}
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.workflow.LogicalPlan
import edu.uci.ics.texera.workflow.operators.sink.storage.SinkStorageReader

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object JobResultService {

  val defaultPageSize: Int = 5

  // convert Tuple from engine's format to JSON format
  def webDataFromTuple(
      mode: WebOutputMode,
      table: List[ITuple],
      chartType: Option[String]
  ): WebDataUpdate = {
    val tableInJson = table.map(t => t.asInstanceOf[Tuple].asKeyValuePairJson())
    WebDataUpdate(mode, tableInJson, chartType)
  }

  /**
    *  convert Tuple from engine's format to JSON format
    */
  private def tuplesToWebData(
      mode: WebOutputMode,
      table: List[ITuple],
      chartType: Option[String]
  ): WebDataUpdate = {
    val tableInJson = table.map(t => t.asInstanceOf[Tuple].asKeyValuePairJson())
    WebDataUpdate(mode, tableInJson, chartType)
  }

  /**
    * For SET_SNAPSHOT output mode: result is the latest snapshot
    * FOR SET_DELTA output mode:
    *   - for insert-only delta: effectively the same as latest snapshot
    *   - for insert-retract delta: the union of all delta outputs, not compacted to a snapshot
    *
    * Produces the WebResultUpdate to send to frontend from a result update from the engine.
    */
  def convertWebResultUpdate(
                              outputDescriptor: OutputDescriptor,
                              operatorStorage:SinkStorageReader,
                              oldTupleCount: Int,
                              newTupleCount: Int
  ): WebResultUpdate = {
    val webOutputMode: WebOutputMode = {
      (outputDescriptor.outputMode, outputDescriptor.chartType) match {
        // visualization sinks use its corresponding mode
        case (SET_SNAPSHOT, Some(_)) => SetSnapshotMode()
        case (SET_DELTA, Some(_))    => SetDeltaMode()
        // Non-visualization sinks use pagination mode
        case (_, None) => PaginationMode()
      }
    }

    val webUpdate = (webOutputMode, outputDescriptor.outputMode) match {
      case (PaginationMode(), SET_SNAPSHOT) =>
        val numTuples = operatorStorage.getCount
        val maxPageIndex = Math.ceil(numTuples / JobResultService.defaultPageSize.toDouble).toInt
        WebPaginationUpdate(
          PaginationMode(),
          newTupleCount,
          (1 to maxPageIndex).toList
        )
      case (SetSnapshotMode(), SET_SNAPSHOT) =>
        tuplesToWebData(webOutputMode, operatorStorage.getAll.toList, outputDescriptor.chartType)
      case (SetDeltaMode(), SET_DELTA) =>
        val deltaList = operatorStorage.getAllAfter(oldTupleCount).toList
        tuplesToWebData(webOutputMode, deltaList, outputDescriptor.chartType)

      // currently not supported mode combinations
      // (PaginationMode, SET_DELTA) | (DataSnapshotMode, SET_DELTA) | (DataDeltaMode, SET_SNAPSHOT)
      case _ =>
        throw new RuntimeException(
          "update mode combination not supported: " + (webOutputMode, outputDescriptor.outputMode)
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

  case class WebDataUpdate(mode: WebOutputMode, table: List[ObjectNode], chartType: Option[String])
      extends WebResultUpdate
}

/**
  * WorkflowResultService manages the materialized result of all sink operators in one workflow execution.
  *
  * On each result update from the engine, WorkflowResultService
  *  - update the result data for each operator,
  *  - send result update event to the frontend
  */
class JobResultService(
    logicalPlan: LogicalPlan,
    jobStateStore: JobStateStore,
    client: AmberClient
) extends SubscriptionManager
    with LazyLogging {

  private val resultPullingFrequency = AmberConfig.executionResultPollingInSecs
  private var resultUpdateCancellable: Cancellable = Cancellable.alreadyCancelled

    addSubscription(jobStateStore.jobMetadataStore.getStateObservable.subscribe {
      newState: JobMetadataStore =>
        {
          if (newState.state == RUNNING) {
            if (resultUpdateCancellable == null || resultUpdateCancellable.isCancelled) {
              resultUpdateCancellable = TexeraWebApplication
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
        .registerCallback[WorkflowCompleted](_ => {
          logger.info("Workflow execution completed.")
          if (resultUpdateCancellable.cancel() || resultUpdateCancellable.isCancelled) {
            // immediately perform final update
            onResultUpdate()
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
      jobStateStore.resultMetadataStore.registerDiffHandler((oldState, newState) => {
        val buf = mutable.HashMap[String, WebResultUpdate]()
        newState.resultInfo.foreach {
          case (opId, info) =>
            val oldInfo = oldState.resultInfo.getOrElse(opId, OperatorResultMetadata())
            val logicalOp = logicalPlan.getOperator(opId)
            buf(opId.id) = JobResultService.convertWebResultUpdate(
              // currently, we don't support operators to view its results for multiple
              // output port. E.g. view result from a Split operator.
              // TODO: send results for multiple output ports to the frontend.
              logicalOp.outputPortsInfo.head.outputDescriptor,
              logicalOp.outputPortsInfo.head.storage.get,
              oldInfo.tupleCount,
              info.tupleCount
            )
        }
        Iterable(WebResultUpdateEvent(buf.toMap))
      })
    )

  def handleResultPagination(request: ResultPaginationRequest): TexeraWebSocketEvent = {
    // calculate from index (pageIndex starts from 1 instead of 0)
    val from = request.pageSize * (request.pageIndex - 1)
    val opId = OperatorIdentity(request.operatorID)
    val logicalOp = logicalPlan.getOperator(opId)
    val paginationIterable =
      if (logicalOp.outputPortsInfo.nonEmpty && logicalOp.outputPortsInfo.head.storage.isDefined) {
        logicalOp.outputPortsInfo.head.storage.get.getRange(from, from + request.pageSize)
      } else {
        Iterable.empty
      }
    val mappedResults = paginationIterable
      .map(tuple => tuple.asKeyValuePairJson())
      .toList
    PaginatedResultEvent.apply(request, mappedResults)
  }

  private def onResultUpdate(): Unit = {
    jobStateStore.resultMetadataStore.updateState { _ =>
      val newInfo: Map[OperatorIdentity, OperatorResultMetadata] = logicalPlan.operators
        // currently, we don't support operators to view its results for multiple
        // output port. E.g. view result from a Split operator.
        // TODO: send results for multiple output ports to the frontend.
        .filter(op => op.outputPortsInfo.nonEmpty && op.outputPortsInfo.head.storage.isDefined)
        .map {
        logicalOp =>
          val count = logicalOp.outputPortsInfo.head.storage.get.getCount.toInt
          val mode = logicalOp.outputPortsInfo.head.outputDescriptor.outputMode
          val changeDetector =
            if (mode == IncrementalOutputMode.SET_SNAPSHOT) {
              UUID.randomUUID.toString
            } else ""
          (logicalOp.operatorIdentifier, OperatorResultMetadata(count, changeDetector))
      }.toMap
      JobResultMetadataStore(newInfo)
    }
  }

}
