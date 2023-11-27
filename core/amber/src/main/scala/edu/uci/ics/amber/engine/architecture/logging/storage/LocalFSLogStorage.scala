package edu.uci.ics.amber.engine.architecture.logging.storage

import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage.{
  DeterminantLogReader,
  DeterminantLogWriter
}

import java.io.{DataInputStream, DataOutputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption, StandardOpenOption}

class LocalFSLogStorage(logKey: String) extends DeterminantLogStorage {

  private val recoveryLogPath: Path = Paths.get("").resolve("recovery-logs/"+logKey + ".logfile")
  if (!Files.exists(recoveryLogPath.getParent)) {
    Files.createDirectories(recoveryLogPath.getParent)
  }

  private def getLogPath: Path = {
    recoveryLogPath
  }

  override def getWriter: DeterminantLogWriter = {
    new DeterminantLogWriter(
      new DataOutputStream(
        Files.newOutputStream(
          getLogPath,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND
        )
      )
    )
  }

  override def getReader: DeterminantLogReader = {
    val path = getLogPath
    if (Files.exists(path)) {
      new DeterminantLogReader(() => new DataInputStream(Files.newInputStream(path)))
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
