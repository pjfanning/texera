package edu.uci.ics.amber.engine.common.rpc

import com.twitter.util.{Future => TwitterFuture}
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.worker.promisehandlers.QueryStatisticsHandler.QueryStatistics
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.rpc.AsyncRPCServer.ControlCommand
import edu.uci.ics.amber.engine.common.rpcwrapper.{ControlInvocation, ControlPayload, ControlReturn}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.texera.web.MethodFinder.findMethodByName
import io.grpc.ServerServiceDefinition
import edu.uci.ics.amber.engine.common.FutureBijection._
import edu.uci.ics.amber.engine.common.ambermessage.ChannelEndpointID
import scalapb.GeneratedMessage

import javax.annotation.processing.Generated
import scala.collection.mutable
import scala.concurrent.Future

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

  def getMethodMapping(instance:Any, sev:ServerServiceDefinition): Map[String, Any => TwitterFuture[_]] ={
    val methods = sev.getMethods
    val map = new mutable.HashMap[String, Any => TwitterFuture[_]]
    methods.forEach(m => {
      val fullMethodName = m.getMethodDescriptor.getFullMethodName
      val localMethodName = m.getMethodDescriptor.getBareMethodName
      val methodName = s"${localMethodName.head.toLower}${localMethodName.tail}"
      val method = findMethodByName(instance, methodName).get
      val wrappedCall = (req:Any) => {method.apply(req).asInstanceOf[Future[_]].asTwitter()}
      map(fullMethodName) = wrappedCall
    })
    map.toMap
  }

}

abstract class AsyncRPCServer(
    controlOutputEndpoint: NetworkOutputPort[ControlPayload],
    val actorId: ActorVirtualIdentity
) extends AmberLogging {

  def methodMapping:Map[String, Any => TwitterFuture[_]]

  def getContext:RpcContext

  def receive(control: ControlInvocation, senderID: ActorVirtualIdentity): Unit = {
    try {
      if(control.enableTracing){
        logger.trace(s"receive $control")
      }
      methodMapping(control.methodName).apply(control.param)
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

  @inline
  private def returnResult(sender: ActorVirtualIdentity, control: ControlInvocation, ret: Any): Unit = {
    val returnValue = ControlReturn(control.controlId, control.enableTracing, com.google.protobuf.any.Any.pack(ret.asInstanceOf[GeneratedMessage]))
    controlOutputEndpoint.sendTo(sender, ControlPayload.defaultInstance.withReturnValue(returnValue))
  }


}
