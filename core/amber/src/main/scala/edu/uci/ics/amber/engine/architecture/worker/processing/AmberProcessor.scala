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

  def init(logManager: LogManager): Unit ={
    this.logManager = logManager
  }

  def updateInputChannel(channelEndpointID: ChannelEndpointID): Unit ={
    if(currentInputChannel != channelEndpointID){
      currentInputChannel = channelEndpointID
    }
  }

  def outputPayload(
                     to: ActorVirtualIdentity,
                     msg:WorkflowFIFOMessage
                   ): Unit = {
    logManager.sendCommitted(SendRequest(to, msg))
  }


  def skipFaultTolerance(payload: ControlPayload): Boolean ={
    payload match {
      case invocation: ControlInvocation => invocation.isInstanceOf[SkipFaultTolerance]
      case ret: ReturnInvocation => ret.skipFaultTolerance
    }
  }

  def processControlPayload(
                             channel:ChannelEndpointID,
                             payload: ControlPayload
                           ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
    updateInputChannel(channel)
    if(!skipFaultTolerance(payload)){
      determinantLogger.setCurrentSender(channel)
      determinantLogger.stepIncrement()
    }
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
