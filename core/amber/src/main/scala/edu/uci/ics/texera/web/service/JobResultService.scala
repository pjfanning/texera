package edu.uci.ics.texera.web.service

import com.fasterxml.jackson.annotation.{JsonTypeInfo, JsonTypeName}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.twitter.util.{Future, FuturePool}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.WorkflowResultUpdate
import edu.uci.ics.amber.engine.common.{AmberClient, AmberUtils}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.texera.web.{SnapshotMulticast, TexeraWebApplication}
import edu.uci.ics.texera.web.model.websocket.event.WorkflowAvailableResultEvent.OperatorAvailableResult
import edu.uci.ics.texera.web.model.websocket.event.{PaginatedResultEvent, TexeraWebSocketEvent, WebResultUpdateEvent, WorkflowAvailableResultEvent}
import edu.uci.ics.texera.web.model.websocket.request.ResultPaginationRequest
import edu.uci.ics.texera.web.service.JobResultService.{PaginationMode, WebPaginationUpdate, defaultPageSize}
import edu.uci.ics.texera.workflow.common.operators.OperatorDescriptor
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import edu.uci.ics.texera.workflow.common.workflow.{WorkflowCompiler, WorkflowInfo}
import edu.uci.ics.texera.workflow.operators.sink.managed.AppendOnlyTableSinkOpDesc
import edu.uci.ics.texera.workflow.operators.sink.storage.SinkStorage
import rx.lang.scala.Observer

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object JobResultService {

  val defaultPageSize: Int = 10

  var opResultStorage: OpResultStorage = new OpResultStorage(
    AmberUtils.amberConfig.getString("storage.mode").toLowerCase
  )

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
    * Calculates the dirty pages (pages with changed tuples) between two progressive updates,
    * by comparing the "before" snapshot and "after" snapshot tuple-by-tuple.
    * Used by WebPaginationUpdate
    *
    * @return list of indices of modified pages, index starts from 1
    */
  def calculateDirtyPageIndices(
      beforeSnapshot: List[ITuple],
      afterSnapshot: List[ITuple],
      pageSize: Int
  ): List[Int] = {
    var currentIndex = 1
    var currentIndexPageCount = 0
    val dirtyPageIndices = new mutable.HashSet[Int]()
    for ((before, after) <- beforeSnapshot.zipAll(afterSnapshot, null, null)) {
      if (before == null || after == null || !before.equals(after)) {
        dirtyPageIndices.add(currentIndex)
      }
      currentIndexPageCount += 1
      if (currentIndexPageCount == pageSize) {
        currentIndexPageCount = 0
        currentIndex += 1
      }
    }
    dirtyPageIndices.toList
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
      totalNumTuples: Int,
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
    workflowInfo: WorkflowInfo,
    client: AmberClient
) extends SnapshotMulticast[TexeraWebSocketEvent] {
  import JobResultService._
  var progressiveResults: mutable.HashMap[String, ProgressiveResultService] =
    mutable.HashMap[String, ProgressiveResultService]()
  val tableResults = new mutable.HashMap[String, SinkStorage]()
  var availableResultMap: Map[String, OperatorAvailableResult] = Map.empty

  client
    .getObservable[WorkflowResultUpdate]
    .subscribe((evt: WorkflowResultUpdate) => onResultUpdate(evt))

  TexeraWebApplication.scheduleRecurringCallThroughActorSystem(3.seconds, 3.seconds){
    onTableResultUpdate()
  }

  workflowInfo.toDAG.getSinkOperators.map(sink => {
    workflowInfo.toDAG.getOperator(sink) match {
      case desc: AppendOnlyTableSinkOpDesc =>
        val upstreamID = workflowInfo.toDAG.getUpstream(sink).head.operatorID
        if(workflowInfo.cachedOperatorIds.contains(upstreamID)){
          tableResults += ((upstreamID, opResultStorage.get(desc.operatorID)))
        }else{
          tableResults += ((sink, opResultStorage.get(desc.operatorID)))
        }
      case _ =>
        val service = new ProgressiveResultService(sink, workflowInfo)
        service.uuid = sink
        progressiveResults += ((sink, service))
    }
  })

  def handleResultPagination(request: ResultPaginationRequest): Unit = {
    // calculate from index (pageIndex starts from 1 instead of 0)
    val from = request.pageSize * (request.pageIndex - 1)
    val opId = request.operatorID
    val paginationIterable =
      if(progressiveResults.contains(opId)){
        progressiveResults(opId).getResult.slice(from, from + request.pageSize)
      }else if(tableResults.contains(opId)){
        tableResults(opId).getRange(from, from + request.pageSize)
      }else{
        Iterable.empty
      }
    val mappedResults = paginationIterable
      .map(tuple => tuple.asInstanceOf[Tuple].asKeyValuePairJson())
      .toList
    send(PaginatedResultEvent.apply(request, mappedResults))
  }

  def onResultUpdate(
      resultUpdate: WorkflowResultUpdate
  ): Unit = {
    val webUpdateEvent = resultUpdate.operatorResults.map {
      case (id, resultUpdate) =>
        (id, progressiveResults(id).convertWebResultUpdate(resultUpdate))
    }

    // return update event
    send(WebResultUpdateEvent(webUpdateEvent))
  }

  def onTableResultUpdate(): Unit ={
    val webUpdateEvent = tableResults.map{
      case (id, storage) =>
        (id, WebPaginationUpdate(PaginationMode(), storage.getCount.toInt, List.empty))
    }.toMap
    // return update event
    send(WebResultUpdateEvent(webUpdateEvent))
  }

  //TODO: refactor the code below

  def updateResultFromPreviousRun(
                                   previousResults: mutable.HashMap[String, ProgressiveResultService],
                                   cachedOps: mutable.HashMap[String, OperatorDescriptor]
  ): Unit = {
    progressiveResults.foreach(e => {
      if (previousResults.contains(e._2.operatorID)) {
        previousResults(e._2.operatorID) = e._2
      }
    })
    previousResults.foreach(e => {
      if (cachedOps.contains(e._2.operatorID) && !progressiveResults.contains(e._2.operatorID)) {
        progressiveResults += ((e._2.operatorID, e._2))
      }
    })
  }

  def updateAvailableResult(operators: Iterable[OperatorDescriptor]): Unit = {
    val cachedIDs = mutable.HashSet[String]()
    val cachedIDMap = mutable.HashMap[String, String]()
    progressiveResults.foreach(e => cachedIDMap += ((e._2.operatorID, e._1)))
    availableResultMap = operators
      .filter(op => cachedIDMap.contains(op.operatorID))
      .map(op => op.operatorID)
      .map(id => {
        (
          id,
          OperatorAvailableResult(
            cachedIDs.contains(id),
            progressiveResults(cachedIDMap(id)).webOutputMode
          )
        )
      })
      .toMap
  }

  override def sendSnapshotTo(observer: Observer[TexeraWebSocketEvent]): Unit = {
    observer.onNext(WorkflowAvailableResultEvent(availableResultMap))
    observer.onNext(WebResultUpdateEvent(progressiveResults.map(e => (e._1, e._2.getSnapshot)).toMap))
  }

}
