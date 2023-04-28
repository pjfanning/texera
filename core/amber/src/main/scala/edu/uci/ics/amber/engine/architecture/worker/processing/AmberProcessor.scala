package edu.uci.ics.amber.engine.architecture.worker.processing

import edu.uci.ics.amber.engine.architecture.common.{WorkflowActor, WorkflowActorService}
import edu.uci.ics.amber.engine.architecture.logging.AsyncLogWriter.SendRequest
import edu.uci.ics.amber.engine.architecture.logging.{ChannelStepCursor, DeterminantLogger, LogManager}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelEndpointID, ControlInvocation, ControlPayload, ReturnInvocation, WorkflowFIFOMessage, WorkflowFIFOMessagePayload, WorkflowFIFOMessagePayloadWithPiggyback, WorkflowMessage}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.SkipConsoleLog
import edu.uci.ics.amber.engine.common.rpc.{AsyncRPCClient, AsyncRPCServer}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity


class AmberProcessor(@transient var actor:WorkflowActor) extends AmberLogging
  with Serializable {

  override def actorId: ActorVirtualIdentity = actor.actorId

  def logManager: LogManager = actor.logManager

  def actorService:WorkflowActorService = actor.actorService

  def determinantLogger: DeterminantLogger = actor.determinantLogger

  def initAP(actor:WorkflowActor): Unit ={
    this.actor = actor
  }

  // 1. Unified Output
  val outputPort: NetworkOutputPort =
    new NetworkOutputPort(this.actorId, this.outputPayload)
  // 2. RPC Layer
  val asyncRPCClient: AsyncRPCClient =
    new AsyncRPCClient(outputPort, actorId)
  val asyncRPCServer: AsyncRPCServer =
    new AsyncRPCServer(outputPort, actorId)

  var cursor = new ChannelStepCursor()

  def doFaultTolerantProcessing(channelEndpointID: ChannelEndpointID, payload:WorkflowFIFOMessagePayloadWithPiggyback)(code: => Unit): Unit ={
    determinantLogger.setCurrentSenderWithPayload(channelEndpointID, cursor.getStep, payload)
    determinantLogger.enableOutputCommit(true)
    cursor.setCurrentChannel(channelEndpointID)
    code
    cursor.stepIncrement()
    determinantLogger.enableOutputCommit(false)
  }

  def outputPayload(
                     to: ActorVirtualIdentity,
                     msg:WorkflowMessage
                   ): Unit = {
    logManager.sendCommitted(SendRequest(to, msg), cursor.getStep)
  }

  def processControlPayload(
                             channel:ChannelEndpointID,
                             payload: ControlPayload
                           ): Unit = {
    // logger.info(s"process control $payload at step $totalValidStep")
    doFaultTolerantProcessing(channel, payload){
      payload match {
        case invocation: ControlInvocation =>
          //if (!invocation.command.isInstanceOf[SkipConsoleLog]) {
            logger.info(
              s"receive command: ${invocation.command} from $channel (controlID: ${invocation.commandID}, current step = ${cursor.getStep})"
            )
          //}
          asyncRPCServer.receive(invocation, channel.endpointWorker)
        case ret: ReturnInvocation =>
          asyncRPCClient.logControlReply(ret, channel.endpointWorker, cursor.getStep)
          asyncRPCClient.fulfillPromise(ret)
      }
    }
  }



}
