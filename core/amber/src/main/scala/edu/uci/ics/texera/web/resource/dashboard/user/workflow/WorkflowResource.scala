package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.Tables._
import edu.uci.ics.texera.web.model.jooq.generated.enums.WorkflowUserAccessPrivilege
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{
  WorkflowDao,
  WorkflowOfProjectDao,
  WorkflowOfUserDao,
  WorkflowUserAccessDao
}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos._
import edu.uci.ics.texera.web.resource.dashboard.hub.workflow.HubWorkflowResource.recordUserActivity
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowAccessResource.hasReadAccess
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowResource._
import io.dropwizard.auth.Auth
import org.jooq.Condition
import org.jooq.impl.DSL.{groupConcatDistinct, noCondition}
import org.jooq.types.UInteger

import java.sql.Timestamp
import java.util
import java.util.UUID
import javax.annotation.security.RolesAllowed
import javax.servlet.http.HttpServletRequest
import javax.ws.rs._
import javax.ws.rs.core.{Context, MediaType}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/**
  * This file handles various request related to saved-workflows.
  * It sends mysql queries to the MysqlDB regarding the UserWorkflow Table
  * The details of UserWorkflowTable can be found in /core/scripts/sql/texera_ddl.sql
  */

object WorkflowResource {
  final private lazy val context = SqlServer.createDSLContext()
  final private lazy val workflowDao = new WorkflowDao(context.configuration)
  final private lazy val workflowOfUserDao = new WorkflowOfUserDao(
    context.configuration
  )
  final private lazy val workflowUserAccessDao = new WorkflowUserAccessDao(
    context.configuration()
  )
  final private lazy val workflowOfProjectDao = new WorkflowOfProjectDao(context.configuration)

  def getWorkflowName(wid: UInteger): String = {
    val workflow = workflowDao.fetchOneByWid(wid)
    if (workflow == null) {
      throw new NotFoundException(s"Workflow with id $wid not found")
    }
    workflow.getName
  }

  private def insertWorkflow(workflow: Workflow, user: User): Unit = {
    workflowDao.insert(workflow)
    workflowOfUserDao.insert(new WorkflowOfUser(user.getUid, workflow.getWid))
    workflowUserAccessDao.insert(
      new WorkflowUserAccess(
        user.getUid,
        workflow.getWid,
        WorkflowUserAccessPrivilege.WRITE
      )
    )
  }

  private def workflowOfUserExists(wid: UInteger, uid: UInteger): Boolean = {
    workflowOfUserDao.existsById(
      context
        .newRecord(WORKFLOW_OF_USER.UID, WORKFLOW_OF_USER.WID)
        .values(uid, wid)
    )
  }

  private def workflowOfProjectExists(wid: UInteger, pid: UInteger): Boolean = {
    workflowOfProjectDao.existsById(
      context
        .newRecord(WORKFLOW_OF_PROJECT.WID, WORKFLOW_OF_PROJECT.PID)
        .values(wid, pid)
    )
  }

  case class DashboardWorkflow(
      isOwner: Boolean,
      accessLevel: String,
      ownerName: String,
      workflow: Workflow,
      projectIDs: List[UInteger],
      ownerId: UInteger
  )

  case class WorkflowWithPrivilege(
      name: String,
      description: String,
      wid: UInteger,
      content: String,
      creationTime: Timestamp,
      lastModifiedTime: Timestamp,
      isPublished: Byte,
      readonly: Boolean
  )

  case class WorkflowIDs(wids: List[UInteger], pid: Option[UInteger])

  private def updateWorkflowField(
      workflow: Workflow,
      sessionUser: SessionUser,
      updateFunction: Workflow => Unit
  ): Unit = {
    val wid = workflow.getWid
    val user = sessionUser.getUser

    if (
      workflowOfUserExists(wid, user.getUid) || WorkflowAccessResource.hasWriteAccess(
        wid,
        user.getUid
      )
    ) {
      val userWorkflow = workflowDao.fetchOneByWid(wid)
      updateFunction(userWorkflow)
      workflowDao.update(userWorkflow)
    } else {
      throw new ForbiddenException("No sufficient access privilege.")
    }
  }

  /**
    * Updates operator IDs in the given workflow content by assigning new unique IDs.
    * Each operator ID in the "operators" section is replaced with a new ID of the form:
    * "<operatorType>-operator-<UUID>"
    *
    * @param workflowContent JSON string representing the workflow, containing operator details.
    * @return The updated workflow content with new operator IDs.
    */
  private def assignNewOperatorIds(workflowContent: String): String = {
    val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val operatorIdMap = objectMapper
      .readValue(workflowContent, classOf[Map[String, List[Map[String, String]]]])("operators")
      .map(operator => {
        val oldOperatorId = operator("operatorID")
        val operatorType = operator("operatorType")
        // operator id in frontend: operatorSchema.operatorType + "-operator-" + uuid(); // v4 = UUID.randomUUID().toString
        val newOperatorId = s"$operatorType-operator-${UUID.randomUUID()}"
        oldOperatorId -> newOperatorId
      })
      .toMap

    // replace all old operator ids with new operator ids
    operatorIdMap.foldLeft(workflowContent) {
      case (updatedContent, (oldId, newId)) =>
        updatedContent.replace(oldId, newId)
    }
  }
}

@Produces(Array(MediaType.APPLICATION_JSON))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/workflow")
class WorkflowResource extends LazyLogging {

  /**
    * This method returns all workflow IDs that the user has access to
    *
    * @return WorkflowID[]
    */
  @GET
  @Path("/user-workflow-ids")
  def retrieveIDs(@Auth user: SessionUser): util.List[String] = {
    context
      .select(WORKFLOW_USER_ACCESS.WID)
      .from(WORKFLOW_USER_ACCESS)
      .where(WORKFLOW_USER_ACCESS.UID.eq(user.getUid))
      .fetchInto(classOf[String])
  }

  /**
    * This method returns all owner user names of the workflows that the user has access to
    *
    * @return OwnerName[]
    */
  @GET
  @Path("/user-workflow-owners")
  def retrieveOwners(@Auth user: SessionUser): util.List[String] = {
    context
      .selectDistinct(USER.EMAIL)
      .from(WORKFLOW_USER_ACCESS)
      .join(WORKFLOW_OF_USER)
      .on(WORKFLOW_USER_ACCESS.WID.eq(WORKFLOW_OF_USER.WID))
      .join(USER)
      .on(WORKFLOW_OF_USER.UID.eq(USER.UID))
      .where(WORKFLOW_USER_ACCESS.UID.eq(user.getUid))
      .fetchInto(classOf[String])
  }

  /**
    * This method returns workflow IDs, that contain the selected operators, as strings
    *
    * @return WorkflowID[]
    */
  @GET
  @Path("/search-by-operators")
  def searchWorkflowByOperator(
      @QueryParam("operator") operator: String,
      @Auth sessionUser: SessionUser
  ): List[String] = {
    // Example GET url: localhost:8080/workflow/searchOperators?operator=Regex,CSVFileScan
    val user = sessionUser.getUser
    val quotes = "\""
    val operatorArray =
      operator.replace(" ", "").stripPrefix("[").stripSuffix("]").split(',')
    var orCondition: Condition = noCondition()
    for (i <- operatorArray.indices) {
      val operatorName = operatorArray(i)
      orCondition = orCondition.or(
        WORKFLOW.CONTENT
          .likeIgnoreCase(
            "%" + quotes + "operatorType" + quotes + ":" + quotes + s"$operatorName" + quotes + "%"
            //gives error when I try to combine escape character with formatted string
            //may be due to old scala version bug
          )
      )

    }

    val workflowEntries =
      context
        .select(
          WORKFLOW.WID
        )
        .from(WORKFLOW)
        .join(WORKFLOW_USER_ACCESS)
        .on(WORKFLOW_USER_ACCESS.WID.eq(WORKFLOW.WID))
        .where(
          orCondition
            .and(WORKFLOW_USER_ACCESS.UID.eq(user.getUid))
        )
        .fetch()

    workflowEntries
      .map(workflowRecord => {
        workflowRecord.into(WORKFLOW).getWid.intValue().toString
      })
      .asScala
      .toList
  }

  /**
    * This method returns the current in-session user's workflow list based on all workflows he/she has access to
    *
    * @return Workflow[]
    */
  @GET
  @Path("/list")
  def retrieveWorkflowsBySessionUser(
      @Auth sessionUser: SessionUser
  ): List[DashboardWorkflow] = {
    val user = sessionUser.getUser
    val workflowEntries = context
      .select(
        WORKFLOW.WID,
        WORKFLOW.NAME,
        WORKFLOW.DESCRIPTION,
        WORKFLOW.CREATION_TIME,
        WORKFLOW.LAST_MODIFIED_TIME,
        WORKFLOW_USER_ACCESS.PRIVILEGE,
        WORKFLOW_OF_USER.UID,
        USER.NAME,
        groupConcatDistinct(WORKFLOW_OF_PROJECT.PID).as("projects")
      )
      .from(WORKFLOW)
      .leftJoin(WORKFLOW_USER_ACCESS)
      .on(WORKFLOW_USER_ACCESS.WID.eq(WORKFLOW.WID))
      .leftJoin(WORKFLOW_OF_USER)
      .on(WORKFLOW_OF_USER.WID.eq(WORKFLOW.WID))
      .leftJoin(USER)
      .on(USER.UID.eq(WORKFLOW_OF_USER.UID))
      .leftJoin(WORKFLOW_OF_PROJECT)
      .on(WORKFLOW.WID.eq(WORKFLOW_OF_PROJECT.WID))
      .where(WORKFLOW_USER_ACCESS.UID.eq(user.getUid))
      .groupBy(WORKFLOW.WID, WORKFLOW_OF_USER.UID)
      .fetch()
    workflowEntries
      .map(workflowRecord =>
        DashboardWorkflow(
          workflowRecord.into(WORKFLOW_OF_USER).getUid.eq(user.getUid),
          workflowRecord
            .into(WORKFLOW_USER_ACCESS)
            .into(classOf[WorkflowUserAccess])
            .getPrivilege
            .toString,
          workflowRecord.into(USER).getName,
          workflowRecord.into(WORKFLOW).into(classOf[Workflow]),
          if (workflowRecord.component9() == null) List[UInteger]()
          else
            workflowRecord.component9().split(',').map(number => UInteger.valueOf(number)).toList,
          workflowRecord.into(WORKFLOW_OF_USER).getUid
        )
      )
      .asScala
      .toList
  }

  /**
    * This method handles the client request to get a specific workflow to be displayed in canvas
    * at current design, it only takes the workflowID and searches within the database for the matching workflow
    * for future design, it should also take userID as an parameter.
    *
    * @param wid     workflow id, which serves as the primary key in the UserWorkflow database
    * @return a json string representing an savedWorkflow
    */
  @GET
  @Path("/{wid}")
  def retrieveWorkflow(
      @PathParam("wid") wid: UInteger,
      @Auth user: SessionUser
  ): WorkflowWithPrivilege = {
    if (WorkflowAccessResource.hasReadAccess(wid, user.getUid)) {
      val workflow = workflowDao.fetchOneByWid(wid)
      WorkflowWithPrivilege(
        workflow.getName,
        workflow.getDescription,
        workflow.getWid,
        workflow.getContent,
        workflow.getCreationTime,
        workflow.getLastModifiedTime,
        workflow.getIsPublished,
        !WorkflowAccessResource.hasWriteAccess(wid, user.getUid)
      )
    } else {
      throw new ForbiddenException("No sufficient access privilege.")
    }
  }

  /**
    * This method persists the workflow into database
    *
    * @param workflow , a workflow
    * @return Workflow, which contains the generated wid if not provided//
    *         TODO: divide into two endpoints -> one for new-workflow and one for updating existing workflow
    *         TODO: if the persist is triggered in parallel, the none atomic actions currently might cause an issue.
    *             Should consider making the operations atomic
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Path("/persist")
  def persistWorkflow(workflow: Workflow, @Auth sessionUser: SessionUser): Workflow = {
    val user = sessionUser.getUser
    if (user == edu.uci.ics.texera.web.auth.GuestAuthFilter.GUEST) {
      throw new ForbiddenException("Guest user does not have access to db.")
    }

    if (workflowOfUserExists(workflow.getWid, user.getUid)) {
      WorkflowVersionResource.insertVersion(workflow, insertingNewWorkflow = false)
      workflowDao.update(workflow)
    } else {
      if (!WorkflowAccessResource.hasReadAccess(workflow.getWid, user.getUid)) {
        // not owner and no access record --> new record
        insertWorkflow(workflow, user)
        WorkflowVersionResource.insertVersion(workflow, insertingNewWorkflow = true)
      } else if (WorkflowAccessResource.hasWriteAccess(workflow.getWid, user.getUid)) {
        WorkflowVersionResource.insertVersion(workflow, insertingNewWorkflow = false)
        // not owner but has write access
        workflowDao.update(workflow)
      } else {
        // not owner and no write access -> rejected
        throw new ForbiddenException("No sufficient access privilege.")
      }
    }

    val wid = workflow.getWid
    workflowDao.fetchOneByWid(wid)
  }

  /**
    * This method duplicates the target workflow, the new workflow name is appended with `_copy`
    *
    * @param workflow , a workflow to be duplicated
    * @return Workflow, which contains the generated wid if not provided
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Path("/duplicate")
  def duplicateWorkflow(
      workflowIDs: WorkflowIDs,
      @Auth sessionUser: SessionUser
  ): List[DashboardWorkflow] = {

    val user = sessionUser.getUser
    // do the permission check first
    for (wid <- workflowIDs.wids) {
      if (!WorkflowAccessResource.hasReadAccess(wid, user.getUid)) {
        throw new ForbiddenException("No sufficient access privilege.")
      }
    }

    val resultWorkflows: ListBuffer[DashboardWorkflow] = ListBuffer()
    val addToProject = workflowIDs.pid.nonEmpty
    // then start a transaction and do the duplication
    try {
      context.transaction { txConfig =>
        for (wid <- workflowIDs.wids) {
          val oldWorkflow: Workflow = workflowDao.fetchOneByWid(wid)
          val newWorkflow = createWorkflow(
            new Workflow(
              oldWorkflow.getName + "_copy",
              oldWorkflow.getDescription,
              null,
              assignNewOperatorIds(oldWorkflow.getContent),
              null,
              null,
              0.toByte
            ),
            sessionUser
          )
          // if workflows also need to be added to the project
          if (addToProject) {
            val newWid = newWorkflow.workflow.getWid
            if (!hasReadAccess(newWid, user.getUid)) {
              throw new ForbiddenException("No sufficient access privilege to workflow.")
            }
            val pid = workflowIDs.pid.get
            if (!workflowOfProjectExists(newWid, pid)) {
              workflowOfProjectDao.insert(new WorkflowOfProject(newWid, pid))
            } else {
              throw new BadRequestException("Workflow already exists in the project")
            }
          }
          resultWorkflows += newWorkflow
        }
      }
    } catch {
      case _: BadRequestException | _: ForbiddenException =>
      case NonFatal(exception) =>
        throw new WebApplicationException(exception)
    }
    resultWorkflows.toList
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/clone/{wid}")
  def cloneWorkflow(
      @PathParam("wid") wid: UInteger,
      @Auth sessionUser: SessionUser,
      @Context request: HttpServletRequest
  ): UInteger = {
    val oldWorkflow: Workflow = workflowDao.fetchOneByWid(wid)
    val newWorkflow: DashboardWorkflow = createWorkflow(
      new Workflow(
        oldWorkflow.getName + "_clone",
        oldWorkflow.getDescription,
        null,
        assignNewOperatorIds(oldWorkflow.getContent),
        null,
        null,
        0.toByte
      ),
      sessionUser
    )

    recordUserActivity(request, sessionUser.getUid, wid, "clone")

    val existingCloneRecord = context
      .selectFrom(WORKFLOW_USER_CLONES)
      .where(WORKFLOW_USER_CLONES.UID.eq(sessionUser.getUid))
      .and(WORKFLOW_USER_CLONES.WID.eq(wid))
      .fetchOne()

    if (existingCloneRecord == null) {
      context
        .insertInto(WORKFLOW_USER_CLONES)
        .set(WORKFLOW_USER_CLONES.UID, sessionUser.getUid)
        .set(WORKFLOW_USER_CLONES.WID, wid)
        .execute()
    }

    newWorkflow.workflow.getWid
  }

  /**
    * This method creates and insert a new workflow to database
    *
    * @param workflow , a workflow to be created
    * @return Workflow, which contains the generated wid if not provided
    */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/create")
  def createWorkflow(workflow: Workflow, @Auth sessionUser: SessionUser): DashboardWorkflow = {
    val user = sessionUser.getUser
    if (workflow.getWid != null) {
      throw new BadRequestException("Cannot create a new workflow with a provided id.")
    } else {
      insertWorkflow(workflow, user)
      WorkflowVersionResource.insertVersion(workflow, insertingNewWorkflow = true)
      DashboardWorkflow(
        isOwner = true,
        WorkflowUserAccessPrivilege.WRITE.toString,
        user.getName,
        workflowDao.fetchOneByWid(workflow.getWid),
        List[UInteger](),
        user.getUid
      )
    }

  }

  /**
    * This method deletes the workflow from database
    *
    * @return Response, deleted - 200, not exists - 400
    */
  @POST
  @Path("/delete")
  def deleteWorkflow(workflowIDs: WorkflowIDs, @Auth sessionUser: SessionUser): Unit = {
    val user = sessionUser.getUser
    try {
      context.transaction { _ =>
        for (wid <- workflowIDs.wids) {
          if (workflowOfUserExists(wid, user.getUid)) {
            workflowDao.deleteById(wid)
          } else {
            throw new BadRequestException("The workflow does not exist.")
          }
        }
      }
    } catch {
      case _: BadRequestException =>
      case NonFatal(exception) =>
        throw new WebApplicationException(exception)
    }
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/update/name")
  def updateWorkflowName(
      workflow: Workflow,
      @Auth sessionUser: SessionUser
  ): Unit = {
    updateWorkflowField(workflow, sessionUser, _.setName(workflow.getName))
  }

  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Path("/update/description")
  def updateWorkflowDescription(
      workflow: Workflow,
      @Auth sessionUser: SessionUser
  ): Unit = {
    updateWorkflowField(workflow, sessionUser, _.setDescription(workflow.getDescription))
  }

  @PUT
  @Path("/public/{wid}")
  def makePublic(@PathParam("wid") wid: UInteger, @Auth user: SessionUser): Unit = {
    println(wid + " is public now")
    val workflow: Workflow = workflowDao.fetchOneByWid(wid)
    workflow.setIsPublished(1.toByte)
    workflowDao.update(workflow)
  }

  @PUT
  @Path("/private/{wid}")
  def makePrivate(@PathParam("wid") wid: UInteger): Unit = {
    println(wid + " is private now")
    val workflow: Workflow = workflowDao.fetchOneByWid(wid)
    workflow.setIsPublished(0.toByte)
    workflowDao.update(workflow)
  }

  @GET
  @Path("/type/{wid}")
  def getWorkflowType(@PathParam("wid") wid: UInteger): String = {
    val workflow: Workflow = workflowDao.fetchOneByWid(wid)
    if (workflow.getIsPublished() == 1.toByte)
      "Public"
    else
      "Private"
  }
}
