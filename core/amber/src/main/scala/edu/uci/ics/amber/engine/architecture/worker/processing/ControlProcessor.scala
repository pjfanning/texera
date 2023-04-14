package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{SkipConsoleLog, SkipFaultTolerance}
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCHandlerInitializer, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


class ControlProcessor(val actorId:ActorVirtualIdentity)  extends AmberLogging
  with Serializable {

  @transient
  private[processing] var logManager: LogManager = _

  var determinantLogger: DeterminantLogger = _

  // 1. Unified Output
  lazy private[processing] val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  // 2. RPC Layer
  lazy private[processing] val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  lazy private[processing] val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)

  // rpc handlers
  @transient
  private[this] var rpcInitializer: AsyncRPCHandlerInitializer = _

  protected var currentInputChannel:ChannelEndpointID = _

  def initCP(rpcInit:AsyncRPCHandlerInitializer, logManager: LogManager): Unit ={
    this.logManager = logManager
    determinantLogger = logManager.getDeterminantLogger
    rpcInitializer = rpcInit
  }

  def updateInputChannel(channelEndpointID: ChannelEndpointID): Unit ={
    if(currentInputChannel != channelEndpointID){
      determinantLogger.setCurrentSender(channelEndpointID)
      currentInputChannel = channelEndpointID
    }
  }

  def outputPayload(
                     to: ActorVirtualIdentity,
                     msg:WorkflowFIFOMessage
                   ): Unit = {
    logManager.sendCommitted(SendRequest(to, msg))
  }

  def processControlPayload(
                             channel:ChannelEndpointID,
                             payload: ControlPayload
                           ): Boolean = {
    // logger.info(s"process control $payload at step $totalValidStep")
    var skipStepIncrement = false
    payload match {
      case invocation: ControlInvocation =>
        if (!invocation.command.isInstanceOf[SkipConsoleLog]) {
          logger.info(
            s"receive command: ${invocation.command} from $channel (controlID: ${invocation.commandID}, current step = ${determinantLogger.getStep})"
          )
        }
        asyncRPCServer.receive(invocation, channel.endpointWorker)
        if (invocation.command.isInstanceOf[SkipFaultTolerance]) {
          skipStepIncrement = true
        }
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, channel.endpointWorker, determinantLogger.getStep)
        asyncRPCClient.fulfillPromise(ret)
    }
    skipStepIncrement
  }



}
