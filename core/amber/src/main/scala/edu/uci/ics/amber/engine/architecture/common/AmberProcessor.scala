package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.logging.DeterminantLogger
import edu.uci.ics.amber.engine.architecture.messaginglayer.{
  NetworkInputGateway,
  NetworkOutputGateway
}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{
  ChannelID,
  ControlPayload,
  WorkflowFIFOMessage,
  WorkflowFIFOMessagePayload
}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class AmberProcessor(
    val actorId: ActorVirtualIdentity,
    @transient var outputHandler: (WorkflowFIFOMessage, Long) => Unit
) extends AmberLogging
    with Serializable {

  /** FIFO & exactly once */
  lazy val inputGateway: NetworkInputGateway = new NetworkInputGateway(this.actorId)

  // 1. Unified Output
  val outputGateway: NetworkOutputGateway =
    new NetworkOutputGateway(
      this.actorId,
      msg => {
        // done by the same thread
        outputHandler(msg, cursor.getStep)
      }
    )
  // 2. RPC Layer
  val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputGateway, actorId)
  val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputGateway, actorId)
  var cursor = new ProcessingStepCursor()

  def doFaultTolerantProcessing(
      detLogger: DeterminantLogger,
      pickedChannelId: ChannelID,
      payload: WorkflowFIFOMessagePayload
  )(code: => Unit): Unit = {
    detLogger.setCurrentStepWithMessage(pickedChannelId, cursor.getStep, payload)
    cursor.setCurrentChannel(pickedChannelId)
    code
    cursor.stepIncrement()
  }

  def processControlPayload(
      channel: ChannelID,
      payload: ControlPayload
  ): Unit = {
    payload match {
      case invocation: ControlInvocation =>
        logger.info(
          s"receive command: ${invocation.command} from $channel (controlID: ${invocation.commandID}, current step = ${cursor.getStep})"
        )
        asyncRPCServer.receive(invocation, channel.from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, channel)
        asyncRPCClient.fulfillPromise(ret)
    }
  }

}
