package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  InputGateway,
  NetworkInputGateway,
  NetworkOutputGateway
}
import edu.uci.ics.amber.engine.architecture.worker.WorkflowWorker.MainThreadDelegate
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class AmberProcessor(
    val actorId: ActorVirtualIdentity,
    @transient var outputHandler: Either[MainThreadDelegate, WorkflowFIFOMessage] => Unit
) extends AmberLogging
    with Serializable {

  /** FIFO & exactly once */
  val inputGateway: InputGateway = new NetworkInputGateway(this.actorId)

  // 1. Unified Output
  val outputGateway: NetworkOutputGateway =
    new NetworkOutputGateway(
      this.actorId,
      msg => {
        // done by the same thread
        outputHandler(Right(msg))
      }
    )
  // 2. RPC Layer
  val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputGateway, actorId)
  val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputGateway, actorId)

  def processControlPayload(
      channel: ChannelID,
      payload: ControlPayload
  ): Unit = {
    payload match {
      case invocation: ControlInvocation =>
        asyncRPCServer.receive(invocation, channel.from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, channel)
        asyncRPCClient.fulfillPromise(ret)
    }
  }

}
