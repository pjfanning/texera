package edu.uci.ics.amber.engine.architecture.pythonworker

import akka.actor.Props
import com.typesafe.config.{Config, ConfigFactory}
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.OpExecConfig
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.NetworkSenderActorRef
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.pythonworker.WorkerBatchInternalQueue.DataElement
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.BackpressureHandler.Backpressure
import edu.uci.ics.amber.engine.architecture.worker.{StateRestoreConfig, WorkflowWorker}
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCHandlerInitializer
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.Utils

import java.io.IOException
import java.net.ServerSocket
import java.nio.file.Path
import java.util.concurrent.{ExecutorService, Executors}
import scala.sys.process.{BasicIO, Process}

object PythonWorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef
  ): Props =
    Props(
      new PythonWorkflowWorker(
        id,
        workerIndex,
        workerLayer,
        parentNetworkCommunicationActorRef
      )
    )
}

class PythonWorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowWorker(
      actorId,
      workerIndex,
      workerLayer,
      parentNetworkCommunicationActorRef,
      false,
      StateRestoreConfig(None, None)
    ) {

  // Input/Output port used in between Python and Java processes.
  private lazy val inputPortNum: Int = getFreeLocalPort
  private lazy val outputPortNum: Int = getFreeLocalPort
  // Proxy Serve and Client
  private lazy val serverThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor
  private lazy val clientThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor
  private lazy val pythonProxyClient: PythonProxyClient =
    new PythonProxyClient(outputPortNum, actorId)
  private lazy val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  private lazy val pythonProxyServer: PythonProxyServer =
    new PythonProxyServer(inputPortNum, outputPort, actorId)

  val pythonSrcDirectory: Path = Utils.amberHomePath
    .resolve("src")
    .resolve("main")
    .resolve("python")
  val config: Config = ConfigFactory.load("python_udf")
  val pythonENVPath: String = config.getString("python.path").trim
  // Python process
  private var pythonServerProcess: Process = _

  // TODO: Implement credit calculation logic in python worker
  override def getSenderCredits(sender: ActorVirtualIdentity) = {
    Constants.unprocessedBatchesCreditLimitPerSender
  }

  override def handlePayload(channelId:(ActorVirtualIdentity, Boolean), payload: WorkflowFIFOMessagePayload): Unit = {
    val (from, _) = channelId
    payload match {
      case control: ControlPayload =>
        control match {
          case ControlInvocation(_, c) =>
            // TODO: Implement backpressure message handling for python worker
            if (!c.isInstanceOf[Backpressure]) {
              pythonProxyClient.enqueueCommand(control, from)
            }
          case ReturnInvocation(_, _) =>
            pythonProxyClient.enqueueCommand(control, from)
          case _ =>
            logger.error(s"unhandled control payload: $control")
        }
      case data: DataPayload =>
        pythonProxyClient.enqueueData(DataElement(data, from))
      case _ => ???
    }
  }

  def outputPayload(
      to: ActorVirtualIdentity,
      self: ActorVirtualIdentity,
      isData: Boolean,
      seqNum: Long,
      payload: WorkflowFIFOMessagePayload
  ): Unit = {
    val msg = WorkflowFIFOMessage(self, isData, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }

  override def postStop(): Unit = {

    try {
      // try to send shutdown command so that it can gracefully shutdown
      pythonProxyClient.close()

      clientThreadExecutor.shutdown()

      serverThreadExecutor.shutdown()

      // destroy python process
      pythonServerProcess.destroy()
    } catch {
      case e: Exception =>
        logger.error(s"$e - happened during shutdown")
    }
  }

  override def preStart(): Unit = {
    startPythonProcess()
    startProxyServer()
    startProxyClient()
  }

  private def startProxyServer(): Unit = {
    serverThreadExecutor.submit(pythonProxyServer)
  }

  private def startProxyClient(): Unit = {
    clientThreadExecutor.submit(pythonProxyClient)
  }

  private def startPythonProcess(): Unit = {
    val udfEntryScriptPath: String =
      pythonSrcDirectory.resolve("texera_run_python_worker.py").toString

    pythonServerProcess = Process(
      Seq(
        if (pythonENVPath.isEmpty) "python3"
        else pythonENVPath, // add fall back in case of empty
        "-u",
        udfEntryScriptPath,
        Integer.toString(outputPortNum),
        Integer.toString(inputPortNum),
        config.getString("python.log.streamHandler.level")
      )
    ).run(BasicIO.standard(false))
  }

  /**
    * Get a random free port.
    *
    * @return The port number.
    * @throws IOException, might happen when getting a free port.
    */
  @throws[IOException]
  private def getFreeLocalPort: Int = {
    var s: ServerSocket = null
    try {
      // ServerSocket(0) results in availability of a free random port
      s = new ServerSocket(0)
      s.getLocalPort
    } catch {
      case e: Exception =>
        throw new RuntimeException(e)
    } finally {
      assert(s != null)
      s.close()
    }
  }
}
