package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.common.{WorkflowActor, WorkflowActorService}
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.{DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlInvocation, ControlPayload, ReturnInvocation, WorkflowFIFOMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipConsoleLog
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


class AmberProcessor(val actorId:ActorVirtualIdentity,
                     val determinantLogger: DeterminantLogger) extends AmberLogging
  with Serializable {

  @transient var logManager: LogManager = _

  @transient var actorService:WorkflowActorService = _

  // 1. Unified Output
  lazy val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  // 2. RPC Layer
  lazy val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  lazy val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)

  protected var currentInputChannel:ChannelEndpointID = _

  def init(actor:WorkflowActor): Unit ={
    this.logManager = actor.logManager
    this.actorService = actor.actorService
  }

  def updateInputChannelThenDoLogging(channelEndpointID: ChannelEndpointID): Unit ={
    determinantLogger.stepIncrement()
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
                           ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
    updateInputChannelThenDoLogging(channel)
    determinantLogger.recordPayload(channel, payload)
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
