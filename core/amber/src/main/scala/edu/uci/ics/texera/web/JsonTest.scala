package edu.uci.ics.texera.web

import com.twitter.util.Promise
import edu.uci.ics.amber.engine.architecture.controller.Controller
import edu.uci.ics.amber.engine.architecture.messaginglayer.NetworkOutputPort
import edu.uci.ics.amber.engine.architecture.worker.rpctest.GreeterGrpc.Greeter
import edu.uci.ics.amber.engine.architecture.worker.rpctest.{GreeterGrpc, HelloReply, HelloRequest}
import edu.uci.ics.amber.engine.architecture.worker.workercallservice.{OpenOperatorRequest, OpenOperatorResponse, WorkerCallServiceGrpc}
import edu.uci.ics.amber.engine.common.rpc.CurrentThreadExecutionContext
import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.common.virtualidentity.util.CONTROLLER
import edu.uci.ics.texera.web.MethodFinder.findMethodByName
import io.grpc.{CallOptions, Channel, ClientCall, ManagedChannelBuilder, Metadata, MethodDescriptor, ServerBuilder}
import scalapb.GeneratedMessage
import scalapb.grpc.{ClientCalls, ConcreteProtoMethodDescriptorSupplier}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.currentMirror

object MethodFinder {
  def findMethodByName(instance: Any, methodName: String): Option[ru.MethodMirror] = {
    val classSymbol = currentMirror.reflect(instance).symbol
    val classMirror = currentMirror.reflect(instance)

    val methods = classSymbol.toType.members
      .filter(_.isMethod)
      .map(_.asMethod)
      .filter(_.name.toString == methodName)

    if (methods.nonEmpty) {
      val methodSymbol = methods.head
      val methodMirror = classMirror.reflectMethod(methodSymbol)
      Some(methodMirror)
    } else {
      None
    }
  }
}

object JsonTest {

  def main(args: Array[String]): Unit = {

    class GreeterImpl extends WorkerCallServiceGrpc.WorkerCallService {
    }

    trait helloImpl{
      this:GreeterImpl =>
      override def openOperator(request: OpenOperatorRequest): Future[OpenOperatorResponse] = {

      }
    }


    trait helloImpl2{
      this:GreeterImpl =>
      override def sayHello2(req: HelloRequest) = {
        println("sayHello2 called!")
        val reply = HelloReply(message = "Hello2 " + req.name)
        Future.successful(reply)
      }
    }

    val methodMapping = {
      val handlers = new GreeterImpl
      val sev = Greeter.bindService(handlers, new CurrentThreadExecutionContext)
    }

    def handling(from:ActorVirtualIdentity, to:ActorVirtualIdentity, seq:Long, generatedMessage: GeneratedMessage): Unit ={
      methodMapping("SayHello2")(generatedMessage).asInstanceOf[Future[_]].map{
        r =>
          println(r)
      }(new CurrentThreadExecutionContext)
    }

    val output = new NetworkOutputPort[GeneratedMessage](CONTROLLER, handling)
    val client2 = new AsyncRPCClient(output)
    client2.getStub(CONTROLLER).sayHello(HelloRequest("234234"))
  }
}

class JsonTest {}
