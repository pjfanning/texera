package edu.uci.ics.texera.web.resource.dashboard.admin.user
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.enums.UserRole
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.UserDao
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User
import edu.uci.ics.texera.web.resource.dashboard.admin.user.AdminUserResource.{file, userDao, context, workflow}
import org.jasypt.util.password.StrongPasswordEncryptor
import org.jooq.types.UInteger

import java.util
import javax.annotation.security.RolesAllowed
import javax.ws.rs._
import javax.ws.rs.core.MediaType

import edu.uci.ics.texera.web.model.jooq.generated.Tables._
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object AdminUserResource {
  final private lazy val context = SqlServer.createDSLContext()
  final private lazy val userDao = new UserDao(context.configuration)

  case class file(
      user_id: UInteger,
      file_id: UInteger,
      file_name: String,
      file_size: UInteger
                 )

  case class workflow(
      user_id: UInteger,
      workflow_id: UInteger,
      workflow_name: String
                     )
}

@Path("/admin/user")
@RolesAllowed(Array("ADMIN"))
class AdminUserResource {

  /**
    * This method returns the list of users
    *
    * @return a list of users
    */
  @GET
  @Path("/list")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def listUser(): util.List[User] = {
    userDao.fetchRangeOfUid(UInteger.MIN, UInteger.MAX)
  }

  @PUT
  @Path("/update")
  def updateUser(user: User): Unit = {
    val updatedUser = userDao.fetchOneByUid(user.getUid)
    updatedUser.setName(user.getName)
    updatedUser.setEmail(user.getEmail)
    updatedUser.setRole(user.getRole)
    userDao.update(updatedUser)
  }

  @POST
  @Path("/add")
  def addUser(): Unit = {
    val random = System.currentTimeMillis().toString
    val newUser = new User
    newUser.setName("User" + random)
    newUser.setPassword(new StrongPasswordEncryptor().encryptPassword(random))
    newUser.setRole(UserRole.INACTIVE)
    userDao.insert(newUser)
  }

  @GET
  @Path("/uploaded_files")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCreatedFile(@QueryParam("user_id") user_id: UInteger): List[file] = {
    val userFileEntries = context
      .select(
        FILE.OWNER_UID,
        FILE.FID,
        FILE.NAME,
        FILE.SIZE
      )
      .from(FILE)
      .where(FILE.OWNER_UID.eq(user_id))
      .fetch()

    userFileEntries
      .map(fileRecord => {
        file(
          fileRecord.get(FILE.OWNER_UID),
          fileRecord.get(FILE.FID),
          fileRecord.get(FILE.NAME),
          fileRecord.get(FILE.SIZE)
        )
      }).toList
  }

  @GET
  @Path("/created_workflows")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getCreatedWorkflow(@QueryParam("user_id") user_id: UInteger): List[workflow] = {
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
        WORKFLOW_OF_USER.UID.eq(user_id)
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
  def getAccessedWorkflow(@QueryParam("user_id") user_id: UInteger): util.List[UInteger] = {
    val availableWorkflowIds = context
      .select(WORKFLOW_USER_ACCESS.WID)
      .from(WORKFLOW_USER_ACCESS)
      .where(WORKFLOW_USER_ACCESS.UID.eq(user_id))
      .fetchInto(classOf[UInteger])

    return availableWorkflowIds
  }

  @GET
  @Path("/access_files")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getAccessedFiles(@QueryParam("user_id") user_id: UInteger): util.List[UInteger] = {
    context
      .select(USER_FILE_ACCESS.FID)
      .from(USER_FILE_ACCESS)
      .where(USER_FILE_ACCESS.UID.eq(user_id))
      .fetchInto(classOf[UInteger])
  }
}


