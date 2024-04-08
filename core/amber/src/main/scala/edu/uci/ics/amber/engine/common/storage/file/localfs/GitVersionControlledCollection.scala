package edu.uci.ics.amber.engine.common.storage.file.localfs

import edu.uci.ics.amber.engine.common.storage.TexeraURI.FILE_SCHEMA
import edu.uci.ics.amber.engine.common.storage.{StorageResource, TexeraDocument, TexeraURI}
import edu.uci.ics.amber.engine.common.storage.file.{FSTreeNode, VersionControlledCollection}
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.utils.JGitVersionControl
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ObjectId, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

import java.net.URI
import java.nio.file.{Path, Paths}
import scala.collection.mutable.ListBuffer

class GitVersionControlledCollection(
    val gitRepoRootURI: TexeraURI,
    val uri: TexeraURI,
    val commitHash: Option[String]
) extends VersionControlledCollection {
  require(gitRepoRootURI.getScheme == TexeraURI.FILE_SCHEMA, "Given URI should be a File URI")
  require(uri.getScheme == TexeraURI.FILE_SCHEMA, "Given URI should be a File URI")
  require(
    (gitRepoRootURI == uri && commitHash.isEmpty) || (gitRepoRootURI.containsChildPath(
      uri
    ) && commitHash.isDefined),
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
    JGitVersionControl.initRepo(path)
  }

  override def withCreateVersion(versionName: String)(operations: => Unit): String = {
    if (gitRepoPath != path) {
      throw new RuntimeException("Current Collection is not able to be inited")
    }
    operations
    JGitVersionControl.commit(gitRepoPath, versionName)
  }
  override def getDocuments: List[TexeraDocument[_]] = {
    val childrenNodes = getChildren
    val documents = ListBuffer[TexeraDocument[_]]()

    childrenNodes.foreach {
      case doc: GitVersionControlledDocument => documents += doc
    }

    documents.toList
  }

  override def createDocument(name: String): TexeraDocument[_] = {
    val docURI = TexeraURI.apply(FILE_SCHEMA, path.resolve(name).toString)
    new GitVersionControlledDocument(gitRepoRootURI, docURI, commitHash)
  }

  override def getAbsolutePath: Path = path

  override def getRelativePath: Path = gitRepoPath.relativize(path)

  override def getChildren: List[FSTreeNode] = {
    val commitHash =
      this.commitHash.getOrElse(throw new IllegalStateException("Commit hash must be provided"))
    val resources = ListBuffer[FSTreeNode]()

    val repository: Repository = new FileRepositoryBuilder()
      .setGitDir(gitRepoPath.resolve(".git").toFile)
      .readEnvironment()
      .findGitDir()
      .build()

    try {
      val commitId: ObjectId = repository.resolve(commitHash)
      val walk = new RevWalk(repository)

      try {
        val commit = walk.parseCommit(commitId)
        val tree = commit.getTree

        // Adjust TreeWalk to start at the specific path within the repository
        val relativePath = gitRepoPath.relativize(path).toString
        val treeWalk = TreeWalk.forPath(repository, relativePath, tree)

        if (treeWalk != null) {
          treeWalk.setRecursive(false)

          // To list direct children, we need to check for subtree
          if (treeWalk.isSubtree) {
            treeWalk.enterSubtree()
            while (treeWalk.next()) {
              val childPathStr = treeWalk.getPathString
              val fullPath = Paths.get(gitRepoPath.toString, childPathStr)
              val isDirectory = treeWalk.isSubtree
              val texeraUri = TexeraURI.apply(FILE_SCHEMA, fullPath.toString)
              val resource: FSTreeNode = if (isDirectory) {
                // Construct GitVersionControlledCollection for directories
                new GitVersionControlledCollection(gitRepoRootURI, texeraUri, Some(commitHash))
              } else {
                // Construct GitVersionControlledDocument for files
                new GitVersionControlledDocument(gitRepoRootURI, texeraUri, Some(commitHash))
              }
              resources += resource
            }
          }
        }
      } finally {
        walk.close()
      }
    } finally {
      repository.close()
    }

    resources.toList
  }
}
