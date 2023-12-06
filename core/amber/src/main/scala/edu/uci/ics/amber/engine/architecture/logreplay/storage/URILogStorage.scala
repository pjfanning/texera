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

object URILogStorage{

  

}


class URILogStorage(fileSystemURI: URI) extends ReplayLogStorage with LazyLogging {
  var fileSystem: FileSystem = _
  val fsConf = new Configuration
  val filePath = new Path(fileSystemURI.getPath)
  // configuration for HDFS
  fsConf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "false")
  try {
    fileSystem = FileSystem.get(fileSystemURI, fsConf) // Supports various URI schemes
  } catch {
    case e: Exception =>
      logger.warn("Caught error during creating file system", e)
  }

  if (!fileSystem.exists(filePath)) {
    fileSystem.mkdirs(filePath)
  }

  override def getWriter: ReplayLogWriter = {
    new ReplayLogWriter(fileSystem.append(getLogPath))
  }

  override def getReader: ReplayLogReader = {
    val path = getLogPath
    if (hdfs.exists(path)) {
      new ReplayLogReader(() => hdfs.open(path))
    } else {
      new EmptyLogStorage().getReader
    }
  }

  override def deleteLog(): Unit = {
    // delete log if exists
    val path = getLogPath
    if (hdfs.exists(path)) {
      hdfs.delete(path, false)
    }
  }

  override def cleanPartiallyWrittenLogFile(): Unit = {
    var tmpPath = getLogPath
    tmpPath = tmpPath.suffix(".tmp")
    copyReadableLogRecords(new ReplayLogWriter(hdfs.create(tmpPath)))
    if (hdfs.exists(getLogPath)) {
      hdfs.delete(getLogPath, false)
    }
    hdfs.rename(tmpPath, getLogPath)
  }

  override def isLogAvailableForRead: Boolean = {
    if (hdfs.exists(getLogPath)) {
      val stats = hdfs.getFileStatus(getLogPath)
      stats.isFile && stats.getLen > 0
    } else {
      false
    }
  }
}
