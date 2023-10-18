package edu.uci.ics.texera.web.resource.dashboard.user.quota
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.resource.dashboard.user.quota.UserQuotaResource.{context, file, getCollectionName, workflow}
import org.jooq.types.UInteger

import java.util
import javax.ws.rs._
import javax.ws.rs.core.MediaType
import edu.uci.ics.texera.web.model.jooq.generated.Tables._
import edu.uci.ics.texera.web.resource.dashboard.admin.user.AdminUserResource.mongoStorage

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import edu.uci.ics.texera.web.storage.MongoDatabaseManager
import io.dropwizard.auth.Auth

object UserQuotaResource {
  final private lazy val context = SqlServer.createDSLContext()

  case class file(
                   userId: UInteger,
                   fileId: UInteger,
                   fileName: String,
                   fileSize: UInteger,
                   uploadedTime: Long,
                   description: String
                 )

  case class workflow(
                       userId: UInteger,
                       workflowId: UInteger,
                       workflowName: String
                     )

  def getCollectionName(result: String): String = {
    var quoteCount = 0
    var name = ""

    for (chr <- result) {
      if (chr == '\"') {
        quoteCount += 1
      } else if (quoteCount == 3) {
        name += chr
      }
    }

    name
  }
}

@Path("/quota")
class UserQuotaResource {

  @GET
  @Path("/uploaded_files")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCreatedFile(@Auth current_user: SessionUser): List[file] = {
    val userFileEntries = context
      .select(
        FILE.OWNER_UID,
        FILE.FID,
        FILE.NAME,
        FILE.SIZE,
        FILE.UPLOAD_TIME,
        FILE.DESCRIPTION
      )
      .from(FILE)
      .where(FILE.OWNER_UID.eq(current_user.getUid))
      .fetch()

    userFileEntries
      .map(fileRecord => {
        file(
          fileRecord.get(FILE.OWNER_UID),
          fileRecord.get(FILE.FID),
          fileRecord.get(FILE.NAME),
          fileRecord.get(FILE.SIZE),
          fileRecord.get(FILE.UPLOAD_TIME).getTime,
          fileRecord.get(FILE.DESCRIPTION)
        )
      }).toList
  }

  @GET
  @Path("/created_workflows")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCreatedWorkflow(@Auth current_user: SessionUser): List[workflow] = {
    val userWorkflowEntries = context
      .select(
        WORKFLOW_OF_USER.UID,
        WORKFLOW_OF_USER.WID,
        WORKFLOW.NAME
      )
      .from(
        WORKFLOW_OF_USER
      )
      .leftJoin(
        WORKFLOW
      )
      .on(
        WORKFLOW.WID.eq(WORKFLOW_OF_USER.WID)
      )
      .where(
        WORKFLOW_OF_USER.UID.eq(current_user.getUid)
      )
      .fetch()

    userWorkflowEntries
      .map(workflowRecord => {
        workflow(
          workflowRecord.get(WORKFLOW_OF_USER.UID),
          workflowRecord.get(WORKFLOW_OF_USER.WID),
          workflowRecord.get(WORKFLOW.NAME)
        )
      }).toList
  }

  @GET
  @Path("/access_workflows")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getAccessedWorkflow(@Auth current_user: SessionUser): util.List[UInteger] = {
    val availableWorkflowIds = context
      .select(
        WORKFLOW_USER_ACCESS.WID
      )
      .from(
        WORKFLOW_USER_ACCESS
      )
      .where(
        WORKFLOW_USER_ACCESS.UID.eq(current_user.getUid)
      )
      .fetchInto(classOf[UInteger])

    return availableWorkflowIds
  }

  @GET
  @Path("/access_files")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getAccessedFiles(@Auth current_user: SessionUser): util.List[UInteger] = {
    context
      .select(
        USER_FILE_ACCESS.FID
      )
      .from(
        USER_FILE_ACCESS
      )
      .where(
        USER_FILE_ACCESS.UID.eq(current_user.getUid)
      )
      .fetchInto(classOf[UInteger])
  }

  @GET
  @Path("/mongodb_size")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def mongoDBSize(@Auth current_user: SessionUser): Array[mongoStorage] = {
    val collectionNames = context
      .select(
        WORKFLOW_EXECUTIONS.RESULT,
        WORKFLOW.NAME,
        WORKFLOW_EXECUTIONS.EID
      )
      .from(
        WORKFLOW_EXECUTIONS
      )
      .leftJoin(
        WORKFLOW_VERSION
      )
      .on(WORKFLOW_EXECUTIONS.VID.eq(WORKFLOW_VERSION.VID))
      .leftJoin(
        WORKFLOW
      )
      .on(WORKFLOW_VERSION.WID.eq(WORKFLOW.WID))
      .where(
        WORKFLOW_EXECUTIONS.UID.eq(current_user.getUid)
          .and(WORKFLOW_EXECUTIONS.RESULT.notEqual(""))
          .and(WORKFLOW_EXECUTIONS.RESULT.isNotNull)
      )
      .fetch()

    val collections = collectionNames.map(result => {
      mongoStorage(
        result.get(WORKFLOW.NAME),
        0.0,
        getCollectionName(result.get(WORKFLOW_EXECUTIONS.RESULT)),
        result.get(WORKFLOW_EXECUTIONS.EID)
      )
    }).toList.toArray

    val collectionSizes = MongoDatabaseManager.getDatabaseSize(collections)

    collectionSizes
  }
}



