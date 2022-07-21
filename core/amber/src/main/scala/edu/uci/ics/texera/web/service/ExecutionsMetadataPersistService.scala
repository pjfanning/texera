package edu.uci.ics.texera.web.service

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.WorkflowExecutionsDao
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowExecutions
import edu.uci.ics.texera.web.resource.dashboard.workflow.WorkflowVersionResource
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import org.jooq.types.UInteger

import java.sql.Timestamp

/**
 * This global object handles inserting a new entry to the DB to store metadata information about every workflow execution
 * It also updates the entry if an execution status is updated
 */
object ExecutionsMetadataPersistService extends LazyLogging {
  final private lazy val context = SqlServer.createDSLContext()

  private val workflowExecutionsDao = new WorkflowExecutionsDao(
    context.configuration
  )

  /**
   * @param state indicates the workflow state
   * @return code indicates the status of the execution in the DB it is 0 by default for any unused states.
   *         This code is stored in the DB and read in the frontend.
   *         If these codes are changed, they also have to be changed in the frontend `ngbd-modal-workflow-executions.component.ts`
   */
  private def maptoStatusCode(state: WorkflowAggregatedState): Byte = {
    state match {
      case WorkflowAggregatedState.UNINITIALIZED => 0
      case WorkflowAggregatedState.READY => 0
      case WorkflowAggregatedState.RUNNING => 1
      case WorkflowAggregatedState.PAUSING => ???
      case WorkflowAggregatedState.PAUSED => 2
      case WorkflowAggregatedState.RESUMING => ???
      case WorkflowAggregatedState.RECOVERING => ???
      case WorkflowAggregatedState.COMPLETED => 3
      case WorkflowAggregatedState.ABORTED => 4
      case WorkflowAggregatedState.UNKNOWN => ???
      case WorkflowAggregatedState.Unrecognized(unrecognizedValue) => ???
    }
  }

  /**
   * @param
   * @return
   */
  def maptoAggregatedState(statusCode: Byte): WorkflowAggregatedState = {
    statusCode match {
      case 0 => WorkflowAggregatedState.READY
      case 1 => WorkflowAggregatedState.RUNNING
      case 2 => WorkflowAggregatedState.PAUSED
      case 3 => WorkflowAggregatedState.COMPLETED
      case 4 => WorkflowAggregatedState.ABORTED
    }
  }

  /**
   * This method inserts a new entry of a workflow execution in the database and returns the generated eId
   *
   * @param wid the given workflow
   * @param uid user id that initiated the execution
   * @return generated execution ID
   */

  def insertNewExecution(
                          wid: Long,
                          vid: Int,
                          uid: Option[UInteger]
                        ): Long = {
    // first retrieve the latest version of this workflow
    val uint = UInteger.valueOf(wid)
    val newExecution = new WorkflowExecutions()
    newExecution.setWid(uint)
    newExecution.setVid(UInteger.valueOf(vid))
    newExecution.setUid(uid.getOrElse(null))
    newExecution.setStartingTime(new Timestamp(System.currentTimeMillis()))
    workflowExecutionsDao.insert(newExecution)
    newExecution.getEid.longValue()
  }

  def tryUpdateExistingExecutionStatus(eid: Long, state: WorkflowAggregatedState): UInteger = {
    var wId: UInteger = UInteger.valueOf(0)
    try {
      val code = maptoStatusCode(state)
      val execution: WorkflowExecutions = workflowExecutionsDao.fetchOneByEid(UInteger.valueOf(eid))
      wId = execution.getWid
      execution.setStatus(code)
      execution.setCompletionTime(new Timestamp(System.currentTimeMillis()))
      workflowExecutionsDao.update(execution)

    } catch {
      case t: Throwable =>
        logger.info("Unable to update execution. Error = " + t.getMessage)
    }
    wId
  }

  def updateExistingExecutionVolumnPointers(eid: Long, pointers: String): Unit = {
    try {
      val execution = workflowExecutionsDao.fetchOneByEid(UInteger.valueOf(eid))
      execution.setResult(pointers)
      workflowExecutionsDao.update(execution)
    } catch {
      case t: Throwable =>
        logger.info("Unable to update execution. Error = " + t.getMessage)
    }
  }
}
