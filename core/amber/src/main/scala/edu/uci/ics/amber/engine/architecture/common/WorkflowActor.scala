package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorRef, Stash}
import akka.pattern.StatusReply.Ack
import akka.serialization.SerializationExtension
import akka.util.Timeout
import edu.uci.ics.amber.engine.architecture.checkpoint.{CheckpointHolder, SavedCheckpoint}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.{CheckInitialized, ResendOutputTo}
import edu.uci.ics.amber.engine.architecture.logging.storage.{DeterminantLogStorage, EmptyLogStorage}
import edu.uci.ics.amber.engine.architecture.logging.{AsyncLogWriter, DeterminantLogger, DeterminantLoggerImpl, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetActorRef, NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkCommunicationActor, NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.architecture.recovery.{InternalPayloadManager, LocalRecoveryManager}
import edu.uci.ics.amber.engine.architecture.worker.{ReplayConfig, WorkerInternalQueueImpl}
import edu.uci.ics.amber.engine.common.{AmberLogging, AmberUtils, CheckpointSupport}
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, ControlInvocation, ControlPayload, CreditRequest, WorkflowDPMessagePayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.SELF

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


object WorkflowActor{
  case class CheckInitialized()
  case class ResendOutputTo()
}

abstract class WorkflowActor(
    val actorId: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef,
    restoreConfig: ReplayConfig,
    supportFaultTolerance: Boolean
) extends Actor
    with Stash
    with AmberLogging {

  /** Akka Actor related */

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  /** Network sender related */

  val networkCommunicationActor: NetworkSenderActorRef = NetworkSenderActorRef(
    // create a network communication actor on the same machine as the WorkflowActor itself
    context.actorOf(
      NetworkCommunicationActor.props(
        parentNetworkCommunicationActorRef.ref,
        actorId
      )
    )
  )

  def forwardResendRequest: Receive = {
    case resend: ResendOutputTo =>
      networkCommunicationActor ! resend
  }

  def disallowActorRefRelatedMessages: Receive = {
    case GetActorRef =>
      throw new WorkflowRuntimeException(
        "workflow actor should never receive get actor ref message"
      )

    case RegisterActorRef =>
      throw new WorkflowRuntimeException(
        "workflow actor should never receive register actor ref message"
      )
  }

  /** FIFO & exactly once */

  lazy val inputPort: NetworkInputPort = new NetworkInputPort(this.actorId, this.handlePayloadAndMarker)

  // actor behavior for FIFO messages
  def receiveFIFOMessage: Receive ={
    case NetworkMessage(id, workflowMsg @ WorkflowFIFOMessage(channel, _, payload)) =>
      sender ! NetworkAck(id, Some(getSenderCredits(channel.endpointWorker)))
      inputPort.handleMessage(workflowMsg)
  }

  /** Fault-tolerance layer*/

  val logStorage: DeterminantLogStorage = {
    DeterminantLogStorage.getLogStorage(supportFaultTolerance, getLogName)
  }
  val recoveryManager = new LocalRecoveryManager()
  val determinantLogger:DeterminantLogger = DeterminantLogger.getDeterminantLogger(supportFaultTolerance)
  val logManager: LogManager =
    LogManager.getLogManager(supportFaultTolerance, networkCommunicationActor, determinantLogger)
  if (!logStorage.isLogAvailableForRead) {
    logManager.setupWriter(logStorage.getWriter)
  } else {
    logManager.setupWriter(new EmptyLogStorage().getWriter)
  }

  // custom state ser/de support (override by Worker and Controller)
  def internalPayloadManager:InternalPayloadManager

  def handlePayloadAndMarker(channelId:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    payload match {
      case internal: AmberInternalPayload =>
        logger.info(s"process internal payload $internal")
        internalPayloadManager.inputMarker(channelId, internal)
      case payload: WorkflowFIFOMessagePayloadWithPiggyback =>
        // take piggybacked payload out and clear the original payload
        val piggybacked = payload.piggybacked
        payload.piggybacked = null
        internalPayloadManager.inputPayload(channelId, payload)
        handlePayload(channelId, payload)
        if(piggybacked != null){
          handlePayloadAndMarker(channelId, piggybacked)
        }
      case other =>
        logger.warn(s"workflow actor cannot handle $other")
    }
  }

  // Get log file name (override by Worker and Controller)
  def getLogName: String

  /** flow-control */
  def getSenderCredits(actorVirtualIdentity: ActorVirtualIdentity): Int

  // Actor behavior
  def handleCreditRequest: Receive ={
    case NetworkMessage(id, CreditRequest(actor)) =>
      logger.info(s"current credit for actor = $actor is ${getSenderCredits(actor)}")
      sender ! NetworkAck(id, Some(getSenderCredits(actor)))
  }

  /** Worker lifecycle: Initialization */
  override def preStart(): Unit = {
    if (parentNetworkCommunicationActorRef != null) {
      parentNetworkCommunicationActorRef.waitUntil(RegisterActorRef(this.actorId, self))
    }
    try {
      loadState()
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }
  }

  def loadState(): Unit ={
    restoreConfig.fromCheckpoint match {
      case None | Some(0) =>
//        internalMessageHandler.restoreStateFrom(None, restoreConfig.replayTo)
      case Some(alignment) =>
        val chkpt = CheckpointHolder.getCheckpoint(actorId, alignment)
        logger.info("checkpoint found, start loading")
        val startLoadingTime = System.currentTimeMillis()
        // restore state from checkpoint: can be in either replaying or normal processing
        chkpt.attachSerialization(SerializationExtension(context.system))
//        internalMessageHandler.restoreStateFrom(Some(chkpt), restoreConfig.replayTo)
        logger.info(
          s"checkpoint loading complete! loading duration = ${(System.currentTimeMillis() - startLoadingTime) / 1000f}s"
        )
    }
  }

  // actor behavior
  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      sender ! Ack
  }


  /** Actor lifecycle: Processing */
  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handlePayloadAndMarker(ChannelEndpointID(SELF, true), invocation)
  }

  // custom logic for payload handling (override by Worker and Controller)
  def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit

  override def receive: Receive = {
    forwardResendRequest orElse
      disallowActorRefRelatedMessages orElse
      acceptDirectInvocations orElse
      acceptInitializationMessage orElse
      receiveFIFOMessage orElse
      handleCreditRequest
  }


  override def postStop(): Unit = {
    logManager.terminate()
    logStorage.deleteLog()
  }

}
