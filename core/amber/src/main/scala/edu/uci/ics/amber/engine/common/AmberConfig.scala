package edu.uci.ics.amber.engine.common

import akka.actor.Address
import com.typesafe.config.{Config, ConfigFactory}
import edu.uci.ics.texera.Utils

import java.io.File
import java.net.URI

import scala.sys.process._
import java.util.logging.{Logger}


object AmberConfig {

  private val configFile: File = Utils.amberHomePath
    .resolve("src/main/resources/application.conf")
    .toFile
  private var lastModifiedTime: Long = 0
  private var conf: Config = _

  var masterNodeAddr: Address = Address("akka", "Amber", "localhost", 2552)

  // perform lazy reload
  private def getConfSource: Config = {
    if (lastModifiedTime == configFile.lastModified()) {
      conf
    } else {
      lastModifiedTime = configFile.lastModified()
      conf = ConfigFactory.parseFile(configFile)
      conf
    }
  }

  // Constants configuration
  val loggingQueueSizeInterval: Int = getConfSource.getInt("constants.logging-queue-size-interval")
  val MAX_RESOLUTION_ROWS: Int = getConfSource.getInt("constants.max-resolution-rows")
  val MAX_RESOLUTION_COLUMNS: Int = getConfSource.getInt("constants.max-resolution-columns")
  val numWorkerPerOperatorByDefault: Int = getConfSource.getInt("constants.num-worker-per-operator")
  val getStatusUpdateIntervalInMs: Long = getConfSource.getLong("constants.status-update-interval")

  // Flow control related configuration
  def maxCreditAllowedInBytesPerChannel: Long = {
    val maxCredit = getConfSource.getLong("flow-control.max-credit-allowed-in-bytes-per-channel")
    if (maxCredit == -1L) Long.MaxValue else maxCredit
  }
  val creditPollingIntervalInMs: Int =
    getConfSource.getInt("flow-control.credit-poll-interval-in-ms")

  // Network buffering configuration
  def defaultBatchSize: Int = getConfSource.getInt("network-buffering.default-batch-size")
  val enableAdaptiveNetworkBuffering: Boolean =
    getConfSource.getBoolean("network-buffering.enable-adaptive-buffering")
  val adaptiveBufferingTimeoutMs: Int =
    getConfSource.getInt("network-buffering.adaptive-buffering-timeout-ms")

  // Fries configuration
  val enableTransactionalReconfiguration: Boolean =
    getConfSource.getBoolean("reconfiguration.enable-transactional-reconfiguration")

  // Fault tolerance configuration
  val faultToleranceLogFlushIntervalInMs: Long =
    getConfSource.getLong("fault-tolerance.log-flush-interval-ms")
  val faultToleranceLogRootFolder: Option[URI] = {
    var locationStr = getConfSource.getString("fault-tolerance.log-storage-uri").trim
    if (locationStr.isEmpty) {
      None
    } else {
      if (locationStr.contains("$AMBER_FOLDER")) {
        assert(locationStr.startsWith("file"))
        locationStr =
          locationStr.replace("$AMBER_FOLDER", Utils.amberHomePath.toAbsolutePath.toString)
      }
      Some(new URI(locationStr))
    }
  }
  val isFaultToleranceEnabled: Boolean = faultToleranceLogRootFolder.nonEmpty

  // Region plan generator
  val enableCostBasedRegionPlanGenerator: Boolean =
    getConfSource.getBoolean("region-plan-generator.enable-cost-based-region-plan-generator")
  val useGlobalSearch: Boolean = getConfSource.getBoolean("region-plan-generator.use-global-search")

  // Storage configuration
  val sinkStorageMode: String = getConfSource.getString("storage.mode")
  val sinkStorageMongoDBConfig: Config = getConfSource.getConfig("storage.mongodb")
  val sinkStorageTTLInSecs: Int = getConfSource.getInt("result-cleanup.ttl-in-seconds")
  val sinkStorageCleanUpCheckIntervalInSecs: Int =
    getConfSource.getInt("result-cleanup.collection-check-interval-in-seconds")

  // User system and authentication configuration
  val isUserSystemEnabled: Boolean = getConfSource.getBoolean("user-sys.enabled")
  val jWTConfig: Config = getConfSource.getConfig("user-sys.jwt")
  val googleClientId: String = getConfSource.getString("user-sys.google.clientId")
  val gmail: String = getConfSource.getString("user-sys.google.smtp.gmail")
  val smtpPassword: String = getConfSource.getString("user-sys.google.smtp.password")

  // Web server configuration
  val operatorConsoleBufferSize: Int = getConfSource.getInt("web-server.python-console-buffer-size")
  val executionResultPollingInSecs: Int =
    getConfSource.getInt("web-server.workflow-result-pulling-in-seconds")
  val executionStateCleanUpInSecs: Int =
    getConfSource.getInt("web-server.workflow-state-cleanup-in-seconds")
  val workflowVersionCollapseIntervalInMinutes: Int =
    getConfSource.getInt("user-sys.version-time-limit-in-minutes")
  val cleanupAllExecutionResults: Boolean =
    getConfSource.getBoolean("web-server.clean-all-execution-results-on-server-start")

  // JDBC configuration
  val jdbcConfig: Config = getConfSource.getConfig("jdbc")

  // Language server configuration
  val languageServer: String = getConfSource.getString("python-language-server.provider")
  val languageServerPort: Int = getConfSource.getInt("python-language-server.port")

  //free the port for the server
  private def releasePort(port: Int): Unit = {
    val logger = Logger.getLogger("AmberConfig")
    val scriptPath = "release_port.py"
    val command = Seq("python", scriptPath, port.toString)
    val exitCode = command.!
    if (exitCode == 0) {
      logger.info(s"Successfully free the port: $port")
    } else {
      logger.warning(s"fail to free the port: $port")
    }
  }

  def startLanguageServer(): Unit = {
    val logger = Logger.getLogger("AmberConfig")
    languageServer match {
      case "pyright" =>
        logger.info("Starting Pyright...")
        releasePort(languageServerPort)
        try {
          val result = {
            // Ignore the stdout and catch the stderr
            Process("node ../languageServer/startPyright.mjs").run(ProcessLogger(_ => (), err => logger.warning(err)))
          }
          logger.info(s"Pyright language server is running on port ${languageServerPort}")
        } catch {
          case e: Exception =>
            logger.warning(s"Failed to start Pyright: ${e.getMessage}")
        }
      case "pylsp" =>
        logger.info("Starting Pylsp...")
        releasePort(3000)
        val result = Process(s"pylsp --ws --port ${languageServerPort}").run(ProcessLogger(_ => (), err => logger.warning(err)))
        logger.info(s"Python language server is running on port ${languageServerPort}")
      case _ =>
        logger.warning(s"Unknown language server: $languageServer")
    }
  }
}
