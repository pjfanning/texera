package edu.uci.ics.amber.engine.common.actormessage

object GreeterGrpc {
  val METHOD_SAY_HELLO: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.common.actormessage.HelloRequest, edu.uci.ics.amber.engine.common.actormessage.HelloReply] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.common.Greeter", "SayHello"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.common.actormessage.HelloRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.common.actormessage.HelloReply])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.common.actormessage.ActormessageProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("edu.uci.ics.amber.engine.common.Greeter")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(edu.uci.ics.amber.engine.common.actormessage.ActormessageProto.javaDescriptor))
      .addMethod(METHOD_SAY_HELLO)
      .build()
  
  /** The greeting service definition.
    */
  trait Greeter extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = Greeter
    /** Sends a greeting
      */
    def sayHello(request: edu.uci.ics.amber.engine.common.actormessage.HelloRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.common.actormessage.HelloReply]
  }
  
  object Greeter extends _root_.scalapb.grpc.ServiceCompanion[Greeter] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[Greeter] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.common.actormessage.ActormessageProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.common.actormessage.ActormessageProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: Greeter, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_SAY_HELLO,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.common.actormessage.HelloRequest, edu.uci.ics.amber.engine.common.actormessage.HelloReply] {
          override def invoke(request: edu.uci.ics.amber.engine.common.actormessage.HelloRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.common.actormessage.HelloReply]): Unit =
            serviceImpl.sayHello(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  /** The greeting service definition.
    */
  trait GreeterBlockingClient {
    def serviceCompanion = Greeter
    /** Sends a greeting
      */
    def sayHello(request: edu.uci.ics.amber.engine.common.actormessage.HelloRequest): edu.uci.ics.amber.engine.common.actormessage.HelloReply
  }
  
  class GreeterBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[GreeterBlockingStub](channel, options) with GreeterBlockingClient {
    /** Sends a greeting
      */
    override def sayHello(request: edu.uci.ics.amber.engine.common.actormessage.HelloRequest): edu.uci.ics.amber.engine.common.actormessage.HelloReply = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SAY_HELLO, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): GreeterBlockingStub = new GreeterBlockingStub(channel, options)
  }
  
  class GreeterStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[GreeterStub](channel, options) with Greeter {
    /** Sends a greeting
      */
    override def sayHello(request: edu.uci.ics.amber.engine.common.actormessage.HelloRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.common.actormessage.HelloReply] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SAY_HELLO, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): GreeterStub = new GreeterStub(channel, options)
  }
  
  def bindService(serviceImpl: Greeter, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = Greeter.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): GreeterBlockingStub = new GreeterBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): GreeterStub = new GreeterStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.common.actormessage.ActormessageProto.javaDescriptor.getServices().get(0)
  
}