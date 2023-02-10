package edu.uci.ics.amber.engine.architecture.worker

import akka.actor.Props
import akka.pattern.StatusReply.Ack
import akka.serialization.SerializationExtension
import akka.util.Timeout
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.checkpoint.{
  CheckpointHolder,
  SavedCheckpoint,
  SerializedState
}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.deploysemantics.layer.{OpExecConfig, OrdinalMapping}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkAck,
  NetworkMessage,
  NetworkSenderActorRef,
  RegisterActorRef
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  BatchToTupleConverter,
  CreditMonitor,
  CreditMonitorImpl,
  NetworkInputPort
}
import edu.uci.ics.amber.engine.architecture.worker.WorkerInternalQueue.ControlElement
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.{
  CheckInitialized,
  ReplaceRecoveryQueue,
  getWorkerLogName
}
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.NoOpHandler.NoOp
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.ShutdownDPHandler.ShutdownDP
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.TakeCheckpointHandler.TakeCheckpoint
import edu.uci.ics.amber.engine.common.{CheckpointSupport, IOperatorExecutor}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{
  ContinueReplay,
  ContinueReplayTo,
  ControlPayload,
  CreditRequest,
  DataPayload,
  GetOperatorInternalState,
  ResendOutputTo,
  TakeLocalCheckpoint,
  UpdateRecoveryStatus,
  WorkflowControlMessage,
  WorkflowDataMessage,
  WorkflowRecoveryMessage
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.tuple.ITuple
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.{CONTROLLER, SELF}

import java.util.concurrent.CompletableFuture
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object WorkflowWorker {
  def props(
      id: ActorVirtualIdentity,
      workerIndex: Int,
      workerLayer: OpExecConfig,
      parentNetworkCommunicationActorRef: NetworkSenderActorRef,
      supportFaultTolerance: Boolean,
      stateRestoreConfig: StateRestoreConfig
  ): Props =
    Props(
      new WorkflowWorker(
        id,
        workerIndex: Int,
        workerLayer: OpExecConfig,
        parentNetworkCommunicationActorRef,
        supportFaultTolerance,
        stateRestoreConfig
      )
    )

  def getWorkerLogName(id: ActorVirtualIdentity): String = id.name.replace("Worker:", "")

  case class ReplaceRecoveryQueue(syncFuture: CompletableFuture[Unit])

  case class CheckInitialized()

}

class WorkflowWorker(
    actorId: ActorVirtualIdentity,
    workerIndex: Int,
    workerLayer: OpExecConfig,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    supportFaultTolerance: Boolean,
    restoreConfig: StateRestoreConfig
) extends WorkflowActor(actorId, parentNetworkCommunicationActorRef, supportFaultTolerance) {
  val ordinalMapping: OrdinalMapping = workerLayer.ordinalMapping
  var dataProcessor: DataProcessor = wire[DataProcessor]
  lazy val operator: IOperatorExecutor =
    workerLayer.initIOperatorExecutor((workerIndex, workerLayer))
  logger.info(s"Worker:$actorId = ${context.self}")
  lazy val dataInputPort: NetworkInputPort[DataPayload] =
    new NetworkInputPort[DataPayload](this.actorId, this.handleDataPayload)
  lazy val controlInputPort: NetworkInputPort[ControlPayload] =
    new NetworkInputPort[ControlPayload](this.actorId, this.handleControlPayload)
  lazy val tupleProducer: BatchToTupleConverter = wire[BatchToTupleConverter]
  val creditMonitor = new CreditMonitorImpl()
  var inputQueue: WorkerInternalQueue = _
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  if (parentNetworkCommunicationActorRef != null) {
    parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
  }

  override def getLogName: String = getWorkerLogName(actorId)

  def getSenderCredits(sender: ActorVirtualIdentity) = {
    creditMonitor.getSenderCredits(sender)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    logger.error(s"Encountered fatal error, worker is shutting done.", reason)
  }

  override def receive: Receive = {
    // load from checkpoint if available
    var outputIter: Iterator[(ITuple, Option[Int])] = null
    restoreConfig.fromCheckpoint match {
      case Some(alignment) =>
        val chkpt = CheckpointHolder.getCheckpoint(actorId, alignment)
        logger.info("checkpoint found, start loading")
        val startLoadingTime = System.currentTimeMillis()
        //restore state from checkpoint: can be in either replaying or normal processing
        val serialization = SerializationExtension(context.system)
        dataInputPort.setFIFOState(chkpt.load("dataFifoState").toObject(serialization))
        controlInputPort.setFIFOState(
          chkpt.load("controlFifoState").toObject(serialization)
        )
        inputQueue = chkpt.load("inputHubState").toObject(serialization)
        operator match {
          case support: CheckpointSupport =>
            outputIter = support.deserializeState(chkpt, serialization)
          case _ =>
        }
        dataProcessor = chkpt.load("controlState").toObject(serialization)
        logger.info(
          s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime) / 1000f}s"
        )
      case None =>
        inputQueue = new WorkerInternalQueueImpl(creditMonitor)
    }

    // set replay
    restoreConfig.replayTo match {
      case Some(replayTo) =>
        val queue = inputQueue match {
          case impl: RecoveryInternalQueueImpl => impl
          case impl: WorkerInternalQueueImpl =>
            // convert to replay queue if we have normal queue
            val newQueue = new RecoveryInternalQueueImpl(creditMonitor)
            inputQueue = newQueue
            newQueue
        }
        queue.initialize(logStorage.getReader.mkLogRecordIterator(), dataProcessor.totalValidStep,()=>{
          val syncFuture = new CompletableFuture[Unit]()
          context.self ! ReplaceRecoveryQueue(syncFuture)
          syncFuture.get()
        })
        logger.info("set replay to " + replayTo)
        queue.setReplayTo(replayTo)
        recoveryManager.registerOnStart(() => {}
          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(true))
        )
        recoveryManager.setNotifyReplayCallback(() => {}
          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
        )
        recoveryManager.Start()
        recoveryManager.registerOnEnd(() => {
          logger.info("recovery complete! restoring stashed inputs...")
          logManager.terminate()
          logStorage.cleanPartiallyWrittenLogFile()
          logManager.setupWriter(logStorage.getWriter)
          logger.info("stashed inputs restored!")
          // context.parent ! WorkflowRecoveryMessage(actorId, UpdateRecoveryStatus(false))
        })
        val fifoState = recoveryManager.getFIFOState(logStorage.getReader.mkLogRecordIterator())
        controlInputPort.overwriteFIFOSeqNum(fifoState)
      case None =>
        inputQueue match {
          case impl: RecoveryInternalQueueImpl =>
            replaceRecoveryQueue()
          case impl: WorkerInternalQueueImpl =>
            // do nothing
        }
    }
    dataProcessor.initialize(
      operator,
      outputIter,
      inputQueue,
      logStorage,
      logManager,
      recoveryManager,
      context
    )
    dataProcessor.start()
    receiveAndProcessMessages
  }

  def replaceRecoveryQueue(): Unit ={
    val oldInputQueue = inputQueue.asInstanceOf[RecoveryInternalQueueImpl]
    inputQueue = new WorkerInternalQueueImpl(creditMonitor)
    // add unprocessed inputs into new queue
    oldInputQueue.getAllStashedInputs.foreach(inputQueue.enqueueData)
    oldInputQueue.getAllStashedControls.foreach(inputQueue.enqueueCommand)
  }

  def receiveAndProcessMessages: Receive =
    acceptInitializationMessage orElse acceptDirectInvocations orElse forwardResendRequest orElse disallowActorRefRelatedMessages orElse {
      case ReplaceRecoveryQueue(sync) =>
        replaceRecoveryQueue()
        // unblock sync future on DP
        sync.complete(())
      case WorkflowRecoveryMessage(from, TakeLocalCheckpoint()) =>
        val startTime = System.currentTimeMillis()
        val syncFuture = new CompletableFuture[Long]()
        val chkpt = new SavedCheckpoint()
        val serialization = SerializationExtension(context.system)
        chkpt.save(
          "dataFifoState",
          SerializedState.fromObject(dataInputPort.getFIFOState, serialization)
        )
        chkpt.save(
          "controlFifoState",
          SerializedState.fromObject(controlInputPort.getFIFOState, serialization)
        )
        inputQueue.enqueueSystemCommand(TakeCheckpoint(chkpt, serialization, syncFuture))
        sender ! syncFuture.get()
        logger.info(
          s"global checkpoint completed! time spent = ${(System.currentTimeMillis() - startTime) / 1000f}s"
        )
      case WorkflowRecoveryMessage(from, GetOperatorInternalState()) =>
        sender ! operator.getStateInformation
      case WorkflowRecoveryMessage(from, ContinueReplayTo(index)) =>
        assert(inputQueue.isInstanceOf[RecoveryInternalQueueImpl])
        inputQueue.asInstanceOf[RecoveryInternalQueueImpl].setReplayTo(index)
        inputQueue.enqueueSystemCommand(NoOp())
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

  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      sender ! Ack
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
        inputQueue.enqueueCommand(ControlElement(controlCommand, from))
      case _ =>
        throw new WorkflowRuntimeException(s"unhandled control payload: $controlPayload")
    }
  }

  override def postStop(): Unit = {
    // shutdown dp thread by sending a command
    val syncFuture = new CompletableFuture[Unit]()
    inputQueue.enqueueSystemCommand(ShutdownDP(None, syncFuture))
    syncFuture.get()
    logger.info("stopped!")
  }

}
