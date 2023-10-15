package edu.uci.ics.texera.web.resource.dashboard.user.dataset

import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{DatasetDao, DatasetOfUserDao}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{Dataset, DatasetOfUser}
import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetOfUser.DATASET_OF_USER
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.{DashboardDataset, DatasetHierarchy, DatasetIDs, DatasetVersions, OWN, PUBLIC, READ, context, getAccessLevel, getDatasetByID, getDatasetVersionDescByIDAndName, getUserAccessLevelOfDataset, userAllowedToReadDataset, withExceptionHandling, withTransaction}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.error.UserHasNoAccessToDatasetException
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage.{DatasetFileHierarchy, LocalFileStorage, PathUtils}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.version.{GitSharedRepoVersionControl, VersionDescriptor}
import io.dropwizard.auth.Auth
import org.glassfish.jersey.media.multipart.{FormDataMultiPart, FormDataParam}
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.types.UInteger

import java.io.{InputStream, OutputStream}
import java.util.Optional
import javax.annotation.security.RolesAllowed
import javax.ws.rs.{Consumes, GET, InternalServerErrorException, POST, Path, PathParam, Produces, QueryParam}
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.ListBuffer

object DatasetResource {
  val PUBLIC: Byte = 1;
  val PRIVATE: Byte = 0;

  val OWN: Byte = 1;
  val WRITE: Byte = 2;
  val READ: Byte = 3;

  def getAccessLevel(level: Byte): String = {
    if (level == OWN) {
      "Own"
    } else if (level == WRITE) {
      "Write"
    } else {
      "Read"
    }
  }

  private val context = SqlServer.createDSLContext()

  private def getDatasetByID(did: UInteger): Dataset = {
    val datasetDao = new DatasetDao(context.configuration())
    datasetDao.fetchOneByDid(did)
  }

  private def getDatasetVersionDescByIDAndName(
      did: UInteger,
      version: String
  ): VersionDescriptor = {
    val targetDataset = getDatasetByID(did)
    val targetDatasetStoragePath = targetDataset.getStoragePath
    val datasetVersionControl = new GitSharedRepoVersionControl(targetDatasetStoragePath)
    datasetVersionControl.checkoutToVersion(version)
  }

  private def getUserAccessLevelOfDataset(
      ctx: DSLContext,
      did: UInteger,
      uid: UInteger
  ): Option[Byte] = {
    val ownerRecord = ctx
      .selectFrom(DATASET_OF_USER)
      .where(DATASET_OF_USER.DID.eq(did))
      .and(DATASET_OF_USER.UID.eq(uid))
      .fetchOne()

    if (ownerRecord == null)
      None
    else
      Some(ownerRecord.getAccessLevel)
  }

  private def userAllowedToReadDataset(ctx: DSLContext, did: UInteger, uid: UInteger): Boolean = {
    val userAccessible = ctx
      .select()
      .from(DATASET)
      .leftJoin(DATASET_OF_USER)
      .on(DATASET.DID.eq(DATASET_OF_USER.DID))
      .where(
        DATASET.DID
          .eq(did)
          .and(
            DATASET.IS_PUBLIC
              .eq(PUBLIC)
              .or(DATASET_OF_USER.UID.eq(uid))
          )
      )
      .fetchInto(classOf[Dataset])

    userAccessible.nonEmpty
  }

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

  private def withTransaction[T](dsl: DSLContext)(block: DSLContext => T): T = {
    var result: Option[T] = None

    dsl.transaction(configuration => {
      val ctx = DSL.using(configuration)
      result = Some(block(ctx))
    })

    result.getOrElse(throw new RuntimeException("Transaction failed without result!"))
  }

  case class DashboardDataset(
                               dataset: Dataset,
                               accessLevel: String,
                               isOwner: Boolean,
                             )

  case class DatasetHierarchy(datasetFileHierarchy: DatasetFileHierarchy)

  case class DatasetVersions(versions: List[String])

  case class DatasetIDs(dids: List[UInteger])
}

@Produces(Array(MediaType.APPLICATION_JSON, "image/jpeg", "application/pdf"))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/dataset")
class DatasetResource {

  @POST
  @Path("/create")
  def createDataset(@Auth user: SessionUser, dataset: Dataset): DashboardDataset = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val uid = user.getUid
        val datasetDao: DatasetDao = new DatasetDao(ctx.configuration())
        val datasetOfUserDao: DatasetOfUserDao = new DatasetOfUserDao(ctx.configuration())

        val datasetPath = PathUtils.getDatasetPath(dataset.getName).toString
        // init the dataset dir
        val datasetFileStorage = new LocalFileStorage(datasetPath)
        datasetFileStorage.initDir()
        dataset.setStoragePath(datasetPath)

        val createdDataset = ctx
          .insertInto(DATASET) // Assuming DATASET is the table reference
          .set(ctx.newRecord(DATASET, dataset))
          .returning() // Assuming ID is the primary key column
          .fetchOne()

        val datasetOfUser = new DatasetOfUser()
        datasetOfUser.setDid(createdDataset.getDid)
        datasetOfUser.setUid(uid)
        datasetOfUser.setAccessLevel(OWN)

        datasetOfUserDao.insert(datasetOfUser)
        DashboardDataset(
          new Dataset(createdDataset.getDid, createdDataset.getName, createdDataset.getIsPublic, createdDataset.getStoragePath, createdDataset.getDescription, createdDataset.getCreationTime),
          getAccessLevel(OWN),
          isOwner = true)
      }
    }
  }

  @POST
  @Path("/delete")
  def deleteDataset(datasetIDs: DatasetIDs, @Auth user: SessionUser): Response = {
    val uid = user.getUid
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val datasetDao = new DatasetDao(ctx.configuration())
        for (did <- datasetIDs.dids) {
          val accessLevel = getUserAccessLevelOfDataset(ctx, did, uid)

          if (accessLevel.isEmpty || accessLevel.get != OWN) {
            // throw the exception that user has no access to certain dataset
            return Response
              .status(Response.Status.FORBIDDEN)
              .entity(s"You do not have permission to delete dataset #{$did}.")
              .build()
          }
          val dataset = getDatasetByID(did)
          val datasetStorage = new LocalFileStorage(dataset.getStoragePath)
          datasetStorage.remove()
          datasetDao.deleteById(did)
        }

        Response.ok().build()
      }
    }
  }

  @POST
  @Path("/{did}/version/persist")
  @Consumes(Array(MediaType.MULTIPART_FORM_DATA))
  def createDatasetVersion(
      @PathParam("did") did: UInteger,
      @FormDataParam("baseVersion") baseVersion: Optional[String],
      @FormDataParam("version") newVersion: String,
      @FormDataParam("remove") remove: Optional[String], // relative paths of files to be deleted
      @Auth user: SessionUser,
      multiPart: FormDataMultiPart
  ): Response = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      val accessLevel = getUserAccessLevelOfDataset(context, did, uid)
      if (accessLevel.isEmpty || accessLevel.get == READ) {
        return Response
          .status(Response.Status.FORBIDDEN)
          .entity(s"You do not have permission to create new version for dataset #{$did}.")
          .build()
      }

      val targetDataset = getDatasetByID(did)
      val targetDatasetStoragePath = targetDataset.getStoragePath

      // then, initialize file storage and version control object
      val datasetVersionControl = new GitSharedRepoVersionControl(targetDatasetStoragePath)

      // create the version
      datasetVersionControl.createVersion(newVersion, baseVersion)
      val versionDescriptor = datasetVersionControl.checkoutToVersion(newVersion)
      val versionFileStorage = new LocalFileStorage(versionDescriptor.getVersionRepoPath)

      if (remove.isPresent) {
        val fileRemovals: List[String] = remove.get().split(",").toList
        for (filePath <- fileRemovals) {
          versionFileStorage.removeFile(filePath)
        }
      }

      // then process the newly uploaded file
      val fields = multiPart.getFields().keySet().iterator()
      while (fields.hasNext) {
        val fieldName = fields.next()
        val bodyPart = multiPart.getField(fieldName)

        if (fieldName != "remove" && fieldName != "version" && fieldName != "baseVersion") {
          //        val contentDisposition = bodyPart.getContentDisposition
          //        val contentType = bodyPart.getMediaType.toString
          val value: InputStream = bodyPart.getValueAs(classOf[InputStream])
          versionFileStorage.addFile(fieldName, value)
        }
      }

      // then commit the changes
      datasetVersionControl.commitVersion(newVersion)
      Response.ok().build()
    })
  }

//  @GET
//  @Path("/list")
//  def getDatasetList(
//      @Auth user: SessionUser
//  ): List[DashboardDataset] = {
//    val uid = user.getUid
//    withExceptionHandling({ () =>
//      withTransaction(context)(ctx => {
//        // Fetch datasets either public or accessible to the user
//        val datasets = ctx
//          .select()
//          .from(DATASET)
//          .leftJoin(DATASET_OF_USER)
//          .on(DATASET.DID.eq(DATASET_OF_USER.DID))
//          .where(
//            DATASET.IS_PUBLIC
//              .eq(PUBLIC) // Assuming PUBLIC is a constant representing '1' or true
//              .or(DATASET_OF_USER.UID.eq(uid))
//          )
//          .fetchInto(
//            classOf[Dataset]
//          ) // Assuming Dataset is the jOOQ generated class for your table
//
//        // Transform datasets into DashboardDataset objects
//        val datasetList = datasets.map(d => DashboardDataset(d)).toList
//        datasetList
//      })
//    })
//  }

  @GET
  @Path("/{did}/version/list")
  def getDatasetVersionList(
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): DatasetVersions = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context)((ctx) => {

        if (!userAllowedToReadDataset(ctx, did, uid)) {
          throw new UserHasNoAccessToDatasetException(did.intValue())
        }
        // first, query the db to get the storage path of the target dataset
        val targetDataset = getDatasetByID(did)
        val targetDatasetStoragePath = targetDataset.getStoragePath

        val datasetVersions = new GitSharedRepoVersionControl(targetDatasetStoragePath)
        val versionsIte = datasetVersions.listVersions().iterator()
        val resultListBuffer: ListBuffer[String] = ListBuffer()

        while (versionsIte.hasNext) {
          resultListBuffer += versionsIte.next()
        }

        DatasetVersions(resultListBuffer.toList)
      })
    })
  }

  @GET
  @Path("/{did}/version/{version}/hierarchy")
  def inspectDatasetFileHierarchy(
      @PathParam("did") did: UInteger,
      @PathParam("version") version: String,
      @Auth user: SessionUser
  ): DatasetHierarchy = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      {
        withTransaction(context)(ctx => {
          if (!userAllowedToReadDataset(ctx, did, uid)) {
            throw new UserHasNoAccessToDatasetException(did.intValue())
          }
          val targetDataset = getDatasetByID(did)
          val targetDatasetStoragePath = targetDataset.getStoragePath
          val datasetVersionControl = new GitSharedRepoVersionControl(targetDatasetStoragePath)
          val versionDesc = datasetVersionControl.checkoutToVersion(version)

          DatasetHierarchy(new DatasetFileHierarchy(versionDesc.getVersionRepoPath))
        })
      }
    })
  }

  @GET
  @Path("/{did}/version/{version}/file")
  def inspectDatasetSingleFile(
      @PathParam("did") did: UInteger,
      @PathParam("version") version: String,
      @QueryParam("path") path: String,
      @Auth user: SessionUser
  ): Response = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context)(ctx => {
        if (!userAllowedToReadDataset(ctx, did, uid)) {
          throw new UserHasNoAccessToDatasetException(did.intValue())
        }
        val versionDesc = getDatasetVersionDescByIDAndName(did, version)
        val versionFileStorage = new LocalFileStorage(versionDesc.getVersionRepoPath)

        val streamingOutput = new StreamingOutput() {
          override def write(output: OutputStream): Unit = {
            versionFileStorage.readFile(path, output)
          }
        }

        val contentType = path.split("\\.").lastOption match {
          case Some("jpg") | Some("jpeg") => "image/jpeg"
          case Some("png")                => "image/png"
          case Some("csv")                => "text/csv"
          case _                          => "application/octet-stream"
        }

        Response.ok(streamingOutput).`type`(contentType).build()
      })
    })
  }
}
