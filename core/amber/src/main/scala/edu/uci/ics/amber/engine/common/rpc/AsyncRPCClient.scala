package edu.uci.ics.amber.engine.common.rpc

import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.common.AmberLogging
import edu.uci.ics.amber.engine.common.rpcwrapper.{ControlInvocation, ControlPayload, ControlReturn}
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import io.grpc.{CallOptions, Channel, ClientCall, Metadata, MethodDescriptor}
import scalapb.GeneratedMessage

import scala.collection.mutable

abstract class AsyncRPCClient[T](controlOutputEndpoint: NetworkOutputPort[ControlPayload], val actorId:ActorVirtualIdentity) extends AmberLogging{

  def stubGen: (Channel) => T

  @transient private val generatedStubs = mutable.HashMap[ActorVirtualIdentity, T]()

  def getContext:RpcContext

  private val unfulfilledPromises = mutable.HashMap[Long, WorkflowPromise[_]]()
  private var promiseID = 0L

  def fulfillPromise(ret:ControlReturn): Unit ={
    val promise = unfulfilledPromises(ret.controlId)
    if(ret.enableTracing){
     logger.trace(s"receive $ret")
    }
    promise.setValue(ret.returnValue.asInstanceOf[promise.returnType])
  }


  class AmberRpcChannel(dest:ActorVirtualIdentity) extends Channel{

      override def newCall[RequestT <: GeneratedMessage, ResponseT <: GeneratedMessage](methodDescriptor: MethodDescriptor[RequestT, ResponseT], callOptions: CallOptions): ClientCall[RequestT, ResponseT] = {
        new ClientCall[RequestT, ResponseT] {
          var localPromiseID = 0L
          override def start(responseListener: ClientCall.Listener[ResponseT], headers: Metadata): Unit = {
            unfulfilledPromises(promiseID) = WorkflowPromise(responseListener)
            localPromiseID = promiseID
            promiseID += 1
          }

          override def request(numMessages: Int): Unit = {
            // no op
          }

          override def cancel(message: String, cause: Throwable): Unit = {
            // no op
          }

          override def halfClose(): Unit = {
            // no op
          }

          override def sendMessage(message: RequestT): Unit = {
            // send message
            val invocation = ControlInvocation(methodDescriptor.getFullMethodName, getContext.enabledTracing, localPromiseID, com.google.protobuf.any.Any.pack(message))
            controlOutputEndpoint.sendTo(dest,ControlPayload.defaultInstance.withInvocation(invocation))
          }
        }
      }

      override def authority(): String = "amber-system"
  }


  def getStub(dest:ActorVirtualIdentity): T ={
    if(!generatedStubs.contains(dest)){
      generatedStubs(dest) = stubGen(new AmberRpcChannel(dest))
    }
    generatedStubs(dest)
  }
}