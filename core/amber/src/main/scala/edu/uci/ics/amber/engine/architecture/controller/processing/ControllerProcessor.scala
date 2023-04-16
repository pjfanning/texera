//package edu.uci.ics.amber.engine.architecture.controller.processing
//
//import akka.actor.{ActorContext, ActorRef, Address, PoisonPill}
//import akka.pattern.ask
//import akka.serialization.SerializationExtension
//import akka.util.Timeout
//import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
//import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
//import edu.uci.ics.amber.engine.architecture.common.ProcessingHistory
//import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{AdditionalOperatorInfo, WorkflowRecoveryStatus, WorkflowStatusUpdate}
//import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
//import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
//import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
//import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
//import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
//import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
//import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, LogManager, StepsOnChannel}
//import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkMessage, NetworkSenderActorRef}
//import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
//import edu.uci.ics.amber.engine.architecture.recovery.{GlobalRecoveryManager, PendingCheckpoint, ReplayInputRecorder}
//import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
//import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.CheckInitialized
//import edu.uci.ics.amber.engine.common.AmberLogging
//import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
//import edu.uci.ics.amber.engine.common.ambermessage.{WorkflowFIFOMessagePayload, _}
//import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
//import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipFaultTolerance
//import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
//import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
//import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}
//
//import scala.collection.mutable
//import scala.concurrent.duration.DurationInt
//import scala.concurrent.{Await, Future}
//
//class ControllerProcessor extends AmberLogging with Serializable {
//
//  override def actorId: ActorVirtualIdentity = CONTROLLER
//
//  private implicit val timeout: Timeout = Timeout(1.minute)
//
//  // outer dependencies:
//  @transient
//  private[processing] var controlInput: NetworkInputPort = _
//  @transient
//  private[processing] var workflow: Workflow = _
//  @transient
//  private[processing] var scheduler: WorkflowScheduler = _
//  @transient
//  private[processing] var logManager: LogManager = _
//  @transient
//  private[processing] var logStorage: DeterminantLogStorage = _
//  @transient
//  private[processing] var actorContext: ActorContext = _
//  @transient
//  private[processing] var networkSender: NetworkSenderActorRef = _
//  @transient
//  private[processing] var controllerConfig: ControllerConfig = _
//  @transient
//  lazy private[processing] implicit val executor = actorContext.dispatcher
//  @transient
//  private[processing] var controlMessagesToReplay: Iterator[InMemDeterminant] = Iterator()
//  @transient
//  private[processing] lazy val serialization = SerializationExtension(actorContext.system)
//  @transient
//  private[processing] var onReplayComplete: () => Unit = _
//  @transient
//  private[processing] var interactionHistory: ProcessingHistory = _
//  @transient
//  private[processing] val pendingCheckpoints = mutable.HashMap[Long, PendingCheckpoint]()
//
//
//  def initialize(
//      controlInput: NetworkInputPort,
//      workflow: Workflow,
//      scheduler: WorkflowScheduler,
//      logManager: LogManager,
//      logStorage: DeterminantLogStorage,
//      networkSender: NetworkSenderActorRef,
//      actorContext: ActorContext,
//      controllerConfig: ControllerConfig
//  ): Unit = {
//    this.controlInput = controlInput
//    this.workflow = workflow
//    this.scheduler = scheduler
//    this.scheduler.attachToExecution(execution, asyncRPCClient)
//    this.logManager = logManager
//    this.networkSender = networkSender
//    this.actorContext = actorContext
//    this.controllerConfig = controllerConfig
//    this.logStorage = logStorage
//    this.interactionHistory = new ProcessingHistory()
//    asyncRPCClient.sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))
//  }
//
//  def availableNodes: Array[Address] =
//    Await
//      .result(
//        actorContext.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses,
//        5.seconds
//      )
//      .asInstanceOf[Array[Address]]
//
//  def outputControlPayload(
//      to: ActorVirtualIdentity,
//      msg: WorkflowFIFOMessage
//  ): Unit = {
//    logManager.sendCommitted(SendRequest(to, msg))
//  }
//
//  def restoreWorkersAndResendUnAckedMessages(): Unit = {
//    Await.result(
//      Future.sequence(
//        execution.getAllWorkers
//          .map { worker =>
//            workflow
//              .getOperator(worker)
//              .buildWorker(
//                worker,
//                AddressInfo(availableNodes, actorContext.self.path.address),
//                actorContext,
//                execution.getOperatorExecution(worker),
//                networkSender,
//                controllerConfig,
//                globalRecoveryManager
//              )
//          }
//          .map { ref =>
//            ref ? CheckInitialized()
//          }
//      ),
//      600.seconds
//    )
//  }
//
//  // inner dependencies:
//  lazy private[processing] val controlOutputPort: NetworkOutputPort = {
//    new NetworkOutputPort(actorId, this.outputControlPayload)
//  }
//  lazy private[controller] val asyncRPCClient: AsyncRPCClient =
//    new AsyncRPCClient(controlOutputPort, actorId)
//  lazy private[processing] val asyncRPCServer: AsyncRPCServer =
//    new AsyncRPCServer(controlOutputPort, actorId)
//  lazy private[processing] val globalRecoveryManager: GlobalRecoveryManager =
//    new GlobalRecoveryManager(
//      () => {
//        logger.info("Start global recovery")
//        asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
//      },
//      () => {
//        logger.info("global recovery complete!")
//        asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
//      }
//    )
//  lazy val execution = new WorkflowExecution(workflow, globalRecoveryManager)
//  lazy private[processing] val determinantLogger = logManager.getDeterminantLogger
//  var isReplaying = false
//  var numControlSteps = 0L
//  private[processing] var replayToStep = -1L
//  private[processing] val messagesDuringReplay = mutable
//    .HashMap[ChannelEndpointID, mutable.Queue[ControlPayload]]()
//  private[processing] var currentHead: ChannelEndpointID = null
//  private val rpcInitializer = new ControllerAsyncRPCHandlerInitializer(this)
//
//  def setReplayToAndStartReplay(targetStep: Long): Unit = {
//    globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
//    logger.info(s"replay to $replayToStep")
//    this.replayToStep = targetStep
//    isReplaying = true
//    rpcInitializer.suppressStatusUpdate = true
//    invokeReplay()
//  }
//
//  def checkIfReplayCompleted(): Boolean = {
//    if (!controlMessagesToReplay.hasNext || replayToStep == numControlSteps) {
//      isReplaying = false
//      globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
//      if (!controlMessagesToReplay.hasNext) {
//        logger.info("replay completed!")
//        logManager.terminate()
//        logStorage.cleanPartiallyWrittenLogFile()
//        logManager.setupWriter(logStorage.getWriter)
//        if (onReplayComplete != null) {
//          onReplayComplete()
//        }
//      }
//      return true
//    }
//    false
//  }
//
//  def invokeReplay(): Unit = {
//    if (currentHead != null) {
//      if (messagesDuringReplay.contains(currentHead) && messagesDuringReplay(currentHead).nonEmpty) {
//        logger.info("get current head from " + currentHead)
//        val elem = messagesDuringReplay(currentHead).dequeue()
//        handleControlPayload(currentHead, elem)
//        currentHead = null
//      }
//    }
//    if (checkIfReplayCompleted()) {
//      return
//    }
//    while (currentHead == null) {
//      controlMessagesToReplay.next() match {
//        case StepsOnChannel(sender, _) =>
//          if (messagesDuringReplay.contains(sender) && messagesDuringReplay(sender).nonEmpty) {
//            logger.info("already have current head of " + sender)
//            val elem = messagesDuringReplay(sender).dequeue()
//            handleControlPayload(sender, elem)
//          } else {
//            logger.info("set current head to " + sender)
//            currentHead = sender
//          }
//      }
//      if (checkIfReplayCompleted()) {
//        return
//      }
//    }
//  }
//
//  def handlePayloadOuter(channelId: ChannelEndpointID, payload: WorkflowFIFOMessagePayload):Unit= {
//    pendingCheckpoints.foreach{
//      case (id, chkpt) =>
//        chkpt.recordInput(channelId, payload)
//    }
//    payload match {
//      case controlPayload: ControlPayload =>
//        if (isReplaying) {
//          logger.info("received " + controlPayload + " from " + channelId)
//          messagesDuringReplay
//            .getOrElseUpdate(channelId, new mutable.Queue[ControlPayload]())
//            .enqueue(controlPayload)
//          invokeReplay()
//        } else {
//          logger.info("normal processing of " + controlPayload)
//          handleControlPayload(channelId, controlPayload)
//        }
//      case marker: FIFOMarker =>
//        if(pendingCheckpoints.contains(marker.id)){
//          pendingCheckpoints(marker.id).acceptSnapshotMarker(channelId)
//          if(pendingCheckpoints(marker.id).isCompleted){
//            pendingCheckpoints.remove(marker.id)
//          }
//        }
//        logger.info(s"receive snapshot marker from $channelId and marker id = ${marker.id}")
//      case _ => logger.warn(s"received $payload which cannot be handled by controller")
//    }
//  }
//
//  def handleControlPayload(
//      channelId: ChannelEndpointID,
//      controlPayload: ControlPayload
//  ): Unit = {
////    if (from == CLIENT || from == SELF || from == actorId) {
////      determinantLogger.logDeterminant(ProcessControlMessage(controlPayload, from))
////    } else {
////      //logger.info("only save sender information for "+ controlPayload+" from "+from)
////      determinantLogger.logDeterminant(StepDelta(from, 0))
////    }
//    controlPayload match {
//      // use control input port to pass control messages
//      case invocation: ControlInvocation =>
//        asyncRPCServer.logControlInvocation(invocation, from, numControlSteps)
//        asyncRPCServer.receive(invocation, from)
//        if (invocation.command.isInstanceOf[SkipFaultTolerance]) {
//          numControlSteps -= 1 // negate the effect, must do it after processing control message
//        }
//      case ret: ReturnInvocation =>
//        determinantLogger.logDeterminant(ProcessControlMessage(ret, from))
//        asyncRPCClient.logControlReply(ret, from, numControlSteps)
//        asyncRPCClient.fulfillPromise(ret)
//      case other =>
//        throw new WorkflowRuntimeException(s"unhandled control message: $other")
//    }
//    numControlSteps += 1
//  }
//
//  def terminate(): Unit = {
//    if (
//      rpcInitializer.statusUpdateAskHandle != null && rpcInitializer.statusUpdateAskHandle.isDefined
//    ) {
//      rpcInitializer.statusUpdateAskHandle.get.cancel()
//    }
//  }
//
//  def processRecoveryMessage(recoveryMsg: AmberInternalMessage): Unit = {
//    // TODO: merge these to control messages?
//    recoveryMsg.payload match {
//      case CheckpointCompleted(id, alignment) =>
//        if(!CheckpointHolder.hasCheckpoint(recoveryMsg.from, alignment)){
//          CheckpointHolder.addCheckpoint(recoveryMsg.from, alignment, null)
//        }
//      case TakeGlobalCheckpoint() =>
//        logger.info("start to take global checkpoint")
//        val startTime = System.currentTimeMillis()
//        val chkpt = new SavedCheckpoint()
//        chkpt.attachSerialization(serialization)
//        chkpt.save("fifoState", controlInput.getFIFOState)
//        chkpt.save("controlState", this)
//        val markerCollectionCountMap = execution.getAllWorkers.map{
//          worker =>
//            worker -> execution.getOperatorExecution(worker).getWorkerInfo(worker).upstreamChannelCount
//        }.toMap
//        val checkpointId = CheckpointHolder.generateCheckpointId
//        controlOutputPort.broadcastMarker(GlobalCheckpointMarker(checkpointId, markerCollectionCountMap))
//        val msgSender = actorContext.sender()
//        val onComplete = () =>{ msgSender ! ((System.currentTimeMillis() - startTime) / 1000d, numControlSteps)}
//        pendingCheckpoints(checkpointId) = new PendingCheckpoint(actorId, startTime, chkpt, numControlSteps, controlInput.getActiveChannels.size, onComplete)
//      case GetOperatorInternalState() =>
//        Future
//          .sequence(
//            execution.getAllWorkers
//              .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
//              .map(info => info.ref ? AmberInternalMessage(actorId, GetOperatorInternalState()))
//          )
//          .onComplete(v => {
//            asyncRPCClient.sendToClient(AdditionalOperatorInfo(v.get.mkString("\n")))
//          })
//      case ContinueReplay(conf) =>
//        controllerConfig.stateRestoreConfig = conf
//        val futures = mutable.ArrayBuffer[Future[Any]]()
//        execution.getAllWorkers
//          .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
//          .foreach(info =>{
//            if(conf.confs(info.id).fromCheckpoint.isDefined){
//              info.ref ! PoisonPill
//              futures.append(workflow
//                .getOperator(info.id)
//                .buildWorker(
//                  info.id,
//                  AddressInfo(availableNodes, actorContext.self.path.address),
//                  actorContext,
//                  execution.getOperatorExecution(info.id),
//                  networkSender,
//                  controllerConfig,
//                  globalRecoveryManager
//                ) ? CheckInitialized())
//            }else{
//              globalRecoveryManager.markRecoveryStatus(info.id, isRecovering = true)
//              info.ref ! AmberInternalMessage(
//                actorId,
//                ContinueReplayTo(conf.confs(info.id).replayTo.get)
//              )
//            }
//          })
//        Await.result(Future.sequence(futures), 600.seconds)
//        setReplayToAndStartReplay(conf.confs(CONTROLLER).replayTo.get)
//      case UpdateRecoveryStatus(isRecovering) =>
//        logger.info("recovery status for " + recoveryMsg.from + " is " + isRecovering)
//        globalRecoveryManager.markRecoveryStatus(recoveryMsg.from, isRecovering)
//      case ResendOutputTo(vid, ref) =>
//        logger.warn(s"controller should not resend output to " + vid)
//      case NotifyFailedNode(addr) =>
//        if (!controllerConfig.supportFaultTolerance) {
//          // do not support recovery
//          throw new RuntimeException("Recovery not supported, abort.")
//        }
//        val deployNodes = availableNodes.filter(_ != actorContext.self.path.address)
//        if (deployNodes.isEmpty) {
//          val error = new RuntimeException(
//            "Cannot recover failed workers! No available worker machines!"
//          )
//          asyncRPCClient.sendToClient(FatalError(error))
//          throw error
//        }
//        logger.info(
//          "Global Recovery: move all worker from " + addr + " to " + deployNodes.head
//        )
//        val infoIter = execution.getAllWorkerInfoOfAddress(addr)
//        logger.info("Global Recovery: sent kill signal to workers on failed node")
//        infoIter.foreach { info =>
//          info.ref ! PoisonPill // in case we can still access the worker
//        }
//        logger.info("Global Recovery: triggering worker respawn")
//        infoIter.foreach { info =>
//          val ref = workflow
//            .getOperator(info.id)
//            .buildWorker(
//              info.id,
//              AddressInfo(availableNodes, actorContext.self.path.address),
//              actorContext,
//              execution.getOperatorExecution(info.id),
//              networkSender,
//              controllerConfig,
//              globalRecoveryManager
//            )
//          logger.info("Global Recovery: respawn " + info.id)
//          val vidSet = infoIter.map(_.id).toSet
//          // wait for some secs to re-send output
//          logger.info("Global Recovery: triggering upstream resend for " + info.id)
//          workflow
//            .getDirectUpstreamWorkers(info.id)
//            .filter(x => !vidSet.contains(x))
//            .foreach { vid =>
//              logger.info("Global Recovery: trigger resend from " + vid + " to " + info.id)
//              execution.getOperatorExecution(vid).getWorkerInfo(vid).ref ! ResendOutputTo(
//                info.id,
//                ref
//              )
//            }
//          // let controller resend control messages immediately
//          networkSender ! ResendOutputTo(info.id, ref)
//        }
//    }
//  }
//
//  def enterReplay(replayTo: Long, onReplayComplete: () => Unit): Unit = {
//    this.onReplayComplete = onReplayComplete
//    controlMessagesToReplay = logStorage.getReader.mkLogRecordIterator().drop(numControlSteps.toInt)
//    setReplayToAndStartReplay(replayTo)
//  }
//
//  def interruptReplay(): Unit = {
//    controlMessagesToReplay = Iterator()
//    replayToStep = numControlSteps
//    assert(checkIfReplayCompleted())
//    this.onReplayComplete = null
//    replayToStep = -1
//  }
//
//}
