package edu.uci.ics.amber.engine.architecture.common

import edu.uci.ics.amber.engine.architecture.logging.{
  DeterminantLogger,
  EmptyDeterminantLogger,
  EmptyLogManagerImpl,
  LogManager
}
import edu.uci.ics.amber.engine.architecture.logging.storage.{
  DeterminantLogStorage,
  EmptyLogStorage
}
import edu.uci.ics.amber.engine.architecture.messaginglayer.{NetworkInputPort, NetworkOutputPort}
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

class AmberProcessor(
    val actorId: ActorVirtualIdentity,
    @transient var outputHandler: WorkflowFIFOMessage => Unit
) extends AmberLogging
    with Serializable {

  /** FIFO & exactly once */
  lazy val inputPort: NetworkInputPort = new NetworkInputPort(this.actorId)

  /** Fault-tolerance layer */
  var logStorage: DeterminantLogStorage = new EmptyLogStorage()
  var determinantLogger: DeterminantLogger = new EmptyDeterminantLogger()
  var logManager: LogManager = new EmptyLogManagerImpl(outputHandler)
  var isReplaying = false

  // 1. Unified Output
  val outputPort: NetworkOutputPort =
    new NetworkOutputPort(
      this.actorId,
      msg => {
        // done by the same thread
        logManager.sendCommitted(msg, cursor.getStep)
      }
    )
  // 2. RPC Layer
  val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)
  var cursor = new ProcessingStepCursor()

//  protected def outputMessage(workflowFIFOMessage: WorkflowFIFOMessage): Unit = {
//    logManager.sendCommitted(SendRequest(to, msg), cursor.getStep)
//  }

  def processControlPayload(
      channel: ChannelID,
      payload: ControlPayload
  ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
//    doFaultTolerantProcessing(channel, payload){
    payload match {
      case invocation: ControlInvocation =>
        //if (!invocation.command.isInstanceOf[SkipConsoleLog]) {
        logger.info(
          s"receive command: ${invocation.command} from $channel (controlID: ${invocation.commandID}, current step = ${cursor.getStep})"
        )
        //}
        asyncRPCServer.receive(invocation, channel.from)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, channel)
        asyncRPCClient.fulfillPromise(ret)
    }
//    }
  }

}
