package edu.uci.ics.amber.engine.common.storage.file.localfs
import edu.uci.ics.amber.engine.common.storage.TexeraURI.FILE_SCHEMA
import edu.uci.ics.amber.engine.common.storage.{TexeraDocument, TexeraURI}

import java.io.{InputStream, OutputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

class GitVersionControlledDocument(val gitRepoRootURI: TexeraURI, val uri: TexeraURI, val commitHash: Option[String]) extends TexeraDocument[AnyRef] {
  require(gitRepoRootURI.getScheme == TexeraURI.FILE_SCHEMA,
    "Given URI should be a File URI")
  require(uri.getScheme == TexeraURI.FILE_SCHEMA,
    "Given URI should be a File URI")
  require(gitRepoRootURI.containsChildPath(uri),
    "Given git repo URI must be the parent of document uri")

  private val gitRepoPath: Path = Paths.get(gitRepoRootURI.getURI)
  private val path: Path = Paths.get(uri.getURI)
  private val readonly: Boolean = commitHash.isDefined

  override def getURI: TexeraURI = uri

  override def writeWithStream(inputStream: InputStream): Unit = {
    if (readonly) {
      throw new RuntimeException("File is read-only")
    }

    // Ensure the parent directory exists before copying the file
    val parentDir = path.getParent
    if (parentDir != null) {
      Files.createDirectories(parentDir)
    }

    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
    JGitVersionControlUtils.add(gitRepoPath, path)
  }

  override def readAsOutputStream(outputStream: OutputStream): Unit = {
    if (!readonly) {
      throw new RuntimeException("File is write-only")
    }

    JGitVersionControlUtils.readFileContentOfCommitAsOutputStream(gitRepoPath, commitHash.get, path, outputStream)
  }

  override def readAsInputStream(): InputStream = {
    if (!readonly) {
      throw new RuntimeException("File is write-only")
    }

    JGitVersionControlUtils.readFileContentOfCommitAsInputStream(gitRepoPath, commitHash.get, path)
  }

  override def copy(to: Option[TexeraURI]): TexeraURI = {
    to match {
      case Some(targetUri) =>
        // Extract the path from the target URI and copy the file content to it
        val targetPath = Paths.get(targetUri.getURI)
        Files.createDirectories(targetPath.getParent) // Ensure target directory exists
        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING)
        targetUri // Return the target URI as copying was to a specified location

      case None =>
        // Generate a temporary file and copy the content to it
        val tempFile = Files.createTempFile("copy", ".tmp")
        Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING)

        // Create a new TexeraURI for the temporary file and return it
        TexeraURI.apply(FILE_SCHEMA, tempFile.toString)
    }
  }

  override def rm(): Unit = {
    if (Files.isDirectory(path))
      throw new IllegalArgumentException("Provided path is a directory, not a file: " + path)

    Files.delete(path)
    JGitVersionControlUtils.rm(gitRepoPath, path)
  }
}
