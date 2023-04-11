package edu.uci.ics.amber.engine.architecture.control.utils

import akka.actor.ActorRef
import com.softwaremill.macwire.wire
import edu.uci.ics.amber.engine.architecture.common.WorkflowActor
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkCommunicationActor.{NetworkAck, NetworkMessage, NetworkSenderActorRef}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkInputPort.AmberChannelID
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, WorkflowFIFOMessage, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCHandlerInitializer, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.error.ErrorUtils.safely

class TrivialControlTester(
    id: ActorVirtualIdentity,
    parentNetworkCommunicationActorRef: NetworkSenderActorRef
) extends WorkflowActor(id, parentNetworkCommunicationActorRef, false) {

  lazy val controlInputPort: NetworkInputPort =
    new NetworkInputPort(id, this.handleControlPayloadWithTryCatch)

  def outputControlPayload(
      to: ActorVirtualIdentity,
      self: ActorVirtualIdentity,
      isData:Boolean,
      seqNum: Long,
      payload: WorkflowFIFOMessagePayload
  ): Unit = {
    val msg = WorkflowFIFOMessage(self, isData, seqNum, payload)
    logManager.sendCommitted(SendRequest(to, msg))
  }
  lazy val controlOutputPort: NetworkOutputPort= {
    new NetworkOutputPort(this.actorId, this.outputControlPayload)
  }

  val asyncRPCServer: AsyncRPCServer = wire[AsyncRPCServer]
  val asyncRPCClient: AsyncRPCClient = wire[AsyncRPCClient]

  override def receive: Receive = {
    disallowActorRefRelatedMessages orElse {
      case NetworkMessage(
            id,
            internalMessage:WorkflowFIFOMessage
          ) =>
        logger.info(s"received $internalMessage")
        sender ! NetworkAck(id, Some(1000))
        this.controlInputPort.handleMessage(internalMessage)
      case other =>
        logger.info(s"unhandled message: $other")
    }
  }

  override def postStop(): Unit = {
    logManager.terminate()
    logStorage.deleteLog()
  }

  def handleControlPayloadWithTryCatch(
      channelId: AmberChannelID,
      controlPayload: WorkflowFIFOMessagePayload
  ): Unit = {
    try {
      controlPayload match {
        // use control input port to pass control messages
        case invocation: ControlInvocation =>
          asyncRPCServer.logControlInvocation(invocation, from, 0)
          asyncRPCServer.receive(invocation, from)
        case ret: ReturnInvocation =>
          asyncRPCClient.logControlReply(ret, from, 0)
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
