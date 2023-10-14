package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorRef, Stash}
import akka.pattern.StatusReply.Ack
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.CheckInitialized
import edu.uci.ics.amber.engine.architecture.logging.storage.{DeterminantLogStorage, EmptyLogStorage}
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, EmptyDeterminantLogger, EmptyLogManagerImpl, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{GetActorRef, NetworkAck, NetworkMessage, RegisterActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkCommunicationActor, NetworkInputPort}
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadManager
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, ControlInvocation, CreditRequest, OutsideWorldChannelEndpointID, WorkflowFIFOMessage, WorkflowFIFOMessagePayload, WorkflowExecutionPayload}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


object WorkflowActor{
  case class CheckInitialized(setupCommands:Iterable[AmberInternalPayload])
}

abstract class WorkflowActor(
    val actorId: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: ActorRef
) extends Actor
    with Stash
    with AmberLogging {

  /** Network sender related */

  val networkCommunicationActor: ActorRef =
    // create a network communication actor on the same machine as the WorkflowActor itself
    context.actorOf(
      NetworkCommunicationActor.props(
        parentNetworkCommunicationActorRef,
        actorId
      )
  )

  /** Akka related */
  val actorService:WorkflowActorService = new WorkflowActorService(this)

  def disallowActorRefRelatedMessages: Receive = {
    case GetActorRef =>
      throw new WorkflowRuntimeException(
        "workflow actor should never receive get actor ref message"
      )

    case RegisterActorRef =>
      logger.error("workflow actor should never receive register actor ref message")
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
  var isReplaying = false

  // custom state ser/de support (override by Worker and Controller)
  def internalPayloadManager:InternalPayloadManager

  def handlePayloadAndMarker(channelId:ChannelEndpointID, payload: WorkflowFIFOMessagePayload): Unit = {
    internalPayloadManager.inputPayload(channelId, payload)
    payload match {
      case internal: AmberInternalPayload =>
        logger.info(s"process internal payload $internal from channel $channelId")
        internalPayloadManager.inputMarker(channelId, internal)
      case payload: WorkflowExecutionPayload =>
        handlePayload(channelId, payload)
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

  def start():Unit = {}

  // actor behavior
  def acceptInitializationMessage: Receive = {
    case init: CheckInitialized =>
      init.setupCommands.foreach{
        cmd => this.handlePayloadAndMarker(OutsideWorldChannelEndpointID, cmd)
      }
      start()
      //sender ! Ack
  }


  /** Actor lifecycle: Processing */
  def acceptDirectInvocations: Receive = {
    case invocation: ControlInvocation =>
      // during replay, cannot take outside world normal control message
      if(!isReplaying){
        this.handlePayloadAndMarker(OutsideWorldChannelEndpointID, invocation)
      }else{
        logger.info(s"doing replay! ignore $invocation from outside world")
      }
  }

  def acceptDirectInternalCommands:Receive = {
    case internalPayload: AmberInternalPayload =>
      this.handlePayloadAndMarker(OutsideWorldChannelEndpointID, internalPayload)
  }

  // custom logic for payload handling (override by Worker and Controller)
  def handlePayload(channelEndpointID: ChannelEndpointID, payload: WorkflowExecutionPayload): Unit

  override def receive: Receive = {
      disallowActorRefRelatedMessages orElse
      acceptDirectInvocations orElse
      acceptDirectInternalCommands orElse
      acceptInitializationMessage orElse
      receiveFIFOMessage orElse
      handleCreditRequest
  }


  override def postStop(): Unit = {
    logManager.terminate()
    //logStorage.deleteLog()
  }

}
