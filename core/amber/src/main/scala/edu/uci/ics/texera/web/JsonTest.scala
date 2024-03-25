package edu.uci.ics.texera.web

import edu.uci.ics.amber.engine.common.actormessage.{GreeterGrpc, HelloReply, HelloRequest}
import io.grpc.{CallOptions, Channel, ClientCall, ManagedChannelBuilder, Metadata, MethodDescriptor, Server, ServerBuilder, ServerCall, Status}

import scala.collection.mutable
import scala.concurrent.{Await, CanAwait, Future}
import scala.concurrent.duration.DurationInt

object JsonTest {

  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(req: HelloRequest): Future[HelloReply] = {
      val reply = HelloReply(message = "Hello " + req.name)
      Future.successful(reply)
    }
  }
  var handlers: PartialFunction[Any, Any] = PartialFunction.empty

  def registerHandler[RequestT, ResponseT](
                                            call: RequestT => ResponseT
                     ): Unit = {
    handlers = handlers orElse{
        case a: RequestT =>
          call(a)
    }
  }

  registerHandler(new GreeterImpl().sayHello)


  def main(args: Array[String]): Unit = {
    val channel = new Channel {
      override def newCall[RequestT, ResponseT](methodDescriptor: MethodDescriptor[RequestT, ResponseT], callOptions: CallOptions): ClientCall[RequestT, ResponseT] = {
        val responses = new mutable.HashMap[Long, ClientCall.Listener[ResponseT]]
        new ClientCall[RequestT, ResponseT] {
          override def start(responseListener: ClientCall.Listener[ResponseT], headers: Metadata): Unit = {
            //println(s"calling start with $responseListener, $headers")
            responses(1) = responseListener
            responses(1).onReady()
            responses(1).onHeaders(headers)
          }

          override def request(numMessages: Int): Unit = {
            //println(s"calling request with numMsg = $numMessages")
          }

          override def cancel(message: String, cause: Throwable): Unit = {
            //println(s"calling cancel with message = $message")

          }

          override def halfClose(): Unit = {
            //println(s"calling half close")
          }

          override def sendMessage(message: RequestT): Unit = {
            //println(s"calling sendMessage with $message")
            val f = Await.result(handlers.apply(message).asInstanceOf[Future[_]], 3.minutes)
            responses(1).onMessage(f.asInstanceOf[ResponseT])
            responses(1).onClose(Status.OK, new Metadata())
          }
        }
      }

      override def authority(): String = ""
    }
    val request = HelloRequest(name = "World")
    val blockingStub = GreeterGrpc.blockingStub(channel)
    val reply: HelloReply = blockingStub.sayHello(request)
    println(reply)

  }
}

class JsonTest {}
