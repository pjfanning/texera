package edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage

import edu.uci.ics.texera.Utils
import org.jooq.types.UInteger

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.asScalaIteratorConverter

object PathUtils {

  val DATASETS_ROOT = Utils.amberHomePath.resolve("user-resources").resolve("datasets")
  def getDatasetPath(did: UInteger): Path = {
    DATASETS_ROOT.resolve(did.toString)
  }

  def getAllDatasetDirectories(): List[Path] = {
    Files.list(DATASETS_ROOT)
      .filter(Files.isDirectory(_))
      .iterator()
      .asScala
      .toList
  }
}