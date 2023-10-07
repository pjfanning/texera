package edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage

import edu.uci.ics.texera.Utils

import java.nio.file.Path

object PathUtils {
  def getDatasetPath(did: String): Path = {
    Utils.amberHomePath.resolve("user-resources").resolve("datasets").resolve(did)
  }
}