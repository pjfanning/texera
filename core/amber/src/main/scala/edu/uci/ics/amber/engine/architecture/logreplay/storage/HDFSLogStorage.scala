package edu.uci.ics.amber.engine.architecture.logreplay.storage

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage.{
  ReplayLogReader,
  ReplayLogWriter
}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.net.URI

class HDFSLogStorage(logKey: String, hdfsIP: String) extends ReplayLogStorage with LazyLogging {
  var hdfs: FileSystem = _
  val hdfsConf = new Configuration
  hdfsConf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "false")
  try {
    hdfs = FileSystem.get(new URI(hdfsIP), hdfsConf)
  } catch {
    case e: Exception =>
      logger.warn("Caught error during creating hdfs", e)
  }
  private val recoveryLogPath: Path = new Path("/recovery-logs/" + logKey + ".logfile")
  if (!hdfs.exists(recoveryLogPath.getParent)) {
    hdfs.mkdirs(recoveryLogPath.getParent)
  }

  private def getLogPath: Path = {
    recoveryLogPath
  }

  override def getWriter: ReplayLogWriter = {
    new ReplayLogWriter(hdfs.append(getLogPath))
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

  override def isLogAvailableForRead: Boolean = {
    if (hdfs.exists(getLogPath)) {
      val stats = hdfs.getFileStatus(getLogPath)
      stats.isFile && stats.getLen > 0
    } else {
      false
    }
  }
}
