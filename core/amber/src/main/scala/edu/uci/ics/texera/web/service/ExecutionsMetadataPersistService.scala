package edu.uci.ics.texera.web.service

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.Utils.maptoStatusCode
import edu.uci.ics.amber.engine.common.AmberConfig
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.WorkflowExecutionsDao
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowExecutions
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowVersionResource._
import edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
import edu.uci.ics.texera.workflow.common.WorkflowContext.DEFAULT_EXECUTION_ID
import org.jooq.types.UInteger

import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicLong

/**
  * This global object handles inserting a new entry to the DB to store metadata information about every workflow execution
  * It also updates the entry if an execution status is updated
  */
object ExecutionsMetadataPersistService extends LazyLogging {
  final private lazy val context = SqlServer.createDSLContext()
  private var localExecutionId = new AtomicLong(0L)
  private val workflowExecutionsDao = new WorkflowExecutionsDao(
    context.configuration
  )

  /**
    * This method inserts a new entry of a workflow execution in the database and returns the generated eId
    *
    * @param wid     the given workflow
    * @param uid     user id that initiated the execution
    * @return generated execution ID
    */

  def insertNewExecution(
      wid: UInteger,
      uid: Option[UInteger],
      executionName: String,
      environmentVersion: String
  ): Long = {
    if (!AmberConfig.isUserSystemEnabled) return DEFAULT_EXECUTION_ID
    // first retrieve the latest version of this workflow
    val vid = getLatestVersion(wid)
    val newExecution = new WorkflowExecutions()
    if (executionName != "") {
      newExecution.setName(executionName)
    }
    newExecution.setVid(vid)
    newExecution.setUid(uid.orNull)
    newExecution.setStartingTime(new Timestamp(System.currentTimeMillis()))
    newExecution.setEnvironmentVersion(environmentVersion)
    workflowExecutionsDao.insert(newExecution)
    newExecution.getEid.longValue()
  }

  def tryGetExistingExecution(eid:Long):Option[WorkflowExecutions] = {
    if (!AmberConfig.isUserSystemEnabled) return None
    try {
      Some(workflowExecutionsDao.fetchOneByEid(UInteger.valueOf(eid)))
    } catch {
      case t: Throwable =>
        logger.info("Unable to get execution. Error = " + t.getMessage)
        None
    }
  }

  def tryUpdateExistingExecution(eid:Long)(updateFunc:WorkflowExecutions => Unit): Unit = {
    if (!AmberConfig.isUserSystemEnabled) return
    try {
      val execution = workflowExecutionsDao.fetchOneByEid(UInteger.valueOf(eid))
      updateFunc(execution)
      workflowExecutionsDao.update(execution)
    } catch {
      case t: Throwable =>
        logger.info("Unable to update execution. Error = " + t.getMessage)
    }
  }
}
