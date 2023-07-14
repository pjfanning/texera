package edu.uci.ics.amber.engine.common.rpc

import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.worker.rpctest.GreeterGrpc
import edu.uci.ics.amber.engine.common.ambermessage.{ControlPayload, ControlPayloadV2}
import edu.uci.ics.amber.engine.common.rpc.WorkflowPromise
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import io.grpc.{CallOptions, Channel, ClientCall, Metadata, MethodDescriptor}
import scalapb.GeneratedMessage

import scala.collection.mutable

class AsyncRPCClient(controlOutputEndpoint: NetworkOutputPort[ControlPayloadV2]){
  private val unfulfilledPromises = mutable.HashMap[Long, WorkflowPromise[_]]()
  private var promiseID = 0L

  def fulfillPromise(ret:Any, id:Long): Unit ={
    val promise = unfulfilledPromises(id)
    promise.setValue(ret.asInstanceOf[promise.returnType])
  }

  def getStub(dest:ActorVirtualIdentity): GreeterGrpc.GreeterStub ={
    GreeterGrpc.stub(new Channel{
      override def newCall[RequestT, ResponseT](methodDescriptor: MethodDescriptor[RequestT, ResponseT], callOptions: CallOptions): ClientCall[RequestT, ResponseT] = {

        new ClientCall[RequestT, ResponseT] {
          override def start(responseListener: ClientCall.Listener[ResponseT], headers: Metadata): Unit = {
            println(s"start called with header = $headers")
            unfulfilledPromises(promiseID) = new WorkflowPromise(responseListener)
            promiseID += 1
          }

          override def request(numMessages: Int): Unit = {
            println(s"request call with numMessages = $numMessages")
            // no op
          }

          override def cancel(message: String, cause: Throwable): Unit = {
            println(s"cancel the call with message = $message and cause = $cause")
            // no op
          }

          override def halfClose(): Unit = {
            println(s"halfClose called")
            // no op
          }

          override def sendMessage(message: RequestT): Unit = {
            println(s"sendMessage called with message = $message")
            // send message
            controlOutputEndpoint.sendTo(dest, message.asInstanceOf[ControlPayloadV2])
          }
        }
      }

      override def authority(): String = "amber-system"
    })
  }
}