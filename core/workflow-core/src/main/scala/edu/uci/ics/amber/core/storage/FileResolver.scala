package edu.uci.ics.amber.core.storage

import edu.uci.ics.texera.dao.SqlServer
import edu.uci.ics.texera.dao.SqlServer.withTransaction
import edu.uci.ics.texera.dao.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.dao.jooq.generated.tables.DatasetVersion.DATASET_VERSION
import edu.uci.ics.texera.dao.jooq.generated.tables.User.USER
import edu.uci.ics.texera.dao.jooq.generated.tables.pojos.{Dataset, DatasetVersion}
import org.apache.commons.vfs2.FileNotFoundException

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.{Success, Try}

object VFSResourceType extends Enumeration {
  val RESULT: Value = Value("result")
  val MATERIALIZED_RESULT: Value = Value("materializedResult")
}

/**
  * Unified object for resolving both VFS resources and local/dataset files.
  */
object FileResolver {

  val DATASET_FILE_URI_SCHEME = "dataset"
  val LAKEFS_FILE_URI_SCHEME = "lakefs"

  /**
    * Resolves a given fileName to either a file on the local file system or a dataset file.
    *
    * @param fileName the name of the file to resolve.
    * @throws FileNotFoundException if the file cannot be resolved.
    * @return A URI pointing to the resolved file.
    */
  def resolve(fileName: String): URI = {
    if (isFileResolved(fileName)) {
      return new URI(fileName)
    }
    val resolvers: Seq[String => URI] = Seq(localResolveFunc, datasetResolveFunc, lakeFSResolveFunc)

    // Try each resolver function in sequence
    resolvers
      .map(resolver => Try(resolver(fileName)))
      .collectFirst {
        case Success(output) => output
      }
      .getOrElse(throw new FileNotFoundException(fileName))
  }

  /**
    * Attempts to resolve a local file path.
    * @throws FileNotFoundException if the local file does not exist
    * @param fileName the name of the file to check
    */
  private def localResolveFunc(fileName: String): URI = {
    val filePath = Paths.get(fileName)
    if (!Files.exists(filePath)) {
      throw new FileNotFoundException(s"Local file $fileName does not exist")
    }
    filePath.toUri
  }

  /**
    * Attempts to resolve a given fileName to a URI.
    *
    * The fileName format should be: /ownerEmail/datasetName/versionName/fileRelativePath
    *   e.g. /bob@texera.com/twitterDataset/v1/california/irvine/tw1.csv
    * The output dataset URI format is: {DATASET_FILE_URI_SCHEME}:///{did}/{versionHash}/file-path
    *   e.g. {DATASET_FILE_URI_SCHEME}:///15/adeq233td/some/dir/file.txt
    *
    * @param fileName the name of the file to attempt resolving as a DatasetFileDocument
    * @return Either[String, DatasetFileDocument] - Right(document) if creation succeeds
    * @throws FileNotFoundException if the dataset file does not exist or cannot be created
    */
  private def datasetResolveFunc(fileName: String): URI = {
    val filePath = Paths.get(fileName)
    val pathSegments = (0 until filePath.getNameCount).map(filePath.getName(_).toString).toArray

    // extract info from the user-given fileName
    val ownerEmail = pathSegments(0)
    val datasetName = pathSegments(1)
    val versionName = pathSegments(2)
    val fileRelativePath = Paths.get(pathSegments.drop(3).head, pathSegments.drop(3).tail: _*)

    // fetch the dataset and version from DB to get dataset ID and version hash
    val (dataset, datasetVersion) =
      withTransaction(
        SqlServer
          .getInstance(
            StorageConfig.jdbcUrl,
            StorageConfig.jdbcUsername,
            StorageConfig.jdbcPassword
          )
          .createDSLContext()
      ) { ctx =>
        // fetch the dataset from DB
        val dataset = ctx
          .select(DATASET.fields: _*)
          .from(DATASET)
          .leftJoin(USER)
          .on(USER.UID.eq(DATASET.OWNER_UID))
          .where(USER.EMAIL.eq(ownerEmail))
          .and(DATASET.NAME.eq(datasetName))
          .fetchOneInto(classOf[Dataset])

        // fetch the dataset version from DB
        val datasetVersion = ctx
          .selectFrom(DATASET_VERSION)
          .where(DATASET_VERSION.DID.eq(dataset.getDid))
          .and(DATASET_VERSION.NAME.eq(versionName))
          .fetchOneInto(classOf[DatasetVersion])

        if (dataset == null || datasetVersion == null) {
          throw new FileNotFoundException(s"Dataset file $fileName not found.")
        }
        (dataset, datasetVersion)
      }

    // Convert each segment of fileRelativePath to an encoded String
    val encodedFileRelativePath = fileRelativePath
      .iterator()
      .asScala
      .map { segment =>
        URLEncoder.encode(segment.toString, StandardCharsets.UTF_8)
      }
      .toArray

    // Prepend did and versionHash to the encoded path segments
    val allPathSegments = Array(
      dataset.getDid.intValue().toString,
      datasetVersion.getVersionHash
    ) ++ encodedFileRelativePath

    // Build the the format /{did}/{versionHash}/{fileRelativePath}, both Linux and Windows use forward slash as the splitter
    val uriSplitter = "/"
    val encodedPath = uriSplitter + allPathSegments.mkString(uriSplitter)

    try {
      new URI(DATASET_FILE_URI_SCHEME, "", encodedPath, null)
    } catch {
      case e: Exception =>
        throw new FileNotFoundException(s"Dataset file $fileName not found.")
    }
  }

  /**
   * Resolves a LakeFS file.
   *
   * Expected input: /repoName/commitHash/objectPath (objectPath can have nested directories)
   * Resolved as: lakefs://repoName/commitHash/objectPath
   */
  private def lakeFSResolveFunc(fileName: String): URI = {
    // Ensure the URI has the lakefs scheme
    val fullUri = if (isFileResolved(fileName)) {
      new URI(fileName)
    } else {
      new URI(s"$LAKEFS_FILE_URI_SCHEME://${fileName.stripPrefix("/")}")
    }

    // Validate the scheme
    if (fullUri.getScheme != LAKEFS_FILE_URI_SCHEME) {
      throw new FileNotFoundException(s"Invalid LakeFS scheme: ${fullUri.getScheme}")
    }

    // Split the path and extract repoName, commitHash, and objectPath
    val filePath = Paths.get(fullUri.getPath.stripPrefix("/"))
    val pathSegments = (0 until filePath.getNameCount).map(filePath.getName(_).toString).toArray

    if (pathSegments.length < 3) {
      throw new FileNotFoundException(s"Invalid LakeFS URI format: $fileName")
    }

    val repoName = pathSegments(0) // repoName
    val commitHash = pathSegments(1) // commitHash
    val objectPath = Paths.get(pathSegments.drop(2).head, pathSegments.drop(2).tail: _*).toString

    try {
      // Verify that the object exists in LakeFS
      LakeFSFileStorage.retrieveFileContent(repoName, commitHash, objectPath)
      fullUri // Return the constructed URI if the object exists
    } catch {
      case _: Exception =>
        throw new FileNotFoundException(s"LakeFS file not found: $fileName")
    }
  }

  /**
    * Checks if a given file path has a valid scheme.
    *
    * @param filePath The file path to check.
    * @return `true` if the file path contains a valid scheme, `false` otherwise.
    */
  def isFileResolved(filePath: String): Boolean = {
    try {
      val uri = new URI(filePath)
      uri.getScheme != null && uri.getScheme.nonEmpty
    } catch {
      case _: Exception => false // Invalid URI format
    }
  }
}
