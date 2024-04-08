package edu.uci.ics.amber.engine.common.storage.file.localfs

import edu.uci.ics.amber.engine.common.storage.file.FSTreeNode
import edu.uci.ics.amber.engine.common.storage.{TexeraDocument, TexeraURI}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.utils.JGitVersionControl

import java.io.{InputStream, OutputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

class GitVersionControlledDocument(val gitRepoRootURI: TexeraURI, val uri: TexeraURI, val commitHash: Option[String]) extends TexeraDocument[AnyRef] with FSTreeNode{
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
    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
    JGitVersionControl.add(gitRepoPath, path)
  }

  override def readAsOutputStream(outputStream: OutputStream): Unit = {
    if (!readonly) {
      throw new RuntimeException("File is write-only")
    }

    JGitVersionControl.readFileContentOfCommitAsOutputStream(gitRepoPath, commitHash.get, path, outputStream)
  }

  override def readAsInputStream(): InputStream = {
    if (!readonly) {
      throw new RuntimeException("File is write-only")
    }

    JGitVersionControl.readFileContentOfCommitAsInputStream(gitRepoPath, commitHash.get, path)
  }

  override def delete(): Unit = {
    JGitVersionControl.rm(gitRepoPath, path)
  }

  override def getAbsolutePath: Path = path

  override def getRelativePath: Path = gitRepoPath.relativize(path)

  override def getChildren: List[FSTreeNode] = List()
}
