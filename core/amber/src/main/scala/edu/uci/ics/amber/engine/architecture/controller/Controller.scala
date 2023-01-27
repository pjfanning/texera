package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{Address, Cancellable, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{AdditionalOperatorInfo, WorkflowRecoveryStatus}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, InMemDeterminant, ProcessControlMessage, SenderActorChange}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort
import edu.uci.ics.amber.engine.architecture.recovery.{GlobalRecoveryManager, RecoveryQueue}
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.{ControlElement, InputTuple}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker
import edu.uci.ics.amber.engine.common.{AmberUtils, Constants}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object ControllerConfig {
  def default: ControllerConfig =
    ControllerConfig(
      monitoringIntervalMs = Option(Constants.monitoringIntervalInMs),
      skewDetectionIntervalMs = Option(Constants.reshapeSkewDetectionIntervalInMs),
      statusUpdateIntervalMs =
        Option(AmberUtils.amberConfig.getLong("constants.status-update-interval")),
      AmberUtils.amberConfig.getBoolean("fault-tolerance.enable-determinant-logging"),
      Map.empty
    )
}

final case class ControllerConfig(
    monitoringIntervalMs: Option[Long],
    skewDetectionIntervalMs: Option[Long],
    statusUpdateIntervalMs: Option[Long],
    var supportFaultTolerance: Boolean,
    var replayRequest: Map[ActorVirtualIdentity, Long]
)

object Controller {

  def props(
      workflow: Workflow,
      controllerConfig: ControllerConfig = ControllerConfig.default,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef = NetworkSenderActorRef()
  ): Props =
    Props(
      new Controller(
        workflow,
        controllerConfig,
        parentNetworkCommunicationActorRef
      )
    )
}

class Controller(
    val workflow: Workflow,
    val controllerConfig: ControllerConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(
      CONTROLLER,
      parentNetworkCommunicationActorRef,
      controllerConfig.supportFaultTolerance
    ) {
  lazy val controlInputPort: NetworkInputPort[ControlPayload] =
    new NetworkInputPort[ControlPayload](this.actorId, this.handleControlPayloadOuter)
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
  var statusUpdateAskHandle: Cancellable = _
  var replayRequestIndex = {
    if(controllerConfig.replayRequest.nonEmpty){
      controllerConfig.replayRequest(CONTROLLER)
    }else{
      -1
    }
  }
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ProcessControlMessage]]()
  private var currentHead: ActorVirtualIdentity = null
  val controlMessagesToRecover: Iterator[InMemDeterminant] = {
    logStorage.getReader.mkLogRecordIterator()
  }
  var isReplaying = false

  override def getLogName: String = "WF" + workflow.getWorkflowId().id + "-CONTROLLER"

  val determinantLogger: DeterminantLogger = logManager.getDeterminantLogger
  val globalRecoveryManager: GlobalRecoveryManager = new GlobalRecoveryManager(
    () => {
      logger.info("Start global recovery")
      asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
    },
    () => {
      logger.info("global recovery complete!")
      asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
    }
  )

  def availableNodes: Array[Address] =
    Await
      .result(context.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses, 5.seconds)
      .asInstanceOf[Array[Address]]

  val workflowScheduler =
    new WorkflowScheduler(
      availableNodes,
      networkCommunicationActor,
      context,
      asyncRPCClient,
      logger,
      workflow,
      controllerConfig
    )

  val rpcHandlerInitializer: ControllerAsyncRPCHandlerInitializer =
    wire[ControllerAsyncRPCHandlerInitializer]

  // register controller itself and client
  networkCommunicationActor.waitUntil(RegisterActorRef(CONTROLLER, self))
  networkCommunicationActor.waitUntil(RegisterActorRef(CLIENT, context.parent))

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, controller is shutting done.", reason)
    // report error to frontend
    asyncRPCClient.sendToClient(FatalError(reason))
  }

  def running: Receive = {
    forwardResendRequest orElse acceptRecoveryMessages orElse acceptDirectInvocations orElse {
      case NetworkMessage(id, WorkflowControlMessage(from, seqNum, payload)) =>
        controlInputPort.handleMessage(
          this.sender(),
          Constants.unprocessedBatchesCreditLimitPerSender, // Controller is assumed to have enough credits
          id,
          from,
          seqNum,
          payload
        )
      case other =>
        logger.info(s"unhandled message: $other")
    }
  }

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handleControlPayload(CLIENT, invocation)
  }

  def acceptRecoveryMessages: Receive = {
    case recoveryMsg: WorkflowRecoveryMessage =>
      recoveryMsg.payload match {
        case GetOperatorInternalState() =>
          Future.sequence(workflow.getAllLayers.flatMap(_.workers).map(_._2).map(info => info.ref ? WorkflowRecoveryMessage(CONTROLLER, GetOperatorInternalState()))).onComplete(v => {
            asyncRPCClient.sendToClient(AdditionalOperatorInfo(v.get.mkString("\n")))
          })
        case ContinueReplay(index) =>
          isReplaying = true
          if(index.nonEmpty){
            replayRequestIndex = index(CONTROLLER)
            // globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
            workflow.getAllLayers.flatMap(_.workers).map(_._2).foreach(info => info.ref ! WorkflowRecoveryMessage(CONTROLLER, ContinueReplayTo(index(info.id))))
          }else{
            replayRequestIndex = 99999
            // globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
            workflow.getAllLayers.flatMap(_.workers).map(_._2).foreach(info => info.ref ! WorkflowRecoveryMessage(CONTROLLER, ContinueReplayTo(9999999)))
          }
          invokeReplay()
        case UpdateRecoveryStatus(isRecovering) =>
          logger.info("recovery status for " + recoveryMsg.from + " is " + isRecovering)
          globalRecoveryManager.markRecoveryStatus(recoveryMsg.from, isRecovering)
        case ResendOutputTo(vid, ref) =>
          logger.warn(s"controller should not resend output to " + vid)
        case NotifyFailedNode(addr) =>
          if (!controllerConfig.supportFaultTolerance) {
            // do not support recovery
            throw new RuntimeException("Recovery not supported, abort.")
          }
          val deployNodes = availableNodes.filter(_ != self.path.address)
          if (deployNodes.isEmpty) {
            val error = new RuntimeException(
              "Cannot recover failed workers! No available worker machines!"
            )
            asyncRPCClient.sendToClient(FatalError(error))
            throw error
          }
          logger.info(
            "Global Recovery: move all worker from " + addr + " to " + deployNodes.head
          )
          val infoIter = workflow.getAllWorkerInfoOfAddress(addr)
          logger.info("Global Recovery: sent kill signal to workers on failed node")
          infoIter.foreach { info =>
            info.ref ! PoisonPill // in case we can still access the worker
          }
          logger.info("Global Recovery: triggering worker respawn")
          infoIter.foreach { info =>
            val ref = workflow.getWorkerLayer(info.id).recover(info.id, deployNodes.head, Map.empty)
            logger.info("Global Recovery: respawn " + info.id)
            val vidSet = infoIter.map(_.id).toSet
            // wait for some secs to re-send output
            logger.info("Global Recovery: triggering upstream resend for " + info.id)
            workflow
              .getDirectUpstreamWorkers(info.id)
              .filter(x => !vidSet.contains(x))
              .foreach { vid =>
                logger.info("Global Recovery: trigger resend from " + vid + " to " + info.id)
                workflow.getWorkerInfo(vid).ref ! ResendOutputTo(info.id, ref)
              }
            // let controller resend control messages immediately
            networkCommunicationActor ! ResendOutputTo(info.id, ref)
          }
      }
  }


  def invokeReplay(): Unit = {
    if(currentHead != null){
      if (controlMessages.contains(currentHead) && controlMessages(currentHead).nonEmpty) {
        logger.info("get current head from "+currentHead)
        val elem = controlMessages(currentHead).dequeue()
        handleControlPayload(elem.from, elem.controlPayload)
        currentHead = null
      }
    }
    while (currentHead == null) {
      controlMessagesToRecover.next() match {
        case SenderActorChange(sender) =>
          if (controlMessages.contains(sender) && controlMessages(sender).nonEmpty) {
            logger.info("already have current head of "+sender)
            val elem = controlMessages(sender).dequeue()
            handleControlPayload(elem.from, elem.controlPayload)
          } else {
            logger.info("set current head to "+sender)
            currentHead = sender
          }
        case ProcessControlMessage(controlPayload, from) =>
          controlInputPort.increaseFIFOSeqNum(from)
          handleControlPayload(from, controlPayload)
      }
      if (
        !controlMessagesToRecover.hasNext || replayRequestIndex == rpcHandlerInitializer.numPauses
      ) {
        isReplaying = false
        globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
        if (!controlMessagesToRecover.hasNext) {
          logManager.terminate()
          logStorage.cleanPartiallyWrittenLogFile()
          logManager.setupWriter(logStorage.getWriter)
          unstashAll()
          context.become(running)
        }
        return
      }
    }
  }


  def handleControlPayloadOuter(from: ActorVirtualIdentity,
                                controlPayload: ControlPayload){
    if(isReplaying){
      logger.info("received "+controlPayload+" from "+from)
      controlMessages
        .getOrElseUpdate(from, new mutable.Queue[ProcessControlMessage]())
        .enqueue(ProcessControlMessage(controlPayload, from))
      invokeReplay()
    }else{
      logger.info("normal processing of "+controlPayload)
      handleControlPayload(from, controlPayload)
    }
  }

  def handleControlPayload(
      from: ActorVirtualIdentity,
      controlPayload: ControlPayload
  ): Unit = {
    if(from == CLIENT || from == SELF || from == CONTROLLER){
      determinantLogger.logDeterminant(ProcessControlMessage(controlPayload, from))
    }else{
      //logger.info("only save sender information for "+ controlPayload+" from "+from)
      determinantLogger.logDeterminant(SenderActorChange(from))
    }
    controlPayload match {
      // use control input port to pass control messages
      case invocation: ControlInvocation =>
        assert(from.isInstanceOf[ActorVirtualIdentity])
        asyncRPCServer.logControlInvocation(invocation, from)
        asyncRPCServer.receive(invocation, from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, from)
        asyncRPCClient.fulfillPromise(ret)
      case other =>
        throw new WorkflowRuntimeException(s"unhandled control message: $other")
    }
  }

  def recovering: Receive = {
    case NetworkMessage(
          _,
          WorkflowControlMessage(from, seqNum, ControlInvocation(_, FatalError(err)))
        ) =>
      // fatal error during recovery, fail
      asyncRPCClient.sendToClient(FatalError(err))
      // re-throw the error to fail the actor
      throw err
    case NetworkMessage(id, WorkflowControlMessage(from, seqNum, payload)) =>
      controlInputPort.handleMessage(
        this.sender(),
        Constants.unprocessedBatchesCreditLimitPerSender, // Controller is assumed to have enough credits
        id,
        from,
        seqNum,
        payload
      )
    case invocation: ControlInvocation =>
      logger.info("Reject during recovery: " + invocation)
    case other =>
      logger.info("Ignore during recovery: " + other)
  }

  override def receive: Receive = {
    if (replayRequestIndex > 0) {
      globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
      // val fifoState = recoveryManager.getFIFOState(logStorage.getReader.mkLogRecordIterator())
      // controlInputPort.overwriteFIFOState(fifoState)
      // self ! controlMessagesToRecover.next()
      isReplaying = true
      rpcHandlerInitializer.suppressStatusUpdate = true
      invokeReplay()
      forwardResendRequest orElse acceptRecoveryMessages orElse recovering
    } else {
      running
    }
  }

  override def postStop(): Unit = {
    if (statusUpdateAskHandle != null) {
      statusUpdateAskHandle.cancel()
    }
    logger.info("Controller start to shutdown")
    logManager.terminate()
//    if (workflow.isCompleted) {
//      workflow.getAllWorkers.foreach { workerId =>
//        DeterminantLogStorage
//          .getLogStorage(
//            controllerConfig.supportFaultTolerance,
//            WorkflowWorker.getWorkerLogName(workerId)
//          )
//          .deleteLog()
//      }
    //logStorage.deleteLog()
//    }
    logger.info("stopped successfully!")
  }
}
