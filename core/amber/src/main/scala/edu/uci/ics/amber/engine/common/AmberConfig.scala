package edu.uci.ics.amber.engine.common

import akka.actor.Address
import com.typesafe.config.{Config, ConfigFactory}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter

import java.io.File
import java.net.URI
import scala.io.Source
import scala.jdk.CollectionConverters.IteratorHasAsScala

object AmberConfig {

  private val configFile: File = Utils.amberHomePath
    .resolve("src/main/resources/application.conf")
    .toFile
  private var lastModifiedTime: Long = 0
  private var conf: Config = _

  var masterNodeAddr: Address = Address("akka", "Amber", "localhost", 2552)
  val lastDeployTimestamp:String = try {
    // Read the timestamp string from the file
    val source = Source.fromFile(Utils.amberHomePath.resolve("timestamp.txt").toString)
    val timestamp = source.getLines().mkString
    source.close()
    // Print the timestamp
    println(s"Timestamp read from file: $timestamp")
    timestamp
  } catch {
    case e: Exception =>
      println(s"An error occurred: ${e.getMessage}")
      "Timestamp unavailable"
  }

  def getLatestCommitMessageFromMaster(): String = {
    val repository = Git.open(new File(Utils.amberHomePath.getParent.getParent.toString)).getRepository

    // Walk through the commit history of the current branch
    val revWalk = new RevWalk(repository)
    val branchHead = revWalk.parseCommit(repository.resolve("HEAD"))
    revWalk.markStart(branchHead)

    // Resolve the master branch and collect its history
    val masterBranch = repository.resolve("refs/heads/master")
    val masterRevWalk = new RevWalk(repository)
    val masterCommit = masterRevWalk.parseCommit(masterBranch)
    masterRevWalk.markStart(masterCommit)

    // Collect all commit IDs from master into a set
    val masterCommits = masterRevWalk.iterator().asScala.map(_.getId).toSet
    masterRevWalk.close()

    // Find the latest merge commit involving 'master'
    val latestMergedMasterCommit = revWalk.iterator()
      .asScala
      .find { commit =>
        commit.getParentCount > 1 && // It is a merge commit
          masterCommits.contains(commit.getParent(1).getId) // Ensure the second parent is from master
      }

    val commitMessage = latestMergedMasterCommit match {
      case Some(commit) =>
        val masterParent = commit.getParent(1) // Second parent is the latest merged master commit
        val commitMessage = masterParent.getFullMessage.split("\n").head
        println(s"Latest Merged Master Commit Message: ${commitMessage}")
        commitMessage
      case None =>
        println("No commits found from 'master' branch merged into the current branch.")
        "Commit not found"
    }

    revWalk.close()
    commitMessage
  }

  val latestCommitFromMaster = getLatestCommitMessageFromMaster()

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
  def defaultDataTransferBatchSize: Int =
    getConfSource.getInt("network-buffering.default-data-transfer-batch-size")

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
  val enableCostBasedScheduleGenerator: Boolean =
    getConfSource.getBoolean("schedule-generator.enable-cost-based-schedule-generator")
  val useGlobalSearch: Boolean = getConfSource.getBoolean("schedule-generator.use-global-search")
  val useTopDownSearch: Boolean =
    getConfSource.getBoolean("schedule-generator.use-top-down-search")
  val searchTimeoutMilliseconds: Int =
    getConfSource.getInt("schedule-generator.search-timeout-milliseconds")

  // Storage configuration
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

  // Language server configuration
  val pythonLanguageServerConfig: Config = getConfSource.getConfig("python-language-server")
  // Python language server configuration
  var aiAssistantConfig: Option[Config] = None
  if (getConfSource.hasPath("ai-assistant-server")) {
    aiAssistantConfig = Some(getConfSource.getConfig("ai-assistant-server"))
  }
}
