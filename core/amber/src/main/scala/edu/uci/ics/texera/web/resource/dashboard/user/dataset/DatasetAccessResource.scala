package edu.uci.ics.texera.web.resource.dashboard.user.dataset

import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetUserAccess.DATASET_USER_ACCESS
import edu.uci.ics.texera.web.model.jooq.generated.enums.DatasetUserAccessPrivilege
import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.PUBLIC
import org.jooq.DSLContext
import org.jooq.types.UInteger

import javax.annotation.security.RolesAllowed
import javax.ws.rs.{BadRequestException, InternalServerErrorException, Path, Produces}
import javax.ws.rs.core.MediaType
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

object DatasetAccessResource {

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
              .or(DATASET_USER_ACCESS.UID.eq(uid))
          )
      )
      .fetchInto(classOf[Dataset])

    userAccessible.nonEmpty
  }

  def userHasWriteAccess(ctx: DSLContext, did: UInteger, uid: UInteger): Boolean = {
    getDatasetUserAccessPrivilege(ctx, did, uid).eq(DatasetUserAccessPrivilege.WRITE)
  }

  def getDatasetUserAccessPrivilege(ctx: DSLContext, did: UInteger, uid: UInteger): DatasetUserAccessPrivilege = {
    val ownerRecord = ctx
      .selectFrom(DATASET_USER_ACCESS)
      .where(DATASET_USER_ACCESS.DID.eq(did))
      .and(DATASET_USER_ACCESS.UID.eq(uid))
      .fetchOne()

    if (ownerRecord == null)
      DatasetUserAccessPrivilege.NONE
    else
      ownerRecord.getPrivilege
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
class DatasetAccessResource {}
