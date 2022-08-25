package edu.uci.ics.texera.web.resource.dashboard.workflow

import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.Tables.{USER, WORKFLOW_EXECUTIONS}
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.WorkflowExecutionsDao
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.WorkflowExecutions
import edu.uci.ics.texera.web.resource.dashboard.workflow.WorkflowExecutionsResource._
import edu.uci.ics.texera.workflow.common.tuple.schema.{Attribute, AttributeType, Schema}
import edu.uci.ics.texera.workflow.operators.sink.storage.{
  MongoDBStorage,
}
import edu.uci.ics.texera.workflow.common.tuple.Tuple
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection, MongoCursor, MongoDatabase}
import edu.uci.ics.amber.engine.common.AmberUtils

import io.dropwizard.auth.Auth
import org.jooq.impl.DSL.field
import org.jooq.types.UInteger

import java.sql.Timestamp
import javax.annotation.security.PermitAll
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object WorkflowExecutionsResource {
  final private lazy val context = SqlServer.createDSLContext()
  final private lazy val executionsDao = new WorkflowExecutionsDao(context.configuration)

  def getExecutionById(eId: UInteger): WorkflowExecutions = {
    executionsDao.fetchOneByEid(eId)
  }

  case class WorkflowExecutionEntry(
      eId: UInteger,
      vId: UInteger,
      userName: String,
      startingTime: Timestamp,
      completionTime: Timestamp,
      status: Byte,
      result: String,
      bookmarked: Boolean,
      name: String
  )

}

case class ExecutionBookmarkRequest(wid: UInteger, eId: UInteger, isBookmarked: Boolean)
case class ExecutionDeleteRequest(wid: UInteger, eId: UInteger)
case class ExecutionRenameRequest(wid: UInteger, eId: UInteger, executionName: String)

@PermitAll
@Path("/executions")
@Produces(Array(MediaType.APPLICATION_JSON))
class WorkflowExecutionsResource {

  /**
    * This method returns the executions of a workflow given by its ID
    *
    * @return executions[]
    */
  @GET
  @Path("/{wid}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def retrieveExecutionsOfWorkflow(
      @PathParam("wid") wid: UInteger,
      @Auth sessionUser: SessionUser
  ): List[WorkflowExecutionEntry] = {
    val user = sessionUser.getUser
    if (
      WorkflowAccessResource.hasNoWorkflowAccess(wid, user.getUid) ||
      WorkflowAccessResource.hasNoWorkflowAccessRecord(wid, user.getUid)
    ) {
      List()
    } else {
      context
        .select(
          WORKFLOW_EXECUTIONS.EID,
          WORKFLOW_EXECUTIONS.VID,
          field(
            context
              .select(USER.NAME)
              .from(USER)
              .where(WORKFLOW_EXECUTIONS.UID.eq(USER.UID))
          ),
          WORKFLOW_EXECUTIONS.STARTING_TIME,
          WORKFLOW_EXECUTIONS.COMPLETION_TIME,
          WORKFLOW_EXECUTIONS.STATUS,
          WORKFLOW_EXECUTIONS.RESULT,
          WORKFLOW_EXECUTIONS.BOOKMARKED,
          WORKFLOW_EXECUTIONS.NAME
        )
        .from(WORKFLOW_EXECUTIONS)
        .where(WORKFLOW_EXECUTIONS.WID.eq(wid))
        .fetchInto(classOf[WorkflowExecutionEntry])
        .toList
    }
  }

  /** Sets a single execution's bookmark to the payload passed in the body. */
  @PUT
  @Path("/set_execution_bookmark")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def setExecutionIsBookmarked(
      request: ExecutionBookmarkRequest,
      @Auth sessionUser: SessionUser
  ): Unit = {
    validateUserCanAccessWorkflow(sessionUser.getUser.getUid, request.wid)
    val execution: WorkflowExecutions = getExecutionById(request.eId)
    execution.setBookmarked((if (request.isBookmarked) 1 else 0).toByte)
    executionsDao.update(execution)
  }

  /** Determine if user is authorized to access the workflow, if not raise 401 */
  def validateUserCanAccessWorkflow(uid: UInteger, wid: UInteger): Unit = {
    if (
      WorkflowAccessResource.hasNoWorkflowAccess(wid, uid) ||
      WorkflowAccessResource.hasNoWorkflowAccessRecord(wid, uid)
    )
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

  /** Delete a single execution */
  @PUT
  @Path("/delete_execution")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def deleteExecutionsOfWorkflow(
      request: ExecutionDeleteRequest,
      @Auth sessionUser: SessionUser
  ): Unit = {
    validateUserCanAccessWorkflow(sessionUser.getUser.getUid, request.wid)
    /* delete the execution in sql */
    context
      .delete(WORKFLOW_EXECUTIONS)
      .where(WORKFLOW_EXECUTIONS.EID.eq(request.eId))
      .execute();
  }

  /** Name a single execution * */
  @POST
  @Path("/update_execution_name")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def updateWorkflowExecutionsName(
      request: ExecutionRenameRequest,
      @Auth sessionUser: SessionUser
  ): Unit = {
    validateUserCanAccessWorkflow(sessionUser.getUser.getUid, request.wid)
    val execution = getExecutionById(request.eId)
    execution.setName(request.executionName)
    executionsDao.update(execution)
  }


  /**
   * This method returns the executions of a workflow given by its ID
   *
   * @return executions[]
   */
  @GET
  @Path("/{wid}/{result}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def retrieveResult(
    @PathParam("wid") wid: UInteger,
    @PathParam("result") result: String,
    @Auth sessionUser: SessionUser
  ): String = {
    val user = sessionUser.getUser
    if (
      WorkflowAccessResource.hasNoWorkflowAccess(wid, user.getUid) ||
        WorkflowAccessResource.hasNoWorkflowAccessRecord(wid, user.getUid)
    ) {
      "Invlid User"
    } else {
      //      val url: String = AmberUtils.amberConfig.getString("storage.mongodb.url")
      //      val databaseName: String = AmberUtils.amberConfig.getString("storage.mongodb.database")
      //      val client: MongoClient = MongoClients.create(url)
      //      val database: MongoDatabase = client.getDatabase(databaseName)
//      new Attribute(attributeName = "id", AttributeType.INTEGER),
//      new Attribute(attributeName = "date", AttributeType.INTEGER),
//      new Attribute(attributeName = "title", AttributeType.STRING),
      val schema = new Schema(
        new Attribute("id", AttributeType.INTEGER),
        new Attribute("date", AttributeType.INTEGER),
        new Attribute("title", AttributeType.STRING),
        new Attribute("status", AttributeType.INTEGER),
        new Attribute("message", AttributeType.STRING),
        new Attribute("type", AttributeType.STRING),
      )
      //      val resultCount = database.getCollection(result).countDocuments()
      //      val resultRow = database.getCollection(result)
      val resultTable = new MongoDBStorage(result, schema)
      val resultRow = resultTable.getRange(0, 1).map(oneRowArray => oneRowArray.asKeyValuePairJson()).toList
      val resultRowJson = resultRow(0).toString()
      resultRowJson
      //    val resultCount = resultTable.getCount;
      //    resultCount
    }
  }
}
