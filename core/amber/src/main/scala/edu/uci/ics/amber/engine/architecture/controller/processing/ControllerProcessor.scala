package edu.uci.ics.amber.engine.architecture.controller.processing

import akka.actor.{ActorContext, Address, PoisonPill}
import akka.pattern.ask
import akka.serialization.SerializationExtension
import akka.util.Timeout
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.checkpoint.{
  CheckpointHolder,
  SavedCheckpoint,
  SerializedState
}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{
  AdditionalOperatorInfo,
  WorkflowRecoveryStatus,
  WorkflowStatusUpdate
}
import edu.uci.ics.amber.engine.architecture.controller.processing.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.{ControllerConfig, Workflow}
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{
  InMemDeterminant,
  LogManager,
  ProcessControlMessage,
  StepDelta
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkMessage,
  NetworkSenderActorRef
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.CheckInitialized
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage._
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ControllerProcessor extends AmberLogging with Serializable {

  override def actorId: ActorVirtualIdentity = CONTROLLER

  private implicit val timeout: Timeout = Timeout(1.minute)

  // outer dependencies:
  @transient
  private[processing] var controlInput: NetworkInputPort[ControlPayload] = _
  @transient
  private[processing] var workflow: Workflow = _
  @transient
  private[processing] var scheduler: WorkflowScheduler = _
  @transient
  private[processing] var logManager: LogManager = _
  @transient
  private[processing] var logStorage: DeterminantLogStorage = _
  @transient
  private[processing] var actorContext: ActorContext = _
  @transient
  private[processing] var networkSender: NetworkSenderActorRef = _
  @transient
  private[processing] var controllerConfig: ControllerConfig = _
  @transient
  lazy private[processing] implicit val executor = actorContext.dispatcher
  @transient
  private[processing] var controlMessagesToReplay: Iterator[InMemDeterminant] = Iterator()
  @transient
  private[processing] lazy val serialization = SerializationExtension(actorContext.system)
  @transient
  private[processing] var onReplayComplete: () => Unit = _

  def initialize(
      controlInput: NetworkInputPort[ControlPayload],
      workflow: Workflow,
      scheduler: WorkflowScheduler,
      logManager: LogManager,
      logStorage: DeterminantLogStorage,
      networkSender: NetworkSenderActorRef,
      actorContext: ActorContext,
      controllerConfig: ControllerConfig
  ): Unit = {
    this.controlInput = controlInput
    this.workflow = workflow
    this.scheduler = scheduler
    this.scheduler.attachToExecution(execution, asyncRPCClient)
    this.logManager = logManager
    this.networkSender = networkSender
    this.actorContext = actorContext
    this.controllerConfig = controllerConfig
    this.logStorage = logStorage
    asyncRPCClient.sendToClient(WorkflowStatusUpdate(execution.getWorkflowStatus))
  }

  def availableNodes: Array[Address] =
    Await
      .result(
        actorContext.actorSelection("/user/cluster-info") ? GetAvailableNodeAddresses,
        5.seconds
      )
      .asInstanceOf[Array[Address]]

  def outputControlPayload(
      to: ActorVirtualIdentity,
      self: ActorVirtualIdentity,
      seqNum: Long,
      payload: ControlPayload
  ): Unit = {
    val msg = WorkflowControlMessage(self, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }

  def restoreWorkersAndResendUnAckedMessages(): Unit = {
    Await.result(
      Future.sequence(
        execution.getAllWorkers
          .map { worker =>
            workflow
              .getOperator(worker)
              .buildWorker(
                worker,
                AddressInfo(availableNodes, actorContext.self.path.address),
                actorContext,
                execution.getOperatorExecution(worker),
                networkSender,
                controllerConfig
              )
          }
          .map { ref =>
            ref ? CheckInitialized()
          }
      ),
      600.seconds
    )
    // re-send outputs:
    // exclude CLIENT
    controlOutputPort.resendMessages(Set(CLIENT))
  }

  // inner dependencies:
  lazy private[processing] val controlOutputPort: NetworkOutputPort[ControlPayload] = {
    new NetworkOutputPort[ControlPayload](actorId, this.outputControlPayload)
  }
  lazy private[controller] val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(controlOutputPort, actorId)
  lazy private[processing] val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(controlOutputPort, actorId)
  lazy val execution = new WorkflowExecution(workflow)
  lazy private[processing] val globalRecoveryManager: GlobalRecoveryManager =
    new GlobalRecoveryManager(
      () => {
        logger.info("Start global recovery")
        asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
      },
      () => {
        logger.info("global recovery complete!")
        asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
      }
    )
  lazy private[processing] val determinantLogger = logManager.getDeterminantLogger
  var isReplaying = false
  var numControlSteps = 0L
  private[processing] var replayToStep = -1L
  private[processing] val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ProcessControlMessage]]()
  private[processing] var currentHead: ActorVirtualIdentity = null
  private val rpcInitializer = new ControllerAsyncRPCHandlerInitializer(this)

  def setReplayToAndStartReplay(targetStep: Long): Unit = {
    globalRecoveryManager.markRecoveryStatus(actorId, isRecovering = true)
    this.replayToStep = targetStep
    isReplaying = true
    rpcInitializer.suppressStatusUpdate = true
    invokeReplay()
  }

  def checkIfReplayCompleted(): Boolean = {
    if (!controlMessagesToReplay.hasNext || replayToStep == numControlSteps) {
      isReplaying = false
      globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
      if (!controlMessagesToReplay.hasNext) {
        logger.info("replay completed!")
        logManager.terminate()
        logStorage.cleanPartiallyWrittenLogFile()
        logManager.setupWriter(logStorage.getWriter)
        if (onReplayComplete != null) {
          onReplayComplete()
        }
      }
      return true
    }
    false
  }

  def invokeReplay(): Unit = {
    if (currentHead != null) {
      if (controlMessages.contains(currentHead) && controlMessages(currentHead).nonEmpty) {
        logger.info("get current head from " + currentHead)
        val elem = controlMessages(currentHead).dequeue()
        handleControlPayload(elem.from, elem.controlPayload)
        currentHead = null
      }
    }
    if (checkIfReplayCompleted()) {
      return
    }
    while (currentHead == null) {
      controlMessagesToReplay.next() match {
        case StepDelta(sender, _) =>
          if (controlMessages.contains(sender) && controlMessages(sender).nonEmpty) {
            logger.info("already have current head of " + sender)
            val elem = controlMessages(sender).dequeue()
            handleControlPayload(elem.from, elem.controlPayload)
          } else {
            logger.info("set current head to " + sender)
            currentHead = sender
          }
        case ProcessControlMessage(controlPayload, from) =>
          controlInput.increaseFIFOSeqNum(from)
          handleControlPayload(from, controlPayload)
      }
      if (checkIfReplayCompleted()) {
        return
      }
    }
  }

  def handleControlPayloadOuter(from: ActorVirtualIdentity, controlPayload: ControlPayload) {
    if (isReplaying) {
      logger.info("received " + controlPayload + " from " + from)
      controlMessages
        .getOrElseUpdate(from, new mutable.Queue[ProcessControlMessage]())
        .enqueue(ProcessControlMessage(controlPayload, from))
      invokeReplay()
    } else {
      logger.info("normal processing of " + controlPayload)
      handleControlPayload(from, controlPayload)
    }
  }

  def handleControlPayload(
      from: ActorVirtualIdentity,
      controlPayload: ControlPayload
  ): Unit = {
    if (from == CLIENT || from == SELF || from == actorId) {
      determinantLogger.logDeterminant(ProcessControlMessage(controlPayload, from))
    } else {
      //logger.info("only save sender information for "+ controlPayload+" from "+from)
      determinantLogger.logDeterminant(StepDelta(from, 0))
    }
    numControlSteps += 1
    controlPayload match {
      // use control input port to pass control messages
      case invocation: ControlInvocation =>
        assert(from.isInstanceOf[ActorVirtualIdentity])
        asyncRPCServer.logControlInvocation(invocation, from, numControlSteps)
        asyncRPCServer.receive(invocation, from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, from, numControlSteps)
        asyncRPCClient.fulfillPromise(ret)
      case other =>
        throw new WorkflowRuntimeException(s"unhandled control message: $other")
    }
  }

  def terminate(): Unit = {
    if (
      rpcInitializer.statusUpdateAskHandle != null && rpcInitializer.statusUpdateAskHandle.isDefined
    ) {
      rpcInitializer.statusUpdateAskHandle.get.cancel()
    }
  }

  def processRecoveryMessage(recoveryMsg: WorkflowRecoveryMessage): Unit = {
    // TODO: merge these to control messages?
    recoveryMsg.payload match {
      case TakeGlobalCheckpoint(cutoffMap) =>
        logger.info("start to take global checkpoint")
        // save output messages first
        val startTime = System.currentTimeMillis()
        val chkpt = new SavedCheckpoint()
        chkpt.attachSerialization(serialization)
        chkpt.save("fifoState", controlInput.getFIFOState)
        // follow topological order
        val runningWorkers = execution.getAllWorkers.toSet
        Await.result(Future.sequence(runningWorkers.map{
          worker =>
          logger.info(s"start to ask $worker to checkpoint")
            (execution
            .getOperatorExecution(worker)
            .getWorkerInfo(worker)
            .ref ? WorkflowRecoveryMessage(actorId, TakeLocalCheckpoint(cutoffMap.getOrElse(worker, Map())))).map{
              result =>
                val alignment = result.asInstanceOf[Long]
                logger.info(s"received alignment for $worker alignment = $alignment")
                if (!CheckpointHolder.hasCheckpoint(worker, alignment)) {
                  // put placeholder
                  CheckpointHolder.addCheckpoint(worker, alignment, new SavedCheckpoint(), false)
                }
            }
        }), 60.seconds)
        // finalize checkpoint
        chkpt.save("controlState", this)
        CheckpointHolder.addCheckpoint(actorId, numControlSteps, chkpt, false)
        logger.info(
          s"checkpoint stored for $actorId at alignment = $numControlSteps size = ${chkpt.size()} bytes"
        )
        logger.info(
          s"global checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000d}s"
        )
        actorContext.sender() ! ((System.currentTimeMillis() - startTime) / 1000d, numControlSteps)
      case GetOperatorInternalState() =>
        Future
          .sequence(
            execution.getAllWorkers
              .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
              .map(info => info.ref ? WorkflowRecoveryMessage(actorId, GetOperatorInternalState()))
          )
          .onComplete(v => {
            asyncRPCClient.sendToClient(AdditionalOperatorInfo(v.get.mkString("\n")))
          })
      case ContinueReplay(conf) =>
        controllerConfig.stateRestoreConfig = conf
        execution.getAllWorkers
          .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
          .foreach(info =>
            info.ref ! WorkflowRecoveryMessage(
              actorId,
              ContinueReplayTo(conf.workerConfs(info.id).replayTo.get)
            )
          )
        setReplayToAndStartReplay(conf.controllerConf.replayTo.get)
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
        val deployNodes = availableNodes.filter(_ != actorContext.self.path.address)
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
        val infoIter = execution.getAllWorkerInfoOfAddress(addr)
        logger.info("Global Recovery: sent kill signal to workers on failed node")
        infoIter.foreach { info =>
          info.ref ! PoisonPill // in case we can still access the worker
        }
        logger.info("Global Recovery: triggering worker respawn")
        infoIter.foreach { info =>
          val ref = workflow
            .getOperator(info.id)
            .buildWorker(
              info.id,
              AddressInfo(availableNodes, actorContext.self.path.address),
              actorContext,
              execution.getOperatorExecution(info.id),
              networkSender,
              controllerConfig
            )
          logger.info("Global Recovery: respawn " + info.id)
          val vidSet = infoIter.map(_.id).toSet
          // wait for some secs to re-send output
          logger.info("Global Recovery: triggering upstream resend for " + info.id)
          workflow
            .getDirectUpstreamWorkers(info.id)
            .filter(x => !vidSet.contains(x))
            .foreach { vid =>
              logger.info("Global Recovery: trigger resend from " + vid + " to " + info.id)
              execution.getOperatorExecution(vid).getWorkerInfo(vid).ref ! ResendOutputTo(
                info.id,
                ref
              )
            }
          // let controller resend control messages immediately
          networkSender ! ResendOutputTo(info.id, ref)
        }
    }
  }

  def enterReplay(replayTo: Long, onReplayComplete: () => Unit): Unit = {
    this.onReplayComplete = onReplayComplete
    controlMessagesToReplay = logStorage.getReader.mkLogRecordIterator().drop(numControlSteps.toInt)
    setReplayToAndStartReplay(replayTo)
  }

  def interruptReplay(): Unit = {
    controlMessagesToReplay = Iterator()
    replayToStep = numControlSteps
    assert(checkIfReplayCompleted())
    this.onReplayComplete = null
    replayToStep = -1
  }

}
