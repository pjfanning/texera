package edu.uci.ics.amber.engine.architecture.controller.controllerrpc

object ControllerRpcGrpc {
  val METHOD_ASSIGN_BREAKPOINT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "AssignBreakpoint"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val METHOD_EPOCH_MARKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "EpochMarker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(1)))
      .build()
  
  val METHOD_FATAL_ERROR: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "FatalError"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(2)))
      .build()
  
  val METHOD_LINK_WORKERS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "LinkWorkers"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(3)))
      .build()
  
  val METHOD_LINK_COMPLETED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "LinkCompleted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(4)))
      .build()
  
  val METHOD_LOCAL_BREAKPOINT_TRIGGERED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "LocalBreakpointTriggered"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(5)))
      .build()
  
  val METHOD_LOCAL_OPERATOR_EXCEPTION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "LocalOperatorException"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(6)))
      .build()
  
  val METHOD_MODIFY_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "ModifyLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(7)))
      .build()
  
  val METHOD_PYTHON_CONSOLE_MESSAGE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "PythonConsoleMessage"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(8)))
      .build()
  
  val METHOD_QUERY_WORKER_STATS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "QueryWorkerStats"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(9)))
      .build()
  
  val METHOD_REGIONS_TIME_SLOT_EXPIRED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "RegionsTimeSlotExpired"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(10)))
      .build()
  
  val METHOD_SKEW_DETECTION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "SkewDetection"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(11)))
      .build()
  
  val METHOD_WORKER_EXECUTION_STARTED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "WorkerExecutionStarted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(12)))
      .build()
  
  val METHOD_WORKER_EXECUTION_COMPLETED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "WorkerExecutionCompleted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(13)))
      .build()
  
  val METHOD_WORKER_RPC_CALLS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc", "WorkerRpcCalls"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(14)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("edu.uci.ics.amber.engine.architecture.controller.ControllerRpc")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor))
      .addMethod(METHOD_ASSIGN_BREAKPOINT)
      .addMethod(METHOD_EPOCH_MARKER)
      .addMethod(METHOD_FATAL_ERROR)
      .addMethod(METHOD_LINK_WORKERS)
      .addMethod(METHOD_LINK_COMPLETED)
      .addMethod(METHOD_LOCAL_BREAKPOINT_TRIGGERED)
      .addMethod(METHOD_LOCAL_OPERATOR_EXCEPTION)
      .addMethod(METHOD_MODIFY_LOGIC)
      .addMethod(METHOD_PYTHON_CONSOLE_MESSAGE)
      .addMethod(METHOD_QUERY_WORKER_STATS)
      .addMethod(METHOD_REGIONS_TIME_SLOT_EXPIRED)
      .addMethod(METHOD_SKEW_DETECTION)
      .addMethod(METHOD_WORKER_EXECUTION_STARTED)
      .addMethod(METHOD_WORKER_EXECUTION_COMPLETED)
      .addMethod(METHOD_WORKER_RPC_CALLS)
      .build()
  
  trait ControllerRpc extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = ControllerRpc
    def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse]
    def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse]
    def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse]
    def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse]
    def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse]
    def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse]
    def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse]
  }
  
  object ControllerRpc extends _root_.scalapb.grpc.ServiceCompanion[ControllerRpc] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[ControllerRpc] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: ControllerRpc, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_ASSIGN_BREAKPOINT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse]): Unit =
            serviceImpl.assignBreakpoint(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_EPOCH_MARKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse]): Unit =
            serviceImpl.epochMarker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_FATAL_ERROR,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.fatalError(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LINK_WORKERS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse]): Unit =
            serviceImpl.linkWorkers(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LINK_COMPLETED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.linkCompleted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LOCAL_BREAKPOINT_TRIGGERED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.localBreakpointTriggered(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LOCAL_OPERATOR_EXCEPTION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.localOperatorException(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MODIFY_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse]): Unit =
            serviceImpl.modifyLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PYTHON_CONSOLE_MESSAGE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.pythonConsoleMessage(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_WORKER_STATS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse]): Unit =
            serviceImpl.queryWorkerStats(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_REGIONS_TIME_SLOT_EXPIRED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.regionsTimeSlotExpired(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SKEW_DETECTION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse]): Unit =
            serviceImpl.skewDetection(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_EXECUTION_STARTED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerExecutionStarted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_EXECUTION_COMPLETED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerExecutionCompleted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_RPC_CALLS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest, edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse]): Unit =
            serviceImpl.workerRpcCalls(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  trait ControllerRpcBlockingClient {
    def serviceCompanion = ControllerRpc
    def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse
    def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse
    def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent): com.google.protobuf.empty.Empty
    def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse
    def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent): com.google.protobuf.empty.Empty
    def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent): com.google.protobuf.empty.Empty
    def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent): com.google.protobuf.empty.Empty
    def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse
    def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent): com.google.protobuf.empty.Empty
    def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse
    def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent): com.google.protobuf.empty.Empty
    def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse
    def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent): com.google.protobuf.empty.Empty
    def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent): com.google.protobuf.empty.Empty
    def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse
  }
  
  class ControllerRpcBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[ControllerRpcBlockingStub](channel, options) with ControllerRpcBlockingClient {
    override def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ASSIGN_BREAKPOINT, options, request)
    }
    
    override def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_EPOCH_MARKER, options, request)
    }
    
    override def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_FATAL_ERROR, options, request)
    }
    
    override def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LINK_WORKERS, options, request)
    }
    
    override def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LINK_COMPLETED, options, request)
    }
    
    override def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LOCAL_BREAKPOINT_TRIGGERED, options, request)
    }
    
    override def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LOCAL_OPERATOR_EXCEPTION, options, request)
    }
    
    override def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MODIFY_LOGIC, options, request)
    }
    
    override def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PYTHON_CONSOLE_MESSAGE, options, request)
    }
    
    override def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_WORKER_STATS, options, request)
    }
    
    override def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_REGIONS_TIME_SLOT_EXPIRED, options, request)
    }
    
    override def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SKEW_DETECTION, options, request)
    }
    
    override def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_EXECUTION_STARTED, options, request)
    }
    
    override def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_EXECUTION_COMPLETED, options, request)
    }
    
    override def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest): edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_RPC_CALLS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): ControllerRpcBlockingStub = new ControllerRpcBlockingStub(channel, options)
  }
  
  class ControllerRpcStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[ControllerRpcStub](channel, options) with ControllerRpc {
    override def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.AssignBreakpointResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ASSIGN_BREAKPOINT, options, request)
    }
    
    override def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.EpochMarkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_EPOCH_MARKER, options, request)
    }
    
    override def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.FatalErrorEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_FATAL_ERROR, options, request)
    }
    
    override def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkWorkersResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LINK_WORKERS, options, request)
    }
    
    override def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LinkCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LINK_COMPLETED, options, request)
    }
    
    override def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalBreakpointTriggeredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LOCAL_BREAKPOINT_TRIGGERED, options, request)
    }
    
    override def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.LocalOperatorExceptionEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LOCAL_OPERATOR_EXCEPTION, options, request)
    }
    
    override def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ModifyLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MODIFY_LOGIC, options, request)
    }
    
    override def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.PythonConsoleMessageEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PYTHON_CONSOLE_MESSAGE, options, request)
    }
    
    override def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.QueryWorkerStatsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_WORKER_STATS, options, request)
    }
    
    override def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.RegionsTimeSlotExpiredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_REGIONS_TIME_SLOT_EXPIRED, options, request)
    }
    
    override def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.SkewDetectionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SKEW_DETECTION, options, request)
    }
    
    override def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionStartedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_EXECUTION_STARTED, options, request)
    }
    
    override def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerExecutionCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_EXECUTION_COMPLETED, options, request)
    }
    
    override def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllerrpc.WorkerRpcCallsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_RPC_CALLS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): ControllerRpcStub = new ControllerRpcStub(channel, options)
  }
  
  def bindService(serviceImpl: ControllerRpc, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = ControllerRpc.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): ControllerRpcBlockingStub = new ControllerRpcBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): ControllerRpcStub = new ControllerRpcStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllerrpc.ControllerrpcProto.javaDescriptor.getServices().get(0)
  
}