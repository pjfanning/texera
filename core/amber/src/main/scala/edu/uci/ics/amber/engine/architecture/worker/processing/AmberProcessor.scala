package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.recovery.InternalPayloadHandler
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{AmberInternalPayload, ChannelEndpointID, ControlPayload, IdempotentInternalPayload, MarkerAlignmentInternalPayload, OneTimeInternalPayload, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipConsoleLog
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


abstract class AmberProcessor(val actorId:ActorVirtualIdentity, val determinantLogger: DeterminantLogger)  extends AmberLogging
  with Serializable {

  @transient var logManager: LogManager = _

  // 1. Unified Output
  lazy val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  // 2. RPC Layer
  lazy val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  lazy val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)

  protected var currentInputChannel:ChannelEndpointID = _

  def internalPayloadHandler: InternalPayloadHandler

  def init(logManager: LogManager): Unit ={
    this.logManager = logManager
  }

  def updateInputChannelThenDoLogging(channelEndpointID: ChannelEndpointID): Unit ={
    if(currentInputChannel != channelEndpointID){
      determinantLogger.setCurrentSender(channelEndpointID)
      currentInputChannel = channelEndpointID
    }
    determinantLogger.stepIncrement()
  }

  def outputPayload(
                     to: ActorVirtualIdentity,
                     msg:WorkflowFIFOMessage
                   ): Unit = {
    logManager.sendCommitted(SendRequest(to, msg))
  }

  def processInternalPayload(internalPayload: AmberInternalPayload): Unit ={
    internalPayloadHandler.process(internalPayload)
  }

  def processControlPayload(
                             channel:ChannelEndpointID,
                             payload: ControlPayload
                           ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
    updateInputChannelThenDoLogging(channel)
    payload match {
      case invocation: ControlInvocation =>
        if (!invocation.command.isInstanceOf[SkipConsoleLog]) {
          logger.info(
            s"receive command: ${invocation.command} from $channel (controlID: ${invocation.commandID}, current step = ${determinantLogger.getStep})"
          )
        }
        asyncRPCServer.receive(invocation, channel.endpointWorker)
      case ret: ReturnInvocation =>
        asyncRPCClient.logControlReply(ret, channel.endpointWorker, determinantLogger.getStep)
        asyncRPCClient.fulfillPromise(ret)
    }
  }



}
