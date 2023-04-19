package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, Stash}
import akka.pattern.StatusReply.Ack
import akka.util.Timeout
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.{CheckInitialized, ResendOutputTo}
import edu.uci.ics.amber.engine.architecture.logging.storage.{DeterminantLogStorage, EmptyLogStorage}
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, EmptyDeterminantLogger, EmptyLogManagerImpl, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetActorRef, NetworkAck, NetworkMessage, NetworkSenderActorRef, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkCommunicationActor, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, ControlInvocation, CreditRequest, InternalChannelEndpointID, OutsideWorldChannelEndpointID, WorkflowDPMessagePayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


object WorkflowActor{
  case class CheckInitialized(setupCommands:Iterable[AmberInternalPayload])
  case class ResendOutputTo()
}

abstract class WorkflowActor(
    val actorId: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
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

  var logStorage: DeterminantLogStorage = new EmptyLogStorage()
  var determinantLogger:DeterminantLogger = new EmptyDeterminantLogger()
  var logManager: LogManager = new EmptyLogManagerImpl(networkCommunicationActor)

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
      logManager.setupWriter(new EmptyLogStorage().getWriter)
      initState()
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }
  }

  def initState(): Unit

  // actor behavior
  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      init.setupCommands.foreach{
        cmd => this.handlePayloadAndMarker(InternalChannelEndpointID, cmd)
      }
      sender ! Ack
  }


  /** Actor lifecycle: Processing */
  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      this.handlePayloadAndMarker(OutsideWorldChannelEndpointID, invocation)
  }

  def acceptDirectInternalCommands:Receive = {
    case internalPayload: AmberInternalPayload =>
      this.handlePayloadAndMarker(InternalChannelEndpointID, internalPayload)
  }

  // custom logic for payload handling (override by Worker and Controller)
  def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowFIFOMessagePayloadWithPiggyback): Unit

  override def receive: Receive = {
    forwardResendRequest orElse
      disallowActorRefRelatedMessages orElse
      acceptDirectInvocations orElse
      acceptDirectInternalCommands orElse
      acceptInitializationMessage orElse
      receiveFIFOMessage orElse
      handleCreditRequest
  }


  override def postStop(): Unit = {
    logManager.terminate()
    logStorage.deleteLog()
  }

}
