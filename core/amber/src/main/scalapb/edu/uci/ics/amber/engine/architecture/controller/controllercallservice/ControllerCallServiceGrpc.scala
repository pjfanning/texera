package edu.uci.ics.amber.engine.architecture.controller.controllercallservice

object ControllerCallServiceGrpc {
  val METHOD_ASSIGN_BREAKPOINT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "AssignBreakpoint"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val METHOD_EPOCH_MARKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "EpochMarker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(1)))
      .build()
  
  val METHOD_FATAL_ERROR: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "FatalError"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(2)))
      .build()
  
  val METHOD_LINK_WORKERS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "LinkWorkers"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(3)))
      .build()
  
  val METHOD_LINK_COMPLETED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "LinkCompleted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(4)))
      .build()
  
  val METHOD_LOCAL_BREAKPOINT_TRIGGERED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "LocalBreakpointTriggered"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(5)))
      .build()
  
  val METHOD_LOCAL_OPERATOR_EXCEPTION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "LocalOperatorException"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(6)))
      .build()
  
  val METHOD_MODIFY_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "ModifyLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(7)))
      .build()
  
  val METHOD_PYTHON_CONSOLE_MESSAGE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "PythonConsoleMessage"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(8)))
      .build()
  
  val METHOD_QUERY_WORKER_STATS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "QueryWorkerStats"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(9)))
      .build()
  
  val METHOD_REGIONS_TIME_SLOT_EXPIRED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "RegionsTimeSlotExpired"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(10)))
      .build()
  
  val METHOD_SKEW_DETECTION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "SkewDetection"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(11)))
      .build()
  
  val METHOD_WORKER_EXECUTION_STARTED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "WorkerExecutionStarted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(12)))
      .build()
  
  val METHOD_WORKER_EXECUTION_COMPLETED: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "WorkerExecutionCompleted"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(13)))
      .build()
  
  val METHOD_WORKER_RPC_CALLS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService", "WorkerRpcCalls"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(14)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("edu.uci.ics.amber.engine.architecture.controller.ControllerCallService")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor))
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
  
  trait ControllerCallService extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = ControllerCallService
    def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse]
    def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse]
    def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse]
    def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse]
    def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse]
    def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse]
    def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse]
  }
  
  object ControllerCallService extends _root_.scalapb.grpc.ServiceCompanion[ControllerCallService] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[ControllerCallService] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: ControllerCallService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_ASSIGN_BREAKPOINT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse]): Unit =
            serviceImpl.assignBreakpoint(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_EPOCH_MARKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse]): Unit =
            serviceImpl.epochMarker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_FATAL_ERROR,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.fatalError(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LINK_WORKERS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse]): Unit =
            serviceImpl.linkWorkers(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LINK_COMPLETED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.linkCompleted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LOCAL_BREAKPOINT_TRIGGERED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.localBreakpointTriggered(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_LOCAL_OPERATOR_EXCEPTION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.localOperatorException(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MODIFY_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse]): Unit =
            serviceImpl.modifyLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PYTHON_CONSOLE_MESSAGE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.pythonConsoleMessage(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_WORKER_STATS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse]): Unit =
            serviceImpl.queryWorkerStats(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_REGIONS_TIME_SLOT_EXPIRED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.regionsTimeSlotExpired(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SKEW_DETECTION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse]): Unit =
            serviceImpl.skewDetection(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_EXECUTION_STARTED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerExecutionStarted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_EXECUTION_COMPLETED,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerExecutionCompleted(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_RPC_CALLS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest, edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse]): Unit =
            serviceImpl.workerRpcCalls(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  trait ControllerCallServiceBlockingClient {
    def serviceCompanion = ControllerCallService
    def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse
    def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse
    def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent): com.google.protobuf.empty.Empty
    def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse
    def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent): com.google.protobuf.empty.Empty
    def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent): com.google.protobuf.empty.Empty
    def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent): com.google.protobuf.empty.Empty
    def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse
    def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent): com.google.protobuf.empty.Empty
    def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse
    def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent): com.google.protobuf.empty.Empty
    def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse
    def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent): com.google.protobuf.empty.Empty
    def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent): com.google.protobuf.empty.Empty
    def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse
  }
  
  class ControllerCallServiceBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[ControllerCallServiceBlockingStub](channel, options) with ControllerCallServiceBlockingClient {
    override def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ASSIGN_BREAKPOINT, options, request)
    }
    
    override def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_EPOCH_MARKER, options, request)
    }
    
    override def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_FATAL_ERROR, options, request)
    }
    
    override def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LINK_WORKERS, options, request)
    }
    
    override def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LINK_COMPLETED, options, request)
    }
    
    override def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LOCAL_BREAKPOINT_TRIGGERED, options, request)
    }
    
    override def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_LOCAL_OPERATOR_EXCEPTION, options, request)
    }
    
    override def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MODIFY_LOGIC, options, request)
    }
    
    override def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PYTHON_CONSOLE_MESSAGE, options, request)
    }
    
    override def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_WORKER_STATS, options, request)
    }
    
    override def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_REGIONS_TIME_SLOT_EXPIRED, options, request)
    }
    
    override def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SKEW_DETECTION, options, request)
    }
    
    override def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_EXECUTION_STARTED, options, request)
    }
    
    override def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_EXECUTION_COMPLETED, options, request)
    }
    
    override def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest): edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_RPC_CALLS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): ControllerCallServiceBlockingStub = new ControllerCallServiceBlockingStub(channel, options)
  }
  
  class ControllerCallServiceStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[ControllerCallServiceStub](channel, options) with ControllerCallService {
    override def assignBreakpoint(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.AssignBreakpointResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ASSIGN_BREAKPOINT, options, request)
    }
    
    override def epochMarker(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.EpochMarkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_EPOCH_MARKER, options, request)
    }
    
    override def fatalError(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.FatalErrorEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_FATAL_ERROR, options, request)
    }
    
    override def linkWorkers(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkWorkersResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LINK_WORKERS, options, request)
    }
    
    override def linkCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LinkCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LINK_COMPLETED, options, request)
    }
    
    override def localBreakpointTriggered(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalBreakpointTriggeredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LOCAL_BREAKPOINT_TRIGGERED, options, request)
    }
    
    override def localOperatorException(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.LocalOperatorExceptionEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_LOCAL_OPERATOR_EXCEPTION, options, request)
    }
    
    override def modifyLogic(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ModifyLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MODIFY_LOGIC, options, request)
    }
    
    override def pythonConsoleMessage(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.PythonConsoleMessageEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PYTHON_CONSOLE_MESSAGE, options, request)
    }
    
    override def queryWorkerStats(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.QueryWorkerStatsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_WORKER_STATS, options, request)
    }
    
    override def regionsTimeSlotExpired(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.RegionsTimeSlotExpiredEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_REGIONS_TIME_SLOT_EXPIRED, options, request)
    }
    
    override def skewDetection(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.SkewDetectionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SKEW_DETECTION, options, request)
    }
    
    override def workerExecutionStarted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionStartedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_EXECUTION_STARTED, options, request)
    }
    
    override def workerExecutionCompleted(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerExecutionCompletedEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_EXECUTION_COMPLETED, options, request)
    }
    
    override def workerRpcCalls(request: edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.controller.controllercallservice.WorkerRpcCallsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_RPC_CALLS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): ControllerCallServiceStub = new ControllerCallServiceStub(channel, options)
  }
  
  def bindService(serviceImpl: ControllerCallService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = ControllerCallService.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): ControllerCallServiceBlockingStub = new ControllerCallServiceBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): ControllerCallServiceStub = new ControllerCallServiceStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.controller.controllercallservice.ControllercallserviceProto.javaDescriptor.getServices().get(0)
  
}