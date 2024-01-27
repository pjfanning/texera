package edu.uci.ics.texera.web.resource.dashboard.user.environment

import edu.uci.ics.texera.Utils.withTransaction
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{DatasetOfEnvironment, Environment, EnvironmentOfWorkflow}
import edu.uci.ics.texera.web.model.jooq.generated.tables.Environment.ENVIRONMENT
import edu.uci.ics.texera.web.model.jooq.generated.tables.EnvironmentOfWorkflow.ENVIRONMENT_OF_WORKFLOW
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetOfEnvironment.DATASET_OF_ENVIRONMENT
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{DatasetOfEnvironmentDao, EnvironmentDao, EnvironmentOfWorkflowDao}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.{DatasetAccessResource, DatasetResource}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.DashboardDataset
import edu.uci.ics.texera.web.resource.dashboard.user.environment.EnvironmentResource.{DashboardEnvironment, DatasetID, DatasetOfEnvironmentAlreadyExistsMessage, EnvironmentIDs, EnvironmentNotFoundMessage, UserNoPermissionExceptionMessage, WorkflowLink, context, doesDatasetExistInEnvironment, doesUserOwnEnvironment, getEnvironmentByEid, userHasReadAccessToEnvironment, userHasWriteAccessToEnvironment, withExceptionHandling}
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowAccessResource
import io.dropwizard.auth.Auth
import io.dropwizard.servlets.assets.ResourceNotFoundException
import org.jooq.DSLContext
import org.jooq.types.UInteger

import javax.annotation.security.RolesAllowed
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{DELETE, GET, InternalServerErrorException, POST, Path, PathParam, Produces}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.asScalaBufferConverter

object EnvironmentResource {
  private def withExceptionHandling[T](block: () => T): T = {
    try {
      block()
    } catch {
      case e: Exception =>
        throw new InternalServerErrorException(
          Option(e.getMessage).getOrElse("An unknown error occurred.")
        )
    }
  }

  private val context = SqlServer.createDSLContext()

  private def getEnvironmentByEid(ctx: DSLContext, eid: UInteger): Environment = {
    val environmentDao: EnvironmentDao = new EnvironmentDao(ctx.configuration())
    val env = environmentDao.fetchOneByEid(eid)

    if (env == null) {
      throw new Exception("Environment is not found")
    }

    env
  }
  private def doesUserOwnEnvironment(ctx: DSLContext, uid: UInteger, eid: UInteger): Boolean = {
    val environment = getEnvironmentByEid(ctx, eid)
    environment.getUid == uid
  }

  private def doesDatasetExistInEnvironment(ctx: DSLContext, did: UInteger, eid: UInteger): Boolean = {
    ctx.selectCount()
      .from(DATASET_OF_ENVIRONMENT)
      .where(DATASET_OF_ENVIRONMENT.EID.eq(eid)
        .and(DATASET_OF_ENVIRONMENT.DID.eq(did)))
      .fetchOne(0, classOf[Int]) > 0
  }

  private def fetchEnvironmentIdOfWorkflow(ctx: DSLContext, wid: UInteger): Option[UInteger] = {
    val environmentOfWorkflowDao = new EnvironmentOfWorkflowDao(ctx.configuration())
    val environmentOfWorkflow = environmentOfWorkflowDao.fetchByWid(wid)
    if (environmentOfWorkflow.isEmpty) {
      None
    } else {
      Some(environmentOfWorkflow.get(0).getEid)
    }
  }

  private def fetchWorkflowIdsOfEnvironment(ctx: DSLContext, eid: UInteger): List[UInteger] = {
    val environmentOfWorkflowDao = new EnvironmentOfWorkflowDao(ctx.configuration())
    val envOfWorkflows = environmentOfWorkflowDao.fetchByEid(eid)

    // Extract wids from envOfWorkflows and collect them into a list
    val workflowIds = envOfWorkflows.asScala.map(_.getWid).toList
    workflowIds
  }
  private def userHasWriteAccessToEnvironment(ctx: DSLContext, eid: UInteger, uid: UInteger): Boolean = {
    // if user is the owner of the environment, return true
    if (doesUserOwnEnvironment(ctx, uid, eid)) {
      return true
    }

    // else, check the corresponding workflow if any, see if user has the write access to that workflow
    fetchWorkflowIdsOfEnvironment(ctx, eid).foreach(wid => {
      if (WorkflowAccessResource.hasWriteAccess(wid, uid)) {
        return true
      }
    })
    false
  }
  private def userHasReadAccessToEnvironment(ctx: DSLContext, eid: UInteger, uid: UInteger): Boolean = {
    // if user is the owner of the environment, return true
    if (doesUserOwnEnvironment(ctx, uid, eid)) {
      return true
    }

    // else, check the corresponding workflow if any, see if user has the read access to that workflow
    fetchWorkflowIdsOfEnvironment(ctx, eid).foreach(wid => {
      if (WorkflowAccessResource.hasReadAccess(wid, uid)) {
        return true
      }
    })
    false
  }

  case class DashboardEnvironment(
      environment: Environment,
      isEditable: Boolean,
  )

  case class EnvironmentIDs(eids: List[UInteger])

  case class DatasetID(did: UInteger)
  case class WorkflowLink(wid: UInteger)

  // error handling
  private val UserNoPermissionExceptionMessage = "user has no permission for the environment"
  private val EnvironmentNotFoundMessage = "environment not found"
  private val DatasetOfEnvironmentAlreadyExistsMessage =
    "the given dataset already exists in the environment"
}

@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/environment")
@Produces(Array(MediaType.APPLICATION_JSON))
class EnvironmentResource {

  @POST
  @Path("/create")
  def createEnvironment(@Auth user: SessionUser, environment: Environment): DashboardEnvironment = {
    withExceptionHandling { () =>
      {
        withTransaction(context) { ctx =>
          {
            environment.setUid(user.getUid)

            val createdEnvironment = ctx
              .insertInto(ENVIRONMENT)
              .set(ctx.newRecord(ENVIRONMENT, environment))
              .returning()
              .fetchOne()

            DashboardEnvironment(
              new Environment(
                createdEnvironment.getEid,
                createdEnvironment.getUid,
                createdEnvironment.getName,
                createdEnvironment.getDescription,
                createdEnvironment.getCreationTime
              ),
              isEditable = true
            )
          }
        }
      }
    }
  }

  @POST
  @Path("/delete")
  def deleteEnvironments(environmentIDs: EnvironmentIDs, @Auth user: SessionUser): Response = {
    val uid = user.getUid

    withExceptionHandling { () =>
      {
        withTransaction(context) { ctx =>
          val environmentDao: EnvironmentDao = new EnvironmentDao(ctx.configuration())

          for (eid <- environmentIDs.eids) {
            if (!doesUserOwnEnvironment(ctx, uid, eid)) {
              Response
                .status(Response.Status.FORBIDDEN)
                .entity(UserNoPermissionExceptionMessage)
                .build()
            }
            environmentDao.deleteById(eid)
          }

          Response.ok().build()
        }
      }
    }
  }

  @GET
  @Path("/{eid}")
  def retrieveEnvironmentByEid(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser
  ): DashboardEnvironment = {
    val uid = user.getUid
    withExceptionHandling { () =>
      {
        withTransaction(context) { ctx =>
          if (!userHasReadAccessToEnvironment(ctx, eid, uid)) {
            Response
              .status(Response.Status.FORBIDDEN)
              .entity(UserNoPermissionExceptionMessage)
              .build()
          }
          val environment = getEnvironmentByEid(ctx, eid);
          DashboardEnvironment(
            environment = environment,
            isEditable = userHasWriteAccessToEnvironment(ctx, eid, uid)
          )
        }
      }
    }
  }

  @GET
  @Path("/{eid}/dataset")
  def getDatasetsOfEnvironment(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser
  ): List[DatasetOfEnvironment] = {
    val uid = user.getUid
    withExceptionHandling(() => {
      withTransaction(context) { ctx =>
        if (!userHasReadAccessToEnvironment(ctx, eid, uid)) {
          Response
            .status(Response.Status.FORBIDDEN)
            .entity(UserNoPermissionExceptionMessage)
            .build()
        }
        val datasetOfEnvironmentDao = new DatasetOfEnvironmentDao(ctx.configuration())
        val datasetsOfEnvironment = datasetOfEnvironmentDao.fetchByEid(eid)
        datasetsOfEnvironment.toList
      }
    })
  }

  @POST
  @Path("/{eid}/dataset/add")
  def addDatasetForEnvironment(
                                @PathParam("eid") eid: UInteger,
                                @Auth user: SessionUser,
                                datasetID: DatasetID
  ): Response = {
    val uid = user.getUid

    withExceptionHandling(() => {
      withTransaction(context)(ctx => {
        val did = datasetID.did

        if (!DatasetAccessResource.userHasReadAccess(ctx, did, uid)
        || !userHasWriteAccessToEnvironment(ctx, eid, uid)) {
          Response
            .status(Response.Status.FORBIDDEN)
            .entity(UserNoPermissionExceptionMessage)
            .build()
        }

        if (doesDatasetExistInEnvironment(ctx, did, eid)) {
          Response
            .status(Response.Status.BAD_REQUEST)
            .entity(DatasetOfEnvironmentAlreadyExistsMessage)
            .build()
        }

        val datasetOfEnvironmentDao = new DatasetOfEnvironmentDao(ctx.configuration())
        val latestDatasetVersion = DatasetResource.getDatasetLatestVersion(ctx, did, uid)
        // TODO: add version to it
        datasetOfEnvironmentDao.insert(new DatasetOfEnvironment(
          did,
          eid,
          latestDatasetVersion.getDvid
        ))
        Response.status(Response.Status.OK).build()
      })
    })
  }

  @DELETE
  @Path("/{eid}/dataset/{did}")
  def removeDatasetForEnvironment(
                                   @PathParam("eid") eid: UInteger,
                                   @PathParam("did") did: UInteger,
                                   @Auth user: SessionUser
                                 ): Response = ???
  @POST
  @Path("/{eid}/linkWorkflow")
  def linkWorkflowToEnvironment(
                                 @PathParam("eid") eid: UInteger,
                                 @Auth user: SessionUser,
                                 workflowLink: WorkflowLink
                               ): Response = {
    val uid = user.getUid

    withExceptionHandling(() => {
      withTransaction(context)(ctx => {
        val wid = workflowLink.wid
        if (!userHasReadAccessToEnvironment(ctx, eid, uid)) {
          Response
            .status(Response.Status.FORBIDDEN)
            .entity(UserNoPermissionExceptionMessage)
            .build()
        }

        // Check if an entry with the specified wid already exists
        val exists = ctx.selectCount()
          .from(ENVIRONMENT_OF_WORKFLOW)
          .where(ENVIRONMENT_OF_WORKFLOW.WID.eq(wid))
          .fetchOne(0, classOf[Int]) > 0

        if (exists) {
          // Update the existing entry
          ctx.update(ENVIRONMENT_OF_WORKFLOW)
            .set(ENVIRONMENT_OF_WORKFLOW.EID, eid)
            .where(ENVIRONMENT_OF_WORKFLOW.WID.eq(wid))
            .execute()
        } else {
          // Insert a new entry
          ctx.insertInto(ENVIRONMENT_OF_WORKFLOW)
            .set(ENVIRONMENT_OF_WORKFLOW.EID, eid)
            .set(ENVIRONMENT_OF_WORKFLOW.WID, wid)
            .execute()
        }

        Response.ok().build()
      })
    })
  }
}
