package edu.uci.ics.amber.engine.common.storage.file.localfs

import edu.uci.ics.amber.engine.common.storage.TexeraURI.FILE_SCHEMA
import edu.uci.ics.amber.engine.common.storage.{StorageResource, TexeraDocument, TexeraURI}
import edu.uci.ics.amber.engine.common.storage.file.{FileTreeNode, VersionControlledCollection}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.utils.JGitVersionControl
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ObjectId, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Using

class GitVersionControlledCollection(
    val gitRepoRootURI: TexeraURI,
    val uri: TexeraURI,
    val commitHash: Option[String]
) extends VersionControlledCollection {
  require(gitRepoRootURI.getScheme == TexeraURI.FILE_SCHEMA, "Given URI should be a File URI")
  require(uri.getScheme == TexeraURI.FILE_SCHEMA, "Given URI should be a File URI")
  require(
    gitRepoRootURI == uri || gitRepoRootURI.containsChildPath(uri),
    "Given git repo URI must be equal to or be the parent of uri"
  )
  // path of the root of the version store(git repo)
  private val gitRepoPath: Path = Paths.get(gitRepoRootURI.getURI)
  private val path: Path = Paths.get(uri.getURI)

  override def getURI: TexeraURI = uri
  override def initVersionStore(): Unit = {
    if (gitRepoPath != path) {
      throw new RuntimeException("Current Collection is not able to be inited")
    }
    JGitVersionControlUtils.initRepo(path)
  }

  override def withCreateVersion(versionName: String)(operations: => Unit): String = {
    if (gitRepoPath != path) {
      throw new RuntimeException("Current Collection is not the root of the version store")
    }
    operations
    JGitVersionControlUtils.commit(gitRepoPath, versionName)
  }

  override def getDocuments: List[TexeraDocument[_]] = {
    getFileTreeNodes
      .filter(node => node.isFile && node.getAbsolutePath.getParent == path)
      .map(node => new GitVersionControlledDocument(gitRepoRootURI, TexeraURI.apply(FILE_SCHEMA, node.getAbsolutePath.toString), commitHash))
  }

  override def createDocument(name: String): TexeraDocument[_] = {
    val docURI = TexeraURI.apply(FILE_SCHEMA, path.resolve(name).toString)
    new GitVersionControlledDocument(gitRepoRootURI, docURI, commitHash)
  }

  override def rm(): Unit = {
    Using(Files.walk(path)) { paths =>
      paths.iterator().asScala
        .toSeq.reverse
        .foreach(path => Files.delete(path))
    }.get
  }

  override def getFileTreeNodes: List[FileTreeNode] = {
    JGitVersionControlUtils.getFileTreeNodesOfCommit(gitRepoPath, commitHash.get)
  }
}
