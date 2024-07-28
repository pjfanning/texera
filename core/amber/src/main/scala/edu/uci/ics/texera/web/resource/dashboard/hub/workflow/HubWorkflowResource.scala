package edu.uci.ics.texera.web.resource.dashboard.hub.workflow

import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.Tables._
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{User, Workflow}
import org.jooq.Record1
import org.jooq.types.UInteger

import java.util
import javax.ws.rs._
import javax.ws.rs.core.MediaType

case class PartialUser(name: String, googleAvatar: String)


@Produces(Array(MediaType.APPLICATION_JSON))
@Path("/hub/workflow")
class HubWorkflowResource {
  final private lazy val context = SqlServer.createDSLContext()

  @GET
  @Path("/list_all")
  def getAllWorkflowList: util.List[Workflow] = {
    context
      .select()
      .from(WORKFLOW)
      .fetchInto(classOf[Workflow])
  }

  @GET
  @Path("/count")
  def getWorkflowCount: Integer = {
    context.selectCount
      .from(WORKFLOW)
      .where(WORKFLOW.IS_PUBLISHED.eq(1.toByte))
      .fetchOne(0, classOf[Integer])
  }

  @GET
  @Path("/list")
  def getPublishedWorkflowList: util.List[Workflow] = {
    context
      .select()
      .from(WORKFLOW)
      .where(WORKFLOW.IS_PUBLISHED.eq(1.toByte))
      .fetchInto(classOf[Workflow])
  }

  @GET
  @Path("/owner_user")
  def getOwnerUser(@QueryParam("wid") wid: UInteger): User = {
    context
      .select(
        USER.UID,
        USER.NAME,
        USER.EMAIL,
        USER.PASSWORD,
        USER.GOOGLE_ID,
        USER.ROLE,
        USER.GOOGLE_AVATAR
      )
      .from(WORKFLOW_OF_USER)
      .join(USER).on(WORKFLOW_OF_USER.UID.eq(USER.UID))
      .where(WORKFLOW_OF_USER.WID.eq(wid))
      .fetchOneInto(classOf[User])
  }

  @GET
  @Path("/user_info")
  def getUserInfo(@QueryParam("wids") wids: java.util.List[UInteger]): java.util.Map[UInteger, PartialUser] = {
    val widsScala: Seq[UInteger] = wids.asScala.toSeq

    val records = context
      .select(WORKFLOW_OF_USER.WID, USER.NAME, USER.GOOGLE_AVATAR)
      .from(WORKFLOW_OF_USER)
      .join(USER).on(WORKFLOW_OF_USER.UID.eq(USER.UID))
      .where(WORKFLOW_OF_USER.WID.in(widsScala: _*))
      .fetch()

    val userMap = mutable.Map[UInteger, PartialUser]()
    records.forEach { record =>
      val wid = record.getValue(WORKFLOW_OF_USER.WID)
      val user = PartialUser(
        name = record.getValue(USER.NAME),
        googleAvatar = record.getValue(USER.GOOGLE_AVATAR)
      )
      userMap += (wid -> user)
    }
    userMap.asJava
  }
}
