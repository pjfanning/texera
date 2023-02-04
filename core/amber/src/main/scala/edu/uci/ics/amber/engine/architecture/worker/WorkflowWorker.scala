package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import akka.util.Timeout
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.controller.promisehandlers.FatalErrorHandler.FatalError
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{BatchToTupleConverter, CreditMonitor, NetworkInputPort, NetworkOutputPort, TupleToBatchConverter}
import edu.uci.ics.amber.engine.architecture.worker.DataProcessor.{Checkpoint, ControlElement, DataElement, InputTuple, InternalCommand, NoOperation, Shutdown}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.getWorkerLogName
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerState.READY
import edu.uci.ics.amber.engine.common.IOperatorExecutor
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ContinueReplay, ContinueReplayTo, ControlPayload, CreditRequest, DataPayload, GetOperatorInternalState, ResendOutputTo, TakeLocalCheckpoint, UpdateRecoveryStatus, WorkflowControlMessage, WorkflowDataMessage, WorkflowRecoveryMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.statetransition.WorkerStateManager
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}

import java.util.concurrent.CompletableFuture
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef,
      supportFaultTolerance: Boolean,
      recoverToPauseIndex: Long
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig,
        parentNetworkCommunicationActorRef,
        supportFaultTolerance,
        recoverToPauseIndex
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")
}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    supportFaultTolerance: Boolean,
    replayTo: Long
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, supportFaultTolerance) {
  logger.info(s"Worker:$actorId = ${context.self}")
  val creditMonitor = new CreditMonitor()
  lazy val inputHub = new InputHub(logStorage.getReader,creditMonitor)
  lazy val dataInputPort: NetworkInputPort[DataPayload] =
    new NetworkInputPort[DataPayload](this.actorId, this.handleDataPayload)
  lazy val controlInputPort: NetworkInputPort[ControlPayload] =
    new NetworkInputPort[ControlPayload](this.actorId, this.handleControlPayload)
  lazy val tupleProducer: BatchToTupleConverter = wire[BatchToTupleConverter]
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
  lazy val dataProcessor: DataProcessor = wire[DataProcessor]

  if (parentNetworkCommunicationActorRef != null) {
    parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
  }

  logger.info("set replay to "+replayTo)
  inputHub.setReplayTo(replayTo, false)

  override def getLogName: String = getWorkerLogName(actorId)

  def getSenderCredits(sender: ActorVirtualIdentity) = {
    creditMonitor.getSenderCredits(sender)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
  }

  override def receive: Receive = {
    if (!inputHub.recoveryCompleted) {
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
    acceptDirectInvocations orElse forwardResendRequest orElse disallowActorRefRelatedMessages orElse {
      case WorkflowRecoveryMessage(from, TakeLocalCheckpoint()) =>
        val syncFuture = new CompletableFuture[Long]()
        inputHub.addInternal(Checkpoint(networkCommunicationActor.ref, syncFuture))
        sender ! syncFuture.get()
      case WorkflowRecoveryMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case WorkflowRecoveryMessage(from, ContinueReplayTo(index)) =>
        inputHub.setReplayTo(index, true)
        inputHub.addInternal(NoOperation)
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

  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handleControlPayload(SELF, invocation)
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
        inputHub.addControl(ControlElement(controlCommand, from))
      case _ =>
        throw new WorkflowRuntimeException(s"unhandled control payload: $controlPayload")
    }
  }


  override def postStop(): Unit = {
    // shutdown dp thread by sending a command
    val syncFuture = new CompletableFuture[Unit]()
    inputHub.addInternal(Shutdown(None, syncFuture))
    syncFuture.get()
    logger.info("stopped!")
  }

}
