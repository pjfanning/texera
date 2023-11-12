package edu.uci.ics.texera.web.resource.dashboard.user.environment

import edu.uci.ics.texera.Utils.withTransaction
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{Environment, InputOfEnvironment}
import edu.uci.ics.texera.web.model.jooq.generated.tables.Environment.ENVIRONMENT
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.EnvironmentDao
import edu.uci.ics.texera.web.resource.dashboard.user.environment.EnvironmentResource.{
  DashboardEnvironment,
  DashboardEnvironmentInput,
  EnvironmentIDs,
  UserNoPermissionExceptionMessage,
  context,
  doesUserOwnEnvironment,
  withExceptionHandling
}
import io.dropwizard.auth.Auth
import org.jooq.DSLContext
import org.jooq.types.UInteger

import javax.annotation.security.RolesAllowed
import javax.ws.rs.core.Response
import javax.ws.rs.{DELETE, GET, InternalServerErrorException, POST, Path, PathParam}

object EnvironmentResource {
  private def withExceptionHandling[T](block: () => T): T = {
    try {
      block()
    } catch {
      case e: Exception =>
        // Optionally log the full exception here for debugging purposes
        throw new InternalServerErrorException(
          Option(e.getMessage).getOrElse("An unknown error occurred.")
        )
    }
  }

  private val context = SqlServer.createDSLContext()

  private def doesUserOwnEnvironment(ctx: DSLContext, uid: UInteger, eid: UInteger): Boolean = {
    val environmentDao: EnvironmentDao = new EnvironmentDao(ctx.configuration())

    val environment = Option(environmentDao.fetchOneByEid(eid))
    environment match {
      case Some(env) => env.getUid == uid
      case None      => false
    }
  }

  case class DashboardEnvironment(
      environment: Environment,
      isOwner: Boolean
  )

  case class DashboardEnvironmentInput(
      input: InputOfEnvironment
  )

  case class EnvironmentIDs(eids: List[UInteger])

  // error handling
  private val UserNoPermissionExceptionMessage = "user has no permission for the environment"
}

@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/environment")
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
              createdEnvironment.getUid == user.getUid
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
  def getEnvironmentByID(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser
  ): DashboardEnvironment = ???

  @GET
  @Path("/{eid}/input")
  def getInputsOfEnvironment(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser
  ): Array[DashboardEnvironmentInput] = ???

  @GET
  @Path("/{eid}/input/{did}")
  def getInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): DashboardEnvironmentInput = ???

  @POST
  @Path("/{eid}/input/add")
  def addInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser,
      inputOfEnvironment: InputOfEnvironment
  ): Response = ???

  @POST
  @Path("/{eid}/input/{did}/update")
  def updateInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser,
      inputOfEnvironment: InputOfEnvironment
  ): Response = ???

  @DELETE
  @Path("/{eid}/input/{did}")
  def deleteInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): Response = ???
}
