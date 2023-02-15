package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.Future
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.ControlPayload
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{
  ControlCommand,
  SkipConsoleLog,
  SkipReply
}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity

/** Motivation of having a separate module to handle control messages as RPCs:
  * In the old design, every control message and its response are handled by
  * message passing. That means developers need to manually send response back
  * and write proper handlers on the sender side.
  * Writing control messages becomes tedious if we use this way.
  *
  * So we want to implement rpc model on top of message passing.
  * rpc (request-response)
  * remote.callFunctionX().then(response => {
  * })
  * user-api: promise
  *
  * goal: request-response model with multiplexing
  * client: initiate request
  * (web browser, actor that invoke control command)
  * server: handle request, return response
  * (web server, actor that handles control command)
  */
object AsyncRPCServer {

  trait ControlCommand[T]

  trait SkipConsoleLog

  trait SkipFaultTolerance

  trait SkipReply

}

class AsyncRPCServer(
    controlOutputEndpoint: NetworkOutputPort[ControlPayload],
    val actorId: ActorVirtualIdentity
) extends AmberLogging
    with Serializable {

  // all handlers
  protected var handlers: PartialFunction[(ControlCommand[_], ActorVirtualIdentity), Future[_]] =
    PartialFunction.empty

  // note that register handler allows multiple handlers for a control message and uses the latest handler.
  def registerHandler(
      newHandler: PartialFunction[(ControlCommand[_], ActorVirtualIdentity), Future[_]]
  ): Unit = {
    handlers =
      newHandler orElse handlers
  }

  def receive(control: ControlInvocation, senderID: ActorVirtualIdentity): Unit = {
    try {
      execute((control.command, senderID))
        .onSuccess { ret =>
          returnResult(senderID, control, ret)
        }
        .onFailure { err =>
          logger.error("Exception occurred", err)
          returnResult(senderID, control, err)
        }

    } catch {
      case err: Throwable =>
        // if error occurs, return it to the sender.
        returnResult(senderID, control, err)

      // if throw this exception right now, the above message might not be able
      // to be sent out. We do not throw for now.
      //        throw err
    }
  }

  def execute(cmd: (ControlCommand[_], ActorVirtualIdentity)): Future[_] = {
    if (handlers.isDefinedAt(cmd)) {
      handlers(cmd)
    } else {
      throw new RuntimeException(s"No handler registered for control message: ${cmd._1}")
    }
  }

  @inline
  private def returnResult(
      sender: ActorVirtualIdentity,
      control: ControlInvocation,
      ret: Any
  ): Unit = {
    if (!control.command.isInstanceOf[SkipReply]) {
      controlOutputEndpoint.sendTo(sender, ReturnInvocation(control.commandID, ret))
    } else {
      if (ret.isInstanceOf[Throwable]) {
        throw ret.asInstanceOf[Throwable]
      }
    }
  }

  def logControlInvocation(call: ControlInvocation, sender: ActorVirtualIdentity): Unit = {
    if (call.command.isInstanceOf[SkipConsoleLog]) {
      return
    }
    logger.info(
      s"receive command: ${call.command} from $sender (controlID: ${call.commandID})"
    )
  }

}
