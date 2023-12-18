package edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage

import edu.uci.ics.texera.Utils
import org.jooq.types.UInteger

import java.nio.file.Path

object PathUtils {
  def getDatasetPath(did: UInteger): Path = {
    Utils.amberHomePath.resolve("user-resources").resolve("datasets").resolve(did.toString)
  }
}