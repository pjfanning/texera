package edu.uci.ics.amber.engine.architecture.logreplay.storage

import edu.uci.ics.amber.engine.architecture.logreplay.storage.ReplayLogStorage.{
  ReplayLogReader,
  ReplayLogWriter
}

import java.io.{DataInputStream, DataOutputStream}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

class LocalFSLogStorage(logKey: String) extends ReplayLogStorage {

  private val recoveryLogPath: Path = Paths.get("").resolve("recovery-logs/" + logKey + ".logfile")
  if (!Files.exists(recoveryLogPath.getParent)) {
    Files.createDirectories(recoveryLogPath.getParent)
  }

  private def getLogPath: Path = {
    recoveryLogPath
  }

  override def getWriter: ReplayLogWriter = {
    new ReplayLogWriter(
      new DataOutputStream(
        Files.newOutputStream(
          getLogPath,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND
        )
      )
    )
  }

  override def getReader: ReplayLogReader = {
    val path = getLogPath
    if (Files.exists(path)) {
      new ReplayLogReader(() => new DataInputStream(Files.newInputStream(path)))
    } else {
      new EmptyLogStorage().getReader
    }
  }

  override def deleteLog(): Unit = {
    val path = getLogPath
    if (Files.exists(path)) {
      Files.delete(path)
    }
  }

  override def isLogAvailableForRead: Boolean = {
    if (Files.exists(getLogPath)) {
      Files.isReadable(getLogPath) && Files.size(getLogPath) > 0
    } else {
      false
    }
  }
}
