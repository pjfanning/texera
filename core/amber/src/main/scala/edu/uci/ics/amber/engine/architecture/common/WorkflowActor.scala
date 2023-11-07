package edu.uci.ics.amber.engine.architecture.common

import akka.actor.{Actor, ActorRef, Stash}
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor.{
  GetActorRef,
  MessageBecomesDeadLetter,
  NetworkAck,
  NetworkMessage,
  RegisterActorRef
}
import edu.uci.ics.amber.engine.common.{AmberLogging, Constants}
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  CreditRequest,
  CreditResponse,
  WorkflowFIFOMessage
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

object WorkflowActor {

  /** Ack for NetworkMessage
    *
    * @param messageId Long, id for a NetworkMessage, used for FIFO and ExactlyOnce
    */
  final case class NetworkAck(
      messageId: Long,
      credits: Int = Constants.unprocessedBatchesSizeLimitInBytesPerWorkerPair
  )

  final case class MessageBecomesDeadLetter(message: WorkflowFIFOMessage)

  /** Identifier <-> ActorRef related messages
    */
  final case class GetActorRef(id: ActorVirtualIdentity, replyTo: Set[ActorRef])

  final case class RegisterActorRef(id: ActorVirtualIdentity, ref: ActorRef)

  /** All outgoing message should be eventually NetworkMessage
    *
    * @param messageId       Long, id for a NetworkMessage, used for FIFO and ExactlyOnce
    * @param internalMessage WorkflowMessage, the message payload
    */
  final case class NetworkMessage(messageId: Long, internalMessage: WorkflowFIFOMessage)
}

abstract class WorkflowActor(val actorId: ActorVirtualIdentity)
    extends Actor
    with Stash
    with AmberLogging {

  /** Akka related */
  val actorService: AkkaActorService = new AkkaActorService(actorId, this.context)
  val actorRefMappingService: AkkaActorRefMappingService = new AkkaActorRefMappingService(
    actorService
  )
  val transferService: AkkaMessageTransferService =
    new AkkaMessageTransferService(actorService, actorRefMappingService, x => {})

  def allowActorRefRelatedMessages: Receive = {
    case GetActorRef(actorId, replyTo) =>
      actorRefMappingService.retrieveActorRef(actorId, replyTo)
    case RegisterActorRef(actorId, ref) =>
      actorRefMappingService.registerActorRef(actorId, ref)
  }

  // actor behavior for FIFO messages
  def receiveMessageAndAck: Receive = {
    case NetworkMessage(id, workflowMsg @ WorkflowFIFOMessage(channel, _, _)) =>
      actorRefMappingService.registerActorRef(channel.from, sender)
      try {
        handleInputMessage(id, workflowMsg)
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          throw e
      }
    case NetworkAck(id, credits) =>
      transferService.receiveAck(id, credits)
  }

  def handleInputMessage(id: Long, workflowMsg: WorkflowFIFOMessage): Unit

  /** flow-control */
  def getSenderCredits(channelEndpointID: ChannelID): Int

  // Actor behavior
  def handleFlowControl: Receive = {
    case CreditRequest(channel) =>
      sender ! CreditResponse(channel, getSenderCredits(channel))
    case CreditResponse(channel, credit) =>
      transferService.updateCredit(channel, credit)
  }

  def handleDeadLetters: Receive = {
    case MessageBecomesDeadLetter(msg) =>
      actorRefMappingService.removeActorRef(msg.channel.from)
  }

  /** Worker lifecycle: Initialization */
  override def preStart(): Unit = {
    try {
      transferService.initialize()
      initState()
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        throw t
    }
  }

  def initState(): Unit

  override def receive: Receive = {
    allowActorRefRelatedMessages orElse
      receiveMessageAndAck orElse
      handleFlowControl
  }

  override def postStop(): Unit = {
    transferService.stop()
  }

}
