package edu.uci.ics.amber.engine.architecture.controller

import akka.actor.{ActorContext, Address, PoisonPill}
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint, SerializedState}
import edu.uci.ics.amber.engine.architecture.controller.ControllerEvent.{AdditionalOperatorInfo, WorkflowRecoveryStatus}
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.execution.WorkflowExecution
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.storage.DeterminantLogStorage
import edu.uci.ics.amber.engine.architecture.logging.{InMemDeterminant, LogManager, ProcessControlMessage, StepDelta}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetMessageInQueue, NetworkMessage, NetworkSenderActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.architecture.recovery.GlobalRecoveryManager
import edu.uci.ics.amber.engine.architecture.scheduling.WorkflowScheduler
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ContinueReplay, ContinueReplayTo, ControlPayload, GetOperatorInternalState, NotifyFailedNode, ResendOutputTo, TakeGlobalCheckpoint, TakeLocalCheckpoint, UpdateRecoveryStatus, WorkflowControlMessage, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CLIENT, CONTROLLER, SELF}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import akka.serialization.SerializationExtension
import edu.uci.ics.amber.clustering.ClusterListener.GetAvailableNodeAddresses
import edu.uci.ics.amber.engine.architecture.deploysemantics.locationpreference.AddressInfo
import edu.uci.ics.amber.engine.architecture.scheduling.policies.SchedulingPolicy

class ControllerProcessor
    extends ControllerAsyncRPCHandlerInitializer
    with AmberLogging
    with Serializable {

  override def actorId: ActorVirtualIdentity = CONTROLLER

  // outer dependencies:
  @transient
  protected var controlInput: NetworkInputPort[ControlPayload] = _
  @transient
  protected var workflow: Workflow = _
  @transient
  protected var scheduler: WorkflowScheduler = _
  @transient
  protected var logManager: LogManager = _
  @transient
  protected var logStorage: DeterminantLogStorage = _
  @transient
  protected var actorContext: ActorContext = _
  @transient
  protected var networkSender: NetworkSenderActorRef = _
  @transient
  protected var controllerConfig: ControllerConfig = _
  @transient
  lazy protected implicit val executor = actorContext.dispatcher
  @transient
  lazy protected val controlMessagesToReplay: Iterator[InMemDeterminant] = {
    logStorage.getReader.mkLogRecordIterator().drop(numControlSteps.toInt)
  }
  @transient
  private lazy val serialization = SerializationExtension(actorContext.system)
  @transient
  private var onReplayComplete: () => Unit = _

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

  def restoreWorkers(): Unit = {
    execution.getAllWorkers.foreach { worker =>
      workflow
        .getOperator(worker)
        .build(
          AddressInfo(availableNodes, actorContext.self.path.address),
          networkSender,
          actorContext,
          execution.getOperatorExecution(worker),
          controllerConfig
        )
    }
  }

  // inner dependencies:
  lazy protected val controlOutputPort: NetworkOutputPort[ControlPayload] = {
    new NetworkOutputPort[ControlPayload](actorId, this.outputControlPayload)
  }
  lazy protected val asyncRPCClient: AsyncRPCClient = new AsyncRPCClient(controlOutputPort, actorId)
  lazy protected val asyncRPCServer: AsyncRPCServer = new AsyncRPCServer(controlOutputPort, actorId)
  lazy val execution = new WorkflowExecution()
  lazy protected val globalRecoveryManager: GlobalRecoveryManager = new GlobalRecoveryManager(
    () => {
      logger.info("Start global recovery")
      asyncRPCClient.sendToClient(WorkflowRecoveryStatus(true))
    },
    () => {
      logger.info("global recovery complete!")
      asyncRPCClient.sendToClient(WorkflowRecoveryStatus(false))
    }
  )
  lazy private val determinantLogger = logManager.getDeterminantLogger
  var isReplaying = false
  var numControlSteps = 0L
  private var replayToStep = -1L
  private val controlMessages = mutable
    .HashMap[ActorVirtualIdentity, mutable.Queue[ProcessControlMessage]]()
  private var currentHead: ActorVirtualIdentity = null

  def setReplayTo(targetStep: Long): Unit = {
    replayToStep = targetStep
  }

  def checkIfReplayCompleted(): Boolean = {
    if (!controlMessagesToReplay.hasNext || replayToStep == numControlSteps) {
      isReplaying = false
      globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = false)
      if (!controlMessagesToReplay.hasNext) {
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
        case StepDelta(sender,_) =>
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
    if (from == CLIENT || from == SELF || from == CONTROLLER) {
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
        asyncRPCServer.logControlInvocation(invocation, from)
        asyncRPCServer.receive(invocation, from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, from)
        asyncRPCClient.fulfillPromise(ret)
      case other =>
        throw new WorkflowRuntimeException(s"unhandled control message: $other")
    }
  }

  def terminate(): Unit = {
    if (statusUpdateAskHandle != null && statusUpdateAskHandle.isDefined) {
      statusUpdateAskHandle.get.cancel()
    }
  }

  def processRecoveryMessage(recoveryMsg: WorkflowRecoveryMessage): Unit = {
    recoveryMsg.payload match {
      case TakeGlobalCheckpoint() =>
        // save output messages first
        val chkpt = new SavedCheckpoint()
        chkpt.save(
          "fifoState",
          SerializedState.fromObject(controlInput.getFIFOState, serialization)
        )
        chkpt.save(
          "outputMessages",
          SerializedState.fromObject(
            Await
              .result((networkSender.ref ? GetMessageInQueue), 5.seconds)
              .asInstanceOf[Array[(ActorVirtualIdentity, Iterable[NetworkMessage])]],
            serialization
          )
        )
        // follow topological order
        val iter = workflow.getDAG.iterator()
        while (iter.hasNext) {
          val worker = iter.next()
          val alignment = Await
            .result(
              execution
                .getOperatorExecution(worker)
                .getWorkerInfo(worker)
                .ref ? WorkflowRecoveryMessage(CONTROLLER, TakeLocalCheckpoint()),
              10.seconds
            )
            .asInstanceOf[Long]
          if (!CheckpointHolder.hasCheckpoint(worker, alignment)) {
            // put placeholder
            CheckpointHolder.addCheckpoint(worker, alignment, new SavedCheckpoint())
          }
        }
        // finalize checkpoint
        chkpt.save("controlState", SerializedState.fromObject(this, serialization))
        CheckpointHolder.addCheckpoint(CONTROLLER, numControlSteps, chkpt)
      case GetOperatorInternalState() =>
        Future
          .sequence(
            execution.getAllWorkers
              .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
              .map(info =>
                info.ref ? WorkflowRecoveryMessage(CONTROLLER, GetOperatorInternalState())
              )
          )
          .onComplete(v => {
            asyncRPCClient.sendToClient(AdditionalOperatorInfo(v.get.mkString("\n")))
          })
      case ContinueReplay(index) =>
        isReplaying = true
        if (index.nonEmpty) {
          setReplayTo(index(CONTROLLER))
          controllerConfig.replayRequest = index
          // globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
          execution.getAllWorkers
            .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
            .foreach(info =>
              info.ref ! WorkflowRecoveryMessage(CONTROLLER, ContinueReplayTo(index(info.id)))
            )
        } else {
          setReplayTo(-1)
          // globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
          execution.getAllWorkers
            .map(x => execution.getOperatorExecution(x).getWorkerInfo(x))
            .foreach(info => info.ref ! WorkflowRecoveryMessage(CONTROLLER, ContinueReplayTo(-1)))
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
            .recover(
              info.id,
              deployNodes.head,
              actorContext,
              execution.getOperatorExecution(info.id),
              networkSender
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
    globalRecoveryManager.markRecoveryStatus(CONTROLLER, isRecovering = true)
    this.onReplayComplete = onReplayComplete
    this.replayToStep = replayTo
    isReplaying = true
    suppressStatusUpdate = true
    invokeReplay()
  }

}
