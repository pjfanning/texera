package edu.uci.ics.texera.web.resource.dashboard.user.dataset

import edu.uci.ics.texera.Utils.withTransaction
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.common.AccessEntry
import edu.uci.ics.texera.web.model.jooq.generated.Tables.USER
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetUserAccess.DATASET_USER_ACCESS
import edu.uci.ics.texera.web.model.jooq.generated.enums.DatasetUserAccessPrivilege
import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{DatasetDao, DatasetUserAccessDao, UserDao}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{Dataset, DatasetUserAccess, User}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetAccessResource.{context, getOwner}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.{PUBLIC, withExceptionHandling}
import org.jooq.DSLContext
import org.jooq.types.UInteger

import java.util
import javax.annotation.security.RolesAllowed
import javax.ws.rs.{BadRequestException, DELETE, GET, InternalServerErrorException, PUT, Path, PathParam, Produces}
import javax.ws.rs.core.{MediaType, Response}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object DatasetAccessResource {
  final private lazy val context = SqlServer.createDSLContext()

  def userHasReadAccess(ctx: DSLContext, did: UInteger, uid: UInteger): Boolean = {
    val userAccessible = ctx
      .select()
      .from(DATASET)
      .leftJoin(DATASET_USER_ACCESS)
      .on(DATASET.DID.eq(DATASET_USER_ACCESS.DID))
      .where(
        DATASET.DID
          .eq(did)
          .and(
            DATASET.IS_PUBLIC
              .eq(PUBLIC)
              .or(DATASET.OWNER_UID.eq(uid))
              .or(DATASET_USER_ACCESS.UID.eq(uid))
          )
      )
      .fetchInto(classOf[Dataset])

    userAccessible.nonEmpty
  }

  def userOwnDataset(ctx: DSLContext, did: UInteger, uid: UInteger): Boolean = {
    val record = ctx
      .selectFrom(DATASET)
      .where(DATASET.DID.eq(did))
      .and(DATASET.OWNER_UID.eq(uid))
      .fetchOne()

    record != null
  }

  def userHasWriteAccess(ctx: DSLContext, did: UInteger, uid: UInteger): Boolean = {
    getDatasetUserAccessPrivilege(ctx, did, uid).eq(
      DatasetUserAccessPrivilege.WRITE
    ) || userOwnDataset(ctx, did, uid)
  }
  def getDatasetUserAccessPrivilege(
      ctx: DSLContext,
      did: UInteger,
      uid: UInteger
  ): DatasetUserAccessPrivilege = {
    val record = ctx
      .selectFrom(DATASET_USER_ACCESS)
      .where(DATASET_USER_ACCESS.DID.eq(did))
      .and(DATASET_USER_ACCESS.UID.eq(uid))
      .fetchOne()

    if (record == null)
      DatasetUserAccessPrivilege.NONE
    else
      record.getPrivilege
  }

  def getOwner(ctx: DSLContext, did: UInteger): User = {
    val datasetDao = new DatasetDao(ctx.configuration())
    val userDao = new UserDao(ctx.configuration())
    userDao.fetchOneByUid(datasetDao.fetchOneByDid(did).getOwnerUid)
  }

  def withExceptionHandling[T](block: () => T): T = {
    try {
      block()
    } catch {
      case e: BadRequestException =>
        throw e
      case e: Exception =>
        // Optionally log the full exception here for debugging purposes
        println(e)
        throw new InternalServerErrorException(
          Option(e.getMessage).getOrElse("An unknown error occurred.")
        )
    }
  }
}

@Produces(Array(MediaType.APPLICATION_JSON))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/access/dataset")
class DatasetAccessResource {

  /**
    * This method returns the owner of a dataset
    *
    * @param did ,  dataset id
    * @return ownerEmail,  the owner's email
    */
  @GET
  @Path("/owner/{did}")
  def getOwnerEmailOfDataset(@PathParam("did") did: UInteger): String = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val owner = getOwner(ctx, did)
        if (owner != null) {
          return owner.getEmail
        }
        // this should not happen based on the foreign key constrains
        return ""
      }
    }
  }

  /**
    * Returns information about all current shared access of the given dataset
    *
    * @param did dataset id
    * @return a List of email/name/permission
    */
  @GET
  @Path("/list/{did}")
  def getAccessList(
      @PathParam("did") did: UInteger
  ): util.List[AccessEntry] = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val datasetDao = new DatasetDao(ctx.configuration())
        ctx
          .select(
            USER.EMAIL,
            USER.NAME,
            DATASET_USER_ACCESS.PRIVILEGE
          )
          .from(DATASET_USER_ACCESS)
          .join(USER)
          .on(USER.UID.eq(DATASET_USER_ACCESS.UID))
          .where(
            DATASET_USER_ACCESS.DID
              .eq(did)
              .and(DATASET_USER_ACCESS.UID.notEqual(datasetDao.fetchOneByDid(did).getOwnerUid))
          )
          .fetchInto(classOf[AccessEntry])
      }
    }
  }

  /**
    * This method shares a dataset to a user with a specific access type
    *
    * @param did       the given dataset
    * @param email     the email which the access is given to
    * @param privilege the type of Access given to the target user
    * @return rejection if user not permitted to share the workflow or Success Message
    */
  @PUT
  @Path("/grant/{did}/{email}/{privilege}")
  def grantAccess(
      @PathParam("did") did: UInteger,
      @PathParam("email") email: String,
      @PathParam("privilege") privilege: String
  ): Response = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val datasetUserAccessDao = new DatasetUserAccessDao(ctx.configuration())
        val userDao = new UserDao(ctx.configuration())
        datasetUserAccessDao.merge(
          new DatasetUserAccess(
            did,
            userDao.fetchOneByEmail(email).getUid,
            DatasetUserAccessPrivilege.valueOf(privilege)
          )
        )
        Response.ok().build()
      }
    }
  }

  /**
    * This method revoke the user's access of the given dataset
    *
    * @param did   the given dataset
    * @param email the email of the use whose access is about to be removed
    * @return message indicating a success message
    */
  @DELETE
  @Path("/revoke/{did}/{email}")
  def revokeAccess(
      @PathParam("did") did: UInteger,
      @PathParam("email") email: String
  ): Response = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val userDao = new UserDao(ctx.configuration())

        ctx
          .delete(DATASET_USER_ACCESS)
          .where(
            DATASET_USER_ACCESS.UID
              .eq(userDao.fetchOneByEmail(email).getUid)
              .and(DATASET_USER_ACCESS.DID.eq(did))
          )
          .execute()

        Response.ok().build()
      }
    }
  }
}
