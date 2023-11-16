package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.{Future, Promise}
import com.typesafe.scalalogging.Logger
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputGateway
import edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlException
import edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.amberexception.WorkflowRuntimeException
import edu.uci.ics.amber.engine.common.ambermessage.{ChannelID, ControlPayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, DEBUG, INFO, RPCLogLevel, ReturnInvocation, logControl}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.mutable

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
object AsyncRPCClient {

  final val IgnoreReply = -1

  sealed trait RPCLogLevel
  final case object DEBUG extends RPCLogLevel
  final case object INFO extends RPCLogLevel

  def logControl(logger:Logger, logLevel: RPCLogLevel, logString:String): Unit ={
    logLevel match {
      case AsyncRPCClient.DEBUG =>
        logger.debug(logString)
      case AsyncRPCClient.INFO =>
        logger.info(logString)
    }
  }

  /** The invocation of a control command
    * @param commandID
    * @param command
    * @param logLevel
    */
  case class ControlInvocation(commandID: Long, command: ControlCommand[_], logLevel:RPCLogLevel) extends ControlPayload

  /** The invocation of a return to a promise.
    * @param originalCommandID
    * @param controlReturn
    * @param logLevel
    */
  case class ReturnInvocation(originalCommandID: Long, controlReturn: Any, logLevel:RPCLogLevel) extends ControlPayload

}

class AsyncRPCClient(
    outputGateway: NetworkOutputGateway,
    val actorId: ActorVirtualIdentity
) extends AmberLogging {

  private val unfulfilledPromises = mutable.LongMap[WorkflowPromise[_]]()
  private var promiseID = 0L

  def send[T](cmd: ControlCommand[T], to: ActorVirtualIdentity, logLevel:RPCLogLevel = INFO): Future[T] = {
    val (p, id) = createPromise[T]()
    logControl(logger, logLevel, s"send request: $cmd to $to (controlID: $id)")
    outputGateway.sendTo(to, ControlInvocation(id, cmd, logLevel))
    p
  }

  def sendToClient(cmd: ControlCommand[_]): Unit = {
    outputGateway.sendTo(CLIENT, ControlInvocation(0, cmd, DEBUG))
  }

  private def createPromise[T](): (Promise[T], Long) = {
    promiseID += 1
    val promise = new WorkflowPromise[T]()
    unfulfilledPromises(promiseID) = promise
    (promise, promiseID)
  }

  def fulfillPromise(ret: ReturnInvocation): Unit = {
    if (unfulfilledPromises.contains(ret.originalCommandID)) {
      val p = unfulfilledPromises(ret.originalCommandID)

      ret.controlReturn match {
        case error: Throwable =>
          p.setException(error)
        case ControlException(msg) =>
          p.setException(new WorkflowRuntimeException(msg))
        case _ =>
          p.setValue(ret.controlReturn.asInstanceOf[p.returnType])
      }

      unfulfilledPromises.remove(ret.originalCommandID)
    }
  }

  def logControlReply(ret: ReturnInvocation, channel: ChannelID): Unit = {
    ret.controlReturn match {
      case throwable: Throwable =>
        logger.error(s"received error from $channel", throwable)
      case other =>
        val logString = if (other != null) {
          s"receive reply: ${other.getClass.getSimpleName} from $channel (controlID: ${ret.originalCommandID})"
        } else {
          s"receive reply: null from $channel (controlID: ${ret.originalCommandID})"
        }
        logControl(logger, ret.logLevel, logString)
    }
  }

}
