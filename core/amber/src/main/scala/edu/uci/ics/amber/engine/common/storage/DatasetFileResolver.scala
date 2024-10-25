package edu.uci.ics.amber.engine.common.storage

import edu.uci.ics.amber.engine.common.Utils.withTransaction
import edu.uci.ics.texera.web.model.jooq.generated.tables.Dataset.DATASET
import edu.uci.ics.texera.web.model.jooq.generated.tables.DatasetVersion.DATASET_VERSION
import edu.uci.ics.texera.web.model.jooq.generated.tables.User.USER
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.{Dataset, DatasetVersion}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.utils.PathUtils.getDatasetPath
import org.jooq.DSLContext

import java.net.URI
import java.nio.file.{Path, Paths}

object DatasetFileResolver {
  def isDatasetFileUri(uri: URI): Boolean = {
    uri.getScheme != null && uri.getScheme.equalsIgnoreCase("file")
  }

  private def getDatasetByNames(ctx: DSLContext, ownerEmail: String, datasetName: String): Dataset = {
    ctx
      .select(DATASET.fields: _*)
      .from(DATASET)
      .leftJoin(USER)
      .on(USER.UID.eq(DATASET.OWNER_UID))
      .where(USER.EMAIL.eq(ownerEmail))
      .and(DATASET.NAME.eq(datasetName))
      .fetchOneInto(classOf[Dataset])
  }

  private def getDatasetVersionByName(ctx: DSLContext, ownerEmail: String, datasetName: String, datasetVersionName: String): (Dataset, DatasetVersion) = {
    val dataset = getDatasetByNames(ctx, ownerEmail, datasetName)
    val datasetVersion = ctx
        .selectFrom(DATASET_VERSION)
        .where(DATASET_VERSION.DID.eq(dataset.getDid))
        .and(DATASET_VERSION.NAME.eq(datasetVersionName))
        .fetchOneInto(classOf[DatasetVersion])
    (dataset, datasetVersion)
  }

  // @param shouldContainFile a boolean flag indicating whether the path includes a fileRelativePath
  // when shouldContainFile is true, user given path is /ownerEmail/datasetName/versionName/fileRelativePath
  // e.g. /bob@texera.com/twitterDataset/v1/california/irvine/tw1.csv
  //      ownerName is bob@texera.com; datasetName is twitterDataset, versionName is v1, fileRelativePath is california/irvine/tw1.csv
  // when shouldContainFile is false, user given path is /ownerEmail/datasetName/versionName
  // e.g. /bob@texera.com/twitterDataset/v1
  //      ownerName is bob@texera.com; datasetName is twitterDataset, versionName is v1
  def resolve(ctx: DSLContext, path: Path): DatasetFileDocument = {
    val pathSegments = (0 until path.getNameCount).map(path.getName(_).toString).toArray

    if (pathSegments.length < 4) {
      throw new RuntimeException(
        s"Invalid path format. Expected format: /ownerEmail/datasetName/versionName/fileRelativePath"
      )
    }

    val ownerEmail = pathSegments(0)
    val datasetName = pathSegments(1)
    val versionName = pathSegments(2)
    val fileRelativePath = Paths.get(pathSegments.drop(3).mkString("/"))

    withTransaction(ctx) { ctx =>
      val (dataset, datasetVersion) = getDatasetVersionByName(ctx, ownerEmail, datasetName, versionName)
      if (dataset == null || datasetVersion == null) {
        throw new RuntimeException(
          "Dataset and DatasetVersion not found"
        )
      }
      val datasetPath = getDatasetPath(dataset.getDid)
      val versionHash = datasetVersion.getVersionHash
      new DatasetFileDocument(datasetPath, versionHash, fileRelativePath)
    }
  }
}
