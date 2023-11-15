package edu.uci.ics.texera.web.resource.dashboard.user.environment

import edu.uci.ics.texera.Utils.withTransaction
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{DatasetOfEnvironment, Environment}
import edu.uci.ics.texera.web.model.jooq.generated.tables.Environment.ENVIRONMENT
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{DatasetOfEnvironmentDao, EnvironmentDao}
import edu.uci.ics.texera.web.resource.dashboard.user.environment.EnvironmentResource.{DashboardEnvironment, DashboardEnvironmentInput, EnvironmentIDs, EnvironmentNotFoundMessage, InputOfEnvironmentAlreadyExistsMessage, UserNoPermissionExceptionMessage, context, doesUserOwnEnvironment, getEnvironmentByEid, withExceptionHandling}
import io.dropwizard.auth.Auth
import org.jooq.DSLContext
import org.jooq.types.UInteger

import javax.annotation.security.RolesAllowed
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{DELETE, GET, InternalServerErrorException, POST, Path, PathParam, Produces}
import scala.collection.mutable.ListBuffer

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

  private def getEnvironmentByEid(ctx: DSLContext, eid: UInteger): Option[Environment] = {
    val environmentDao: EnvironmentDao = new EnvironmentDao(ctx.configuration())
    Option(environmentDao.fetchOneByEid(eid))
  }
  private def doesUserOwnEnvironment(ctx: DSLContext, uid: UInteger, eid: UInteger): Boolean = {
    val environment = getEnvironmentByEid(ctx, eid)
    environment match {
      case Some(env) => env.getUid == uid
      case None      => false
    }
  }

  case class DashboardEnvironment(
      environment: Environment,
      isOwner: Boolean,
      inputs: List[String],
      outputs: List[String]
  )

  case class DashboardEnvironmentInput(
      input: DatasetOfEnvironment,
      inputName: String
  )

  case class EnvironmentIDs(eids: List[UInteger])

  // error handling
  private val UserNoPermissionExceptionMessage = "user has no permission for the environment"
  private val EnvironmentNotFoundMessage = "environment not found"
  private val InputOfEnvironmentAlreadyExistsMessage =
    "the given input already exists in the environment"
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
              createdEnvironment.getUid == user.getUid,
              List(),
              List()
            )
          }
        }
      }
    }
  }

  @GET
  @Path("")
  def retrieveEnvironments(@Auth user: SessionUser): List[DashboardEnvironment] = {
    val uid = user.getUid

    withExceptionHandling { () =>
      {
        withTransaction(context) { ctx =>
          val environmentDao = new EnvironmentDao(ctx.configuration())

          val environments = environmentDao.findAll()
          val dashboardEnvironments: ListBuffer[DashboardEnvironment] = ListBuffer()

          environments.forEach(env => {
            dashboardEnvironments += DashboardEnvironment(
              environment = env,
              isOwner = env.getUid == uid,
              inputs = List(),
              outputs = List()
            )
          })

          dashboardEnvironments.toList
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
    withExceptionHandling { () =>
      {
        withTransaction(context) { ctx =>
          val inputOfEnvironmentDao = new DatasetOfEnvironmentDao(ctx.configuration())
          val environment = getEnvironmentByEid(ctx, eid);

          val inputsOfEnvironment = inputOfEnvironmentDao.fetchByEid(eid)
          val inputs: ListBuffer[String] = ListBuffer()

          inputsOfEnvironment.forEach(input => {
            inputs += ("ds" + input.getDid)
          })

          environment match {
            case Some(env) =>
              DashboardEnvironment(
                env,
                env.getUid == user.getUid,
                inputs.toList,
                List()
              )

            case None => throw new Exception(EnvironmentNotFoundMessage)
          }
        }
      }
    }
  }

  @GET
  @Path("/{eid}/input")
  def getInputsOfEnvironment(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser
  ): List[DashboardEnvironmentInput] = {
    withExceptionHandling(() => {
      withTransaction(context) { ctx =>
        val inputOfEnvironmentDao = new DatasetOfEnvironmentDao(ctx.configuration())

        val inputs = inputOfEnvironmentDao.fetchByEid(eid)
        val res = ListBuffer[DashboardEnvironmentInput]()

        inputs.forEach(input =>
          res += DashboardEnvironmentInput(
            input,
            "ds" + input.getDid
          )
        )

        res.toList
      }
    })
  }

  @GET
  @Path("/{eid}/input/{did}")
  def getInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger
  ): DashboardEnvironmentInput = ???

  @POST
  @Path("/{eid}/input/add")
  def addInputForEnvironment(
      @PathParam("eid") eid: UInteger,
      @Auth user: SessionUser,
      inputOfEnvironment: DatasetOfEnvironment
  ): Response = {
    val uid = user.getUid

    withExceptionHandling(() => {
      withTransaction(context)(ctx => {
        val environment = getEnvironmentByEid(ctx, eid)

        if (environment.isEmpty || !doesUserOwnEnvironment(ctx, uid, eid)) {
          Response
            .status(Response.Status.FORBIDDEN)
            .entity(UserNoPermissionExceptionMessage)
            .build()
        }

        val env = environment.get

        val inputOfEnvironmentDao = new DatasetOfEnvironmentDao(ctx.configuration())
        val inputs = inputOfEnvironmentDao.fetchByEid(env.getEid)

        inputs.forEach(input =>
          if (input.getDid == inputOfEnvironment.getDid) {
            Response
              .status(Response.Status.BAD_REQUEST)
              .entity(InputOfEnvironmentAlreadyExistsMessage)
              .build()
          }
        )

        inputOfEnvironmentDao.insert(inputOfEnvironment)
        Response.status(Response.Status.OK).build()
      })
    })
  }

  @POST
  @Path("/{eid}/input/{did}/update")
  def linkDatasetForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser,
      inputOfEnvironment: DatasetOfEnvironment
  ): Response = ???

  @DELETE
  @Path("/{eid}/input/{did}")
  def unlinkDatasetForEnvironment(
      @PathParam("eid") eid: UInteger,
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): Response = ???
}
