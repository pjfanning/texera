package edu.uci.ics.texera.workflow.operators.source

import edu.uci.ics.texera.web.resource.dashboard.user.dataset.version.GitVersionControl

import java.io.{FileInputStream, IOException}

class DatasetFileDesc(val filePath: String, val datasetPath: String, val versionHash: String) {
  // Initialize GitVersionControl as a member variable using datasetPath
  private val gitVersionControl = new GitVersionControl(datasetPath)

  // Method to get a FileInputStream for the current file and version
  def tempFilePath(): String = {
    try {
      gitVersionControl.writeFileContentToTempFile(versionHash, filePath)
    } catch {
      case e: IOException => throw new RuntimeException("Failed to retrieve file input stream.", e)
      case e: InterruptedException => throw new RuntimeException("Retrieval was interrupted.", e)
    }
  }

  override def toString: String = s"DatasetFileDesc(filePath=$filePath, datasetPath=$datasetPath, versionHash=$versionHash)"
}