package edu.uci.ics.texera.web.resource.dashboard.user.dataset

import edu.uci.ics.texera.Utils.{withExceptionHandling, withTransaction}
import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.{DatasetDao, DatasetOfUserDao, DatasetVersionDao}
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{Dataset, DatasetOfUser, DatasetVersion}
import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetOfUser.DATASET_OF_USER
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetVersion.DATASET_VERSION
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.{DashboardDataset, DatasetHierarchy, DatasetIDs, DatasetVersions, OWN, PUBLIC, READ, context, getAccessLevel, getDatasetByID, getDatasetVersionByID, getUserAccessLevelOfDataset, persistNewVersion, userAllowedToReadDataset}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.error.UserHasNoAccessToDatasetException
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage.{LocalFileStorage, PathUtils}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.version.GitVersionControl
import io.dropwizard.auth.Auth
import org.glassfish.jersey.media.multipart.{FormDataBodyPart, FormDataMultiPart, FormDataParam}
import org.jooq.DSLContext
import org.jooq.types.UInteger

import java.io.{InputStream, OutputStream}
import java.util
import java.util.{Map, Optional}
import javax.annotation.security.RolesAllowed
import javax.ws.rs.{Consumes, GET, InternalServerErrorException, POST, Path, PathParam, Produces, QueryParam}
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._


object DatasetResource {
  val PUBLIC: Byte = 1;
  val PRIVATE: Byte = 0;

  val OWN: Byte = 1;
  val WRITE: Byte = 2;
  val READ: Byte = 3;

  val FILE_OPERATION_UPLOAD_PREFIX = "file:upload:"
  val FILE_OPERATION_REMOVE_PREFIX = "file:remove"
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

  private def getDatasetByID(ctx: DSLContext, did: UInteger): Dataset = {
    val datasetDao = new DatasetDao(ctx.configuration())
    datasetDao.fetchOneByDid(did)
  }

  private def getDatasetVersionByID(
      ctx: DSLContext,
      dvid: UInteger
  ): DatasetVersion = {
    val datasetVersionDao = new DatasetVersionDao(ctx.configuration())
    datasetVersionDao.fetchOneByDvid(dvid)
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

  private def persistNewVersion(
      ctx: DSLContext,
      did: UInteger,
      versionName: String,
      multiPart: FormDataMultiPart,
                               ): DatasetVersion = {

    // TODO: consider have a lock here
    val datasetPath = PathUtils.getDatasetPath(did).toString

    val gitVersionControl = new GitVersionControl(datasetPath)
    val fileStorage = new LocalFileStorage(datasetPath)

    // for multipart, each file-related operation's key starts with file:
    // the operation is either upload or remove
    // for file:upload, the file path will be suffixed to it, e.g. file:upload:a/b/c.csv The value will be the file content
    // for file:remove, the value would be filepath1,filepath2
    val fields = multiPart.getFields().keySet().iterator()
    while (fields.hasNext) {
      val fieldName = fields.next()
      val bodyPart = multiPart.getField(fieldName)

      if (fieldName.startsWith(FILE_OPERATION_UPLOAD_PREFIX)) {
        //        val contentDisposition = bodyPart.getContentDisposition
        //        val contentType = bodyPart.getMediaType.toString
        val filePath = fieldName.substring(FILE_OPERATION_UPLOAD_PREFIX.length)
        val value: InputStream = bodyPart.getValueAs(classOf[InputStream])
        fileStorage.addFile(filePath, value)
      } else if (fieldName.startsWith(FILE_OPERATION_REMOVE_PREFIX)) {
        val filePathsValue = bodyPart.getValueAs(classOf[String])
        val filePaths = filePathsValue.split(",")
        filePaths.foreach { filePath =>
          fileStorage.removeFile(filePath.trim)
        }
      }
    }

    val commitHash = gitVersionControl.createVersion(versionName)
    val datasetVersion = new DatasetVersion()

    datasetVersion.setName(versionName)
    datasetVersion.setDid(did)
    datasetVersion.setVersionHash(commitHash)

    ctx
      .insertInto(DATASET_VERSION) // Assuming DATASET is the table reference
      .set(ctx.newRecord(DATASET_VERSION, datasetVersion))
      .returning() // Assuming ID is the primary key column
      .fetchOne().into(classOf[DatasetVersion])
  }

  case class DashboardDataset(
      dataset: Dataset,
      accessLevel: String,
      isOwner: Boolean
  )

  case class DatasetHierarchy(hierarchy: util.Map[String, AnyRef])

  case class DatasetVersions(versions: List[DatasetVersion])

  case class DatasetIDs(dids: List[UInteger])
}

@Produces(Array(MediaType.APPLICATION_JSON, "image/jpeg", "application/pdf"))
@RolesAllowed(Array("REGULAR", "ADMIN"))
@Path("/dataset")
class DatasetResource {

  @POST
  @Path("/create")
  @Consumes(Array(MediaType.MULTIPART_FORM_DATA))
  def createDataset(@Auth user: SessionUser,
                    @FormDataParam("datasetName") datasetName: String,
                    @FormDataParam("datasetDescription") datasetDescription: String,
                    @FormDataParam("isDatasetPublic") isDatasetPublic: String,
                    @FormDataParam("initialVersionName") initialVersionName: String,
                    files: FormDataMultiPart): DashboardDataset = {

    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val uid = user.getUid
        val datasetDao: DatasetDao = new DatasetDao(ctx.configuration())
        val datasetOfUserDao: DatasetOfUserDao = new DatasetOfUserDao(ctx.configuration())

        val dataset: Dataset = new Dataset()
        dataset.setName(datasetName)
        dataset.setDescription(datasetDescription)
        if (isDatasetPublic.toBoolean) {
          dataset.setIsPublic(1.toByte)
        } else {
          dataset.setIsPublic(0.toByte)
        }

        val did = dataset.getDid
        val datasetPath = PathUtils.getDatasetPath(did).toString
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

        // create the initial version of the dataset
        persistNewVersion(ctx, did, initialVersionName, files)

        datasetOfUserDao.insert(datasetOfUser)
        DashboardDataset(
          new Dataset(
            createdDataset.getDid,
            createdDataset.getName,
            createdDataset.getIsPublic,
            createdDataset.getStoragePath,
            createdDataset.getDescription,
            createdDataset.getCreationTime
          ),
          getAccessLevel(OWN),
          isOwner = true
        )
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
          val dataset = getDatasetByID(ctx, did)
          val datasetStorage = new LocalFileStorage(dataset.getStoragePath)
          datasetStorage.remove()
          datasetDao.deleteById(did)
        }

        Response.ok().build()
      }
    }
  }

  @POST
  @Path("/{did}/version/create")
  @Consumes(Array(MediaType.MULTIPART_FORM_DATA))
  def createDatasetVersion(
      @PathParam("did") did: UInteger,
      @FormDataParam("version") newVersion: String,
      @FormDataParam("remove") remove: Optional[String], // relative paths of files to be deleted
      @Auth user: SessionUser,
      multiPart: FormDataMultiPart
  ): Response = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context) { ctx =>
        val accessLevel = getUserAccessLevelOfDataset(ctx, did, uid)
        if (accessLevel.isEmpty || accessLevel.get == READ) {
          return Response
            .status(Response.Status.FORBIDDEN)
            .entity(s"You do not have permission to create new version for dataset #{$did}.")
            .build()
        }
        // create the version
        persistNewVersion(ctx, did, newVersion, multiPart)
        Response.ok().build()
      }
    })
  }

  @GET
  @Path("/{did}")
  def getDataset(
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): DashboardDataset = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context)(ctx => {
        if (!userAllowedToReadDataset(ctx, did, uid)) {
          throw new UserHasNoAccessToDatasetException(did.intValue())
        }

        val targetDataset = getDatasetByID(ctx, did)
        val userAccessLevel = getUserAccessLevelOfDataset(ctx, did, uid)

        DashboardDataset(
          targetDataset,
          getAccessLevel(userAccessLevel.get),
          userAccessLevel.get == OWN
        )
      })
    })
  }

  @GET
  @Path("/{did}/version/list")
  def getDatasetVersionList(
      @PathParam("did") did: UInteger,
      @Auth user: SessionUser
  ): DatasetVersions = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context)(ctx => {

        if (!userAllowedToReadDataset(ctx, did, uid)) {
          throw new UserHasNoAccessToDatasetException(did.intValue())
        }
        val result: java.util.List[DatasetVersion] = ctx.selectFrom(DATASET_VERSION)
          .where(DATASET_VERSION.DID.eq(did))
          .orderBy(DATASET_VERSION.CREATION_TIME.desc()) // or .asc() for ascending
          .fetchInto(classOf[DatasetVersion])

        DatasetVersions(result.asScala.toList)
      })
    })
  }

  @GET
  @Path("/{did}/version/{dvid}/hierarchy")
  def inspectDatasetFileHierarchy(
      @PathParam("did") did: UInteger,
      @PathParam("dvid") dvid: UInteger,
      @Auth user: SessionUser
  ): DatasetHierarchy = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      {
        withTransaction(context)(ctx => {
          if (!userAllowedToReadDataset(ctx, did, uid)) {
            throw new UserHasNoAccessToDatasetException(did.intValue())
          }
          val targetDataset = getDatasetByID(ctx, did)
          val targetDatasetStoragePath = targetDataset.getStoragePath

          val targetDatasetVersion = getDatasetVersionByID(ctx, dvid)
          val versionCommitHash = targetDatasetVersion.getVersionHash

          val gitVersionControl = new GitVersionControl(targetDatasetStoragePath)

          DatasetHierarchy(gitVersionControl.retrieveFileTreeOfVersion(versionCommitHash))
        })
      }
    })
  }

  @GET
  @Path("/{did}/version/{dvid}/file")
  def inspectDatasetSingleFile(
      @PathParam("did") did: UInteger,
      @PathParam("dvid") dvid: UInteger,
      @QueryParam("path") path: String,
      @Auth user: SessionUser
  ): Response = {
    val uid = user.getUid
    withExceptionHandling({ () =>
      withTransaction(context)(ctx => {
        if (!userAllowedToReadDataset(ctx, did, uid)) {
          throw new UserHasNoAccessToDatasetException(did.intValue())
        }
        val targetDataset = getDatasetByID(ctx, did)
        val targetDatasetStoragePath = targetDataset.getStoragePath

        val targetDatasetVersion = getDatasetVersionByID(ctx, dvid)
        val versionCommitHash = targetDatasetVersion.getVersionHash

        val gitVersionControl = new GitVersionControl(targetDatasetStoragePath)

        val streamingOutput = new StreamingOutput() {
          override def write(output: OutputStream): Unit = {
            gitVersionControl.retrieveFileContentOfVersion(versionCommitHash, path, output)
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
