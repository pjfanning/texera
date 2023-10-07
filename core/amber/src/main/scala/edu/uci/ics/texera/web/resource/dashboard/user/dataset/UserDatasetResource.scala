package edu.uci.ics.texera.web.resource.dashboard.user.dataset


import edu.uci.ics.texera.web.SqlServer
import edu.uci.ics.texera.web.model.jooq.generated.tables.daos.DatasetDao
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.Dataset
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.UserDatasetResource.{DashboardDataset, DatasetHierarchy, DatasetIDs, DatasetVersions, context, getDatasetByID, getDatasetVersionDescByIDAndName, withExceptionHandling, withTransaction}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage.{DatasetFileHierarchy, LocalFileStorage, PathUtils}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.version.{GitSharedRepoVersionControl, VersionDescriptor}
import org.glassfish.jersey.media.multipart.{FormDataMultiPart, FormDataParam}
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.types.UInteger

import java.io.{InputStream, OutputStream}
import java.util.Optional
import javax.ws.rs.{Consumes, GET, InternalServerErrorException, POST, Path, PathParam, Produces, QueryParam}
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}
import scala.collection.mutable.ListBuffer

object UserDatasetResource {

  private val context = SqlServer.createDSLContext()

  private def getDatasetByID(did: UInteger): Dataset = {
    val datasetDao = new DatasetDao(context.configuration())
    datasetDao.fetchOneByDid(did)
  }

  private def getDatasetVersionDescByIDAndName(did: UInteger, version: String): VersionDescriptor = {
    val targetDataset = getDatasetByID(did)
    val targetDatasetStoragePath = targetDataset.getStoragePath
    val datasetVersionControl = new GitSharedRepoVersionControl(targetDatasetStoragePath)
    datasetVersionControl.checkoutToVersion(version)
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

  case class DashboardDataset(dataset: Dataset)

  case class DatasetHierarchy(datasetFileHierarchy: DatasetFileHierarchy)

  case class DatasetVersions(versions: List[String])

  case class DatasetIDs(dids: List[UInteger])
}

@Produces(Array(MediaType.APPLICATION_JSON, "image/jpeg", "application/pdf"))
@Path("/dataset")
class UserDatasetResource {

  @POST
  @Path("/persist")
  def createDataset(dataset: Dataset): Response = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val datasetDao: DatasetDao = new DatasetDao(ctx.configuration())

        val datasetPath = PathUtils.getDatasetPath(dataset.getName).toString
        // init the dataset dir
        val datasetFileStorage = new LocalFileStorage(datasetPath)
        datasetFileStorage.initDir()
        // init the git version
        dataset.setStoragePath(datasetPath)
        datasetDao.insert(dataset)
        Response.ok().build()
      }
    }
  }

  @POST
  @Path("/delete")
  def deleteDataset(datasetIDs: DatasetIDs): Response = {
    withExceptionHandling { () =>
      withTransaction(context) { ctx =>
        val datasetDao = new DatasetDao(ctx.configuration())
        for (did <- datasetIDs.dids) {
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
                            multiPart: FormDataMultiPart
                          ): Response = {

    withExceptionHandling({ () =>
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
      while(fields.hasNext) {
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

  @GET
  @Path("/list")
  def getDatasetList(): List[DashboardDataset] = {
    withExceptionHandling({ () =>
      val datasetDao = new DatasetDao(context.configuration())
      val datasetListBuffer = new ListBuffer[DashboardDataset]
      val tableDatasetList = datasetDao.findAll()
      val it = tableDatasetList.iterator()
      while(it.hasNext) {
        datasetListBuffer += DashboardDataset(it.next())
      }
      print(datasetListBuffer)
      datasetListBuffer.toList
    })
  }


  @GET
  @Path("/{did}/version/list")
  def getDatasetVersionList(
                             @PathParam("did") did: UInteger): DatasetVersions = {
    withExceptionHandling({ () =>
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
  }

  @GET
  @Path("/{did}/version/{version}/hierarchy")
  def inspectDatasetFileHierarchy(
                                   @PathParam("did") did: UInteger,
                                   @PathParam("version") version: String
                                 ): DatasetHierarchy = {
    withExceptionHandling({ () => {
      val targetDataset = getDatasetByID(did)
      val targetDatasetStoragePath = targetDataset.getStoragePath
      val datasetVersionControl = new GitSharedRepoVersionControl(targetDatasetStoragePath)
      val versionDesc = datasetVersionControl.checkoutToVersion(version)

      DatasetHierarchy(new DatasetFileHierarchy(versionDesc.getVersionRepoPath))
    }
    })
  }

  @GET
  @Path("/{did}/version/{version}/file")
  def inspectDatasetSingleFile(
                                @PathParam("did") did: UInteger,
                                @PathParam("version") version: String,
                                @QueryParam("path") path: String
                              ): Response = {
    withExceptionHandling({
      () =>
        val versionDesc = getDatasetVersionDescByIDAndName(did, version)
        val versionFileStorage = new LocalFileStorage(versionDesc.getVersionRepoPath)

        val streamingOutput = new StreamingOutput() {
          override def write(output: OutputStream): Unit = {
            versionFileStorage.readFile(path, output)
          }
        }

        val contentType = path.split("\\.").lastOption match {
          case Some("jpg") | Some("jpeg") => "image/jpeg"
          case Some("png") => "image/png"
          case Some("csv") => "text/csv"
          case _ => "application/octet-stream"
        }

        Response.ok(streamingOutput).`type`(contentType).build()
    })
  }


}

