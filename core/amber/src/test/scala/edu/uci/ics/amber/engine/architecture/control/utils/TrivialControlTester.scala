package edu.uci.ics.amber.engine.architecture.control.utils

import akka.actor.ActorRef
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{
  NetworkMessage,
  NetworkSenderActorRef
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.common.Constants
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, WorkflowControlMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{
  AsyncRPCClient,
  AsyncRPCHandlerInitializer,
  AsyncRPCServer
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.error.ErrorUtils.safely

class TrivialControlTester(
    id: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(id, parentNetworkCommunicationActorRef, false)
    with TesterAsyncRPCHandlerInitializer {

  lazy val controlInputPort: NetworkInputPort[ControlPayload] =
    new NetworkInputPort[ControlPayload](id, this.handleControlPayloadWithTryCatch)

  def outputControlPayload(
      to: ActorVirtualIdentity,
      self: ActorVirtualIdentity,
      seqNum: Long,
      payload: ControlPayload
  ): Unit = {
    val msg = WorkflowControlMessage(self, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }
  lazy val controlOutputPort: NetworkOutputPort[ControlPayload] = {
    new NetworkOutputPort[ControlPayload](this.actorId, this.outputControlPayload)
  }

  val asyncRPCServer: AsyncRPCServer = wire[AsyncRPCServer]
  val asyncRPCClient: AsyncRPCClient = wire[AsyncRPCClient]

  override def receive: Receive = {
    disallowActorRefRelatedMessages orElse {
      case NetworkMessage(
            id,
            internalMessage @ WorkflowControlMessage(from, sequenceNumber, payload)
          ) =>
        logger.info(s"received $internalMessage")
        this.controlInputPort.handleMessage(
          this.sender(),
          Constants.unprocessedBatchesCreditLimitPerSender,
          id,
          from,
          sequenceNumber,
          payload
        )
      case other =>
        logger.info(s"unhandled message: $other")
    }
  }

  override def postStop(): Unit = {
    logManager.terminate()
    logStorage.deleteLog()
  }

  def handleControlPayloadWithTryCatch(
      from: ActorVirtualIdentity,
      controlPayload: ControlPayload
  ): Unit = {
    try {
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
          logger.error(s"unhandled control message: $other")
      }
    } catch safely {
      case e =>
        logger.error("", e)
    }

  }
}
