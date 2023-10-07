package edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage

import edu.uci.ics.texera.web.resource.dashboard.user.dataset.error.DatasetAlreadyExistsException

import java.io._
import java.nio.file.{Files, Path, Paths}
import java.util.Comparator

class LocalFileStorage(baseDir: String) {



  private def getFullPath(path: String): String = s"$baseDir/$path"

  def remove(): Unit = {
    Files.walk(Paths.get(baseDir))
      .sorted(Comparator.reverseOrder[Path]())  // Provide a more explicit type here
      .forEach(Files.delete(_))
  }

  def addFile(path: String, contentStream: InputStream): Boolean = {
    try {
      val file = new File(getFullPath(path))
      println("FilePath: ", file.getPath)

      // Create any missing directories
      file.getParentFile.mkdirs()

      val outputStream = new FileOutputStream(file)

      val buffer = new Array[Byte](4096)
      var bytesRead: Int = contentStream.read(buffer)
      while (bytesRead != -1) {
        outputStream.write(buffer, 0, bytesRead)
        bytesRead = contentStream.read(buffer)
      }

      contentStream.close()
      outputStream.close()

      true
    } catch {
      case _: Exception => false
    }
  }

  def removeFile(path: String): Boolean = {
    try {
      Files.deleteIfExists(Paths.get(getFullPath(path)))
    } catch {
      case _: Exception => false
    }
  }

  def readFile(path: String, output: OutputStream): Unit = {
    val fileInputStream = new FileInputStream(getFullPath(path))
    try {
      val buffer = new Array[Byte](4096)
      var bytesRead = fileInputStream.read(buffer)
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead)
        bytesRead = fileInputStream.read(buffer)
      }
    } finally {
      fileInputStream.close()
    }
  }


  def initDir(): Unit = {
    val dirPath = Paths.get(baseDir)

    if (Files.exists(dirPath)) {
      throw new RuntimeException(s"Directory $baseDir already exists.")
    } else {
      try {
        Files.createDirectories(dirPath)
      } catch {
        case ex: Exception => throw new DatasetAlreadyExistsException(baseDir)
      }
    }
  }
}



