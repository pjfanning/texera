package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import akka.util.Timeout
import com.softwaremill.macwire.wire
import akka.pattern.ask
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.WorkerExecutionStartedHandler.WorkerStateUpdated
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef, ResendFeasibility, SendRequest}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{BatchToTupleConverter, NetworkInputPort, NetworkOutputPort, TupleToBatchConverter}
import edu.uci.ics.amber.engine.architecture.recovery.ReplayGate
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.ControlElement
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.getWorkerLogName
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.CheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ShutdownDPThreadHandler.ShutdownDPThread
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ContinueReplay, ContinueReplayTo, ControlPayload, CreditRequest, DataPayload, GetOperatorInternalState, ResendOutputTo, TakeLocalCheckpoint, UpdateRecoveryStatus, WorkflowControlMessage, WorkflowDataMessage, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCHandlerInitializer}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.virtualidentity.{ActorVirtualIdentity, LinkIdentity}
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}

import java.util.concurrent.CompletableFuture
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      op: IOperatorExecutor,
      inputToOrdinalMapping: Map[LinkIdentity, Int],
      outputToOrdinalMapping: mutable.Map[LinkIdentity, Int],
      parentNetworkCommunicationActorRef: NetworkSenderActorRef,
      allUpstreamLinkIds: Set[LinkIdentity],
      supportFaultTolerance: Boolean,
      recoverToPauseIndex: Long
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        op,
        inputToOrdinalMapping,
        outputToOrdinalMapping,
        parentNetworkCommunicationActorRef,
        allUpstreamLinkIds,
        supportFaultTolerance,
        recoverToPauseIndex
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")
}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    operator: IOperatorExecutor,
    inputToOrdinalMapping: Map[LinkIdentity, Int],
    outputToOrdinalMapping: mutable.Map[LinkIdentity, Int],
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    allUpstreamLinkIds: Set[LinkIdentity],
    supportFaultTolerance: Boolean,
    replayTo: Long
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, supportFaultTolerance) {
  lazy val replayGate = new ReplayGate(logStorage.getReader)
  lazy val pauseManager: PauseManager = wire[PauseManager]
  lazy val upstreamLinkStatus: UpstreamLinkStatus = wire[UpstreamLinkStatus]
  lazy val dataProcessor: DataProcessor = wire[DataProcessor]
  lazy val dataInputPort: NetworkInputPort[DataPayload] =
    new NetworkInputPort[DataPayload](this.actorId, this.handleDataPayload)
  lazy val controlInputPort: NetworkInputPort[ControlPayload] =
    new NetworkInputPort[ControlPayload](this.actorId, this.handleControlPayload)
  lazy val dataOutputPort: NetworkOutputPort[DataPayload] =
    new NetworkOutputPort[DataPayload](this.actorId, this.outputDataPayload)
  lazy val batchProducer: TupleToBatchConverter = wire[TupleToBatchConverter]
  lazy val tupleProducer: BatchToTupleConverter = wire[BatchToTupleConverter]
  lazy val breakpointManager: BreakpointManager = wire[BreakpointManager]
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
  val workerStateManager: WorkerStateManager = new WorkerStateManager()
  val rpcHandlerInitializer: WorkerAsyncRPCHandlerInitializer =
    wire[WorkerAsyncRPCHandlerInitializer]

  rpcHandlerInitializer.setNetworkSender(networkCommunicationActor)

  if (parentNetworkCommunicationActorRef != null) {
    parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
  }

  logger.info("set replay to "+replayTo)
  replayGate.setReplayTo(replayTo, false)

  override def getLogName: String = getWorkerLogName(actorId)

  def getSenderCredits(sender: ActorVirtualIdentity) = {
    tupleProducer.getSenderCredits(sender)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
    asyncRPCClient.send(
      FatalError(reason),
      CONTROLLER
    )
  }

  override def receive: Receive = {
    if (!replayGate.recoveryCompleted) {
      recoveryManager.registerOnStart(() =>{}
        // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(true))
      )
      recoveryManager.setNotifyReplayCallback(() =>{}
        // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
      )
      recoveryManager.registerOnEnd(() =>{}
        // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
      )
      val fifoState = recoveryManager.getFIFOState(logStorage.getReader.mkLogRecordIterator())
      controlInputPort.overwriteFIFOState(fifoState)
    }
    dataProcessor.start()
    receiveAndProcessMessages
  }

  def receiveAndProcessMessages: Receive =
    forwardResendRequest orElse disallowActorRefRelatedMessages orElse {
      case WorkflowRecoveryMessage(from, TakeLocalCheckpoint()) =>
        val syncFuture = new CompletableFuture[Unit]()
        dataProcessor.enqueueCommand(ControlInvocation(AsyncRPCClient.IgnoreReplyAndDoNotLog, TakeCheckpoint(syncFuture)), SELF)
        syncFuture.get()
      case WorkflowRecoveryMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case WorkflowRecoveryMessage(from, ContinueReplayTo(index)) =>
        // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(true))
        replayGate.setReplayTo(index, true)
      case NetworkMessage(id, WorkflowDataMessage(from, seqNum, payload)) =>
        dataInputPort.handleMessage(
          this.sender(),
          getSenderCredits(from),
          id,
          from,
          seqNum,
          payload
        )
      case NetworkMessage(id, WorkflowControlMessage(from, seqNum, payload)) =>
        controlInputPort.handleMessage(
          this.sender(),
          getSenderCredits(from),
          id,
          from,
          seqNum,
          payload
        )
      case NetworkMessage(id, CreditRequest(from, _)) =>
        sender ! NetworkAck(id, Some(getSenderCredits(from)))
      case other =>
        throw new WorkflowRuntimeException(s"unhandled message: $other")
    }

  def handleDataPayload(from: ActorVirtualIdentity, dataPayload: DataPayload): Unit = {
    tupleProducer.processDataPayload(from, dataPayload)
  }

  def handleControlPayload(
      from: ActorVirtualIdentity,
      controlPayload: ControlPayload
  ): Unit = {
    // let dp thread process it
    controlPayload match {
      case controlCommand @ (ControlInvocation(_, _) | ReturnInvocation(_, _)) =>
        replayGate.addControl(ControlElement(controlCommand, from))
      case _ =>
        throw new WorkflowRuntimeException(s"unhandled control payload: $controlPayload")
    }
  }

  def outputDataPayload(
      to: ActorVirtualIdentity,
      self: ActorVirtualIdentity,
      seqNum: Long,
      payload: DataPayload
  ): Unit = {
    val msg = WorkflowDataMessage(self, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }

  override def postStop(): Unit = {
    // shutdown dp thread by sending a command
    val shutdown = ShutdownDPThread()
    replayGate.addControl(
      ControlElement(ControlInvocation(AsyncRPCClient.IgnoreReply, shutdown),
      SELF)
    )
    shutdown.completed.get()
    logger.info("stopped!")
  }

}
