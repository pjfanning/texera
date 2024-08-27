package edu.uci.ics.texera.web.resource.languageserver

import edu.uci.ics.amber.engine.common.AmberConfig

import java.util.logging.Logger
import scala.sys.process._

object PythonLanguageServerManager {
  private val pythonLanguageServerConfig = AmberConfig.pythonLanguageServerConfig
  val pythonLanguageServerProvider: String = pythonLanguageServerConfig.getString("provider")
  val pythonLanguageServerPort: Int = pythonLanguageServerConfig.getInt("port")
  private val logger = Logger.getLogger("PythonLanguageServerManager")

  // To start the python language server based on the python-language-server provider
  def startLanguageServer(): Unit = {
    pythonLanguageServerProvider match {
      // The situation when the provider is Pyright
      case "pyright" =>
        logger.info("Starting Pyright...")
        releasePort(pythonLanguageServerPort)
        try {
          val result = {
            Process("node ../pyright-language-server/startPyright.mjs").run(
              ProcessLogger(_ => (), err => logger.warning(s"Error during Pyright startup: $err"))
            )
          }
          logger.info(s"Pyright language server is running on port $pythonLanguageServerPort")
        } catch {
          case e: Exception =>
            logger.warning(s"Failed to start Pyright: ${e.getMessage}")
        }

      // The situation when the provider is Pylsp
      case "pylsp" =>
        logger.info("Starting Pylsp...")
        releasePort(pythonLanguageServerPort)
        try {
          Process(s"pylsp --ws --port $pythonLanguageServerPort").run(
            ProcessLogger(_ => (), err => logger.warning(s"Error during Pylsp startup: $err"))
          )
          logger.info(s"Pylsp language server is running on port $pythonLanguageServerPort")
        } catch {
          case e: Exception =>
            logger.warning(s"Failed to start Pylsp: ${e.getMessage}")
        }

      case _ =>
        logger.warning(s"Unknown language server: $pythonLanguageServerPort")
    }
  }

  private def releasePort(port: Int): Unit = {
    val scriptPath =
      "../amber/src/main/scala/edu/uci/ics/texera/web/resource/pythonlanguageserver/release_port.py"
    val command = Seq("python", scriptPath, port.toString)
    val exitCode = command.!
    if (exitCode == 0) {
      logger.info(s"Successfully freed the port: $port")
    } else {
      logger.warning(s"Failed to free the port: $port")
    }
  }
}
