package edu.uci.ics.amber.engine.architecture.logreplay.storage

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage.{
  ReplayLogReader,
  ReplayLogWriter
}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.net.URI

import java.net.URI
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import org.slf4j.LoggerFactory


class URILogStorage(logFolderURI: URI) extends ReplayLogStorage with LazyLogging {
  var fileSystem: FileSystem = _
  val fsConf = new Configuration
  val folderPath = new Path(logFolderURI.getPath)
  // configuration for HDFS
  fsConf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "false")
  try {
    fileSystem = FileSystem.get(logFolderURI, fsConf) // Supports various URI schemes
  } catch {
    case e: Exception =>
      logger.warn("Caught error during creating file system", e)
  }

  if (!fileSystem.exists(folderPath)) {
    fileSystem.mkdirs(folderPath)
  }

  override def getWriter(logFileName:String): ReplayLogWriter = {
    new ReplayLogWriter(fileSystem.append(folderPath.suffix(logFileName)))
  }

  override def getReader(logFileName:String): ReplayLogReader = {
    val path = folderPath.suffix(logFileName)
    if (fileSystem.exists(path)) {
      new ReplayLogReader(() => fileSystem.open(path))
    } else {
      new EmptyLogStorage().getReader
    }
  }

  override def deleteFolder(): Unit = {
    // delete the entire log folder if exists
    if (!fileSystem.exists(folderPath)) {
      fileSystem.delete(folderPath, true)
    }
  }

}
