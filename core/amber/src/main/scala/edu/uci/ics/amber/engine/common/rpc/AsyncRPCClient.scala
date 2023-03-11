package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.{Future, Promise}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlException
import edu.uci.ics.amber.engine.architecture.worker.processing.promisehandlers.TakeCheckpointHandler.CheckpointStats
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, SnapshotMarker, WorkflowFIFOMessagePayload}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCClient.{ControlInvocation, ReturnInvocation}
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.{ControlCommand, SkipReply}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CLIENT

import scala.collection.generic.CanBuildFrom
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

  /** The invocation of a control command
    * @param commandID
    * @param command
    */
  object ControlInvocation {
    def apply(controlCommand: ControlCommand[_] with SkipReply): ControlInvocation = {
      ControlInvocation(-1, controlCommand)
    }
  }
  case class ControlInvocation(commandID: Long, command: ControlCommand[_]) extends ControlPayload

  /** The invocation of a return to a promise.
    * @param originalCommandID
    * @param controlReturn
    */
  case class ReturnInvocation(originalCommandID: Long, controlReturn: Any) extends ControlPayload

}

class AsyncRPCClient(
                      controlOutputEndpoint: NetworkOutputPort,
                      val actorId: ActorVirtualIdentity
) extends AmberLogging
    with Serializable {

  private val unfulfilledPromises = mutable.HashMap[Long, WorkflowPromise[_]]()
  private var promiseID = 0L

  class Convertable[T, U](val convertFunc: ControlCommand[T] => U)

  def send[T](cmd: ControlCommand[T], to: ActorVirtualIdentity): Future[T] = {
    val (p, id) = createPromise[T]()
    logger.info(
      s"send request: ${cmd} to $to (controlID: ${id})"
    )
    controlOutputEndpoint.sendTo(to, ControlInvocation(id, cmd))
    p
  }

  def send[T](cmd: ControlCommand[T] with SkipReply, to: ActorVirtualIdentity): Unit = {
    controlOutputEndpoint.sendTo(to, ControlInvocation(cmd))
  }

  def sendToClient(cmd: ControlCommand[_]): Unit = {
    controlOutputEndpoint.sendTo(CLIENT, ControlInvocation(0, cmd))
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
          p.setException(new RuntimeException(msg))
        case _ =>
          p.setValue(ret.controlReturn.asInstanceOf[p.returnType])
      }

      unfulfilledPromises.remove(ret.originalCommandID)
    }
  }

  def logControlReply(
      ret: ReturnInvocation,
      sender: ActorVirtualIdentity,
      currentStep: Long
  ): Unit = {
    if (ret.controlReturn != null) {
      logger.info(
        s"receive reply: ${ret.controlReturn.getClass.getSimpleName} from $sender (controlID: ${ret.originalCommandID}, current step = $currentStep)"
      )
      ret.controlReturn match {
        case throwable: Throwable =>
          throwable.printStackTrace()
        case _ =>
      }
    } else {
      logger.info(
        s"receive reply: null from $sender (controlID: ${ret.originalCommandID}, current step = $currentStep)"
      )
    }
  }

}
