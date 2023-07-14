package edu.uci.ics.amber.engine.architecture.worker.workercallservice

object WorkerCallServiceGrpc {
  val METHOD_ACCEPT_IMMUTABLE_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "AcceptImmutableState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val METHOD_ADD_PARTITIONING: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "AddPartitioning"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(1)))
      .build()
  
  val METHOD_ASSIGN_LOCAL_BREAKPOINT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "AssignLocalBreakpoint"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(2)))
      .build()
  
  val METHOD_BACK_PRESSURE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "BackPressure"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(3)))
      .build()
  
  val METHOD_FLUSH_NETWORK_BUFFER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "FlushNetworkBuffer"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(4)))
      .build()
  
  val METHOD_MODIFY_OPERATOR_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "ModifyOperatorLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(5)))
      .build()
  
  val METHOD_MONITOR_WORKER_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "MonitorWorkerState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(6)))
      .build()
  
  val METHOD_PAUSE_SKEW_MITIGATION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "PauseSkewMitigation"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(7)))
      .build()
  
  val METHOD_QUERY_AND_REMOVE_BREAKPOINTS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "QueryAndRemoveBreakpoints"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(8)))
      .build()
  
  val METHOD_QUERY_CURRENT_INPUT_TUPLE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "QueryCurrentInputTuple"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(9)))
      .build()
  
  val METHOD_QUERY_STATISTICS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "QueryStatistics"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(10)))
      .build()
  
  val METHOD_START_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "StartWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(11)))
      .build()
  
  val METHOD_PAUSE_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "PauseWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(12)))
      .build()
  
  val METHOD_RESUME_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "ResumeWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(13)))
      .build()
  
  val METHOD_SCHEDULER_TIME_SLOT_EVENT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "SchedulerTimeSlotEvent"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(14)))
      .build()
  
  val METHOD_SEND_IMMUTABLE_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "SendImmutableState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(15)))
      .build()
  
  val METHOD_SHARE_PARTITION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "SharePartition"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(16)))
      .build()
  
  val METHOD_SHUTDOWN_DPTHREAD: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "ShutdownDPThread"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(17)))
      .build()
  
  val METHOD_OPEN_OPERATOR: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "OpenOperator"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(18)))
      .build()
  
  val METHOD_UPDATE_INPUT_LINKING: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "UpdateInputLinking"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(19)))
      .build()
  
  val METHOD_WORKER_PROPAGATE_EPOCH_MARKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "WorkerPropagateEpochMarker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(20)))
      .build()
  
  val METHOD_RETRY_TUPLE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "RetryTuple"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(21)))
      .build()
  
  val METHOD_DEBUG_COMMAND: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "DebugCommand"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(22)))
      .build()
  
  val METHOD_EVALUATE_PYTHON_EXPRESSION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "EvaluatePythonExpression"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(23)))
      .build()
  
  val METHOD_INITIALIZE_OPERATOR_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService", "InitializeOperatorLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0).getMethods().get(24)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("edu.uci.ics.amber.engine.architecture.worker.WorkerCallService")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor))
      .addMethod(METHOD_ACCEPT_IMMUTABLE_STATE)
      .addMethod(METHOD_ADD_PARTITIONING)
      .addMethod(METHOD_ASSIGN_LOCAL_BREAKPOINT)
      .addMethod(METHOD_BACK_PRESSURE)
      .addMethod(METHOD_FLUSH_NETWORK_BUFFER)
      .addMethod(METHOD_MODIFY_OPERATOR_LOGIC)
      .addMethod(METHOD_MONITOR_WORKER_STATE)
      .addMethod(METHOD_PAUSE_SKEW_MITIGATION)
      .addMethod(METHOD_QUERY_AND_REMOVE_BREAKPOINTS)
      .addMethod(METHOD_QUERY_CURRENT_INPUT_TUPLE)
      .addMethod(METHOD_QUERY_STATISTICS)
      .addMethod(METHOD_START_WORKER)
      .addMethod(METHOD_PAUSE_WORKER)
      .addMethod(METHOD_RESUME_WORKER)
      .addMethod(METHOD_SCHEDULER_TIME_SLOT_EVENT)
      .addMethod(METHOD_SEND_IMMUTABLE_STATE)
      .addMethod(METHOD_SHARE_PARTITION)
      .addMethod(METHOD_SHUTDOWN_DPTHREAD)
      .addMethod(METHOD_OPEN_OPERATOR)
      .addMethod(METHOD_UPDATE_INPUT_LINKING)
      .addMethod(METHOD_WORKER_PROPAGATE_EPOCH_MARKER)
      .addMethod(METHOD_RETRY_TUPLE)
      .addMethod(METHOD_DEBUG_COMMAND)
      .addMethod(METHOD_EVALUATE_PYTHON_EXPRESSION)
      .addMethod(METHOD_INITIALIZE_OPERATOR_LOGIC)
      .build()
  
  trait WorkerCallService extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = WorkerCallService
    def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse]
    def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse]
    def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse]
    def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse]
    def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse]
    def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse]
    def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse]
    def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse]
    def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse]
    def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse]
    def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse]
    def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse]
    def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse]
    def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse]
    def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse]
    def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse]
    def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse]
    def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse]
    def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse]
    def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse]
    def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse]
  }
  
  object WorkerCallService extends _root_.scalapb.grpc.ServiceCompanion[WorkerCallService] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[WorkerCallService] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: WorkerCallService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_ACCEPT_IMMUTABLE_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse]): Unit =
            serviceImpl.acceptImmutableState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_ADD_PARTITIONING,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse]): Unit =
            serviceImpl.addPartitioning(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_ASSIGN_LOCAL_BREAKPOINT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse]): Unit =
            serviceImpl.assignLocalBreakpoint(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_BACK_PRESSURE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.backPressure(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_FLUSH_NETWORK_BUFFER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.flushNetworkBuffer(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MODIFY_OPERATOR_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse]): Unit =
            serviceImpl.modifyOperatorLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MONITOR_WORKER_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse]): Unit =
            serviceImpl.monitorWorkerState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PAUSE_SKEW_MITIGATION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse]): Unit =
            serviceImpl.pauseSkewMitigation(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_AND_REMOVE_BREAKPOINTS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse]): Unit =
            serviceImpl.queryAndRemoveBreakpoints(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_CURRENT_INPUT_TUPLE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse]): Unit =
            serviceImpl.queryCurrentInputTuple(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_STATISTICS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse]): Unit =
            serviceImpl.queryStatistics(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_START_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse]): Unit =
            serviceImpl.startWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PAUSE_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse]): Unit =
            serviceImpl.pauseWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_RESUME_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse]): Unit =
            serviceImpl.resumeWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SCHEDULER_TIME_SLOT_EVENT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse]): Unit =
            serviceImpl.schedulerTimeSlotEvent(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SEND_IMMUTABLE_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse]): Unit =
            serviceImpl.sendImmutableState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SHARE_PARTITION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse]): Unit =
            serviceImpl.sharePartition(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SHUTDOWN_DPTHREAD,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.shutdownDPThread(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_OPEN_OPERATOR,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse]): Unit =
            serviceImpl.openOperator(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_UPDATE_INPUT_LINKING,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse]): Unit =
            serviceImpl.updateInputLinking(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_PROPAGATE_EPOCH_MARKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerPropagateEpochMarker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_RETRY_TUPLE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse]): Unit =
            serviceImpl.retryTuple(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_DEBUG_COMMAND,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse]): Unit =
            serviceImpl.debugCommand(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_EVALUATE_PYTHON_EXPRESSION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse]): Unit =
            serviceImpl.evaluatePythonExpression(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_INITIALIZE_OPERATOR_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse]): Unit =
            serviceImpl.initializeOperatorLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  trait WorkerCallServiceBlockingClient {
    def serviceCompanion = WorkerCallService
    def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse
    def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse
    def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse
    def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent): com.google.protobuf.empty.Empty
    def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent): com.google.protobuf.empty.Empty
    def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse
    def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse
    def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse
    def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse
    def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse
    def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse
    def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse
    def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse
    def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse
    def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse
    def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse
    def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse
    def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent): com.google.protobuf.empty.Empty
    def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse
    def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse
    def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest): com.google.protobuf.empty.Empty
    def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse
    def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse
    def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse
    def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse
  }
  
  class WorkerCallServiceBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[WorkerCallServiceBlockingStub](channel, options) with WorkerCallServiceBlockingClient {
    override def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ACCEPT_IMMUTABLE_STATE, options, request)
    }
    
    override def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ADD_PARTITIONING, options, request)
    }
    
    override def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ASSIGN_LOCAL_BREAKPOINT, options, request)
    }
    
    override def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_BACK_PRESSURE, options, request)
    }
    
    override def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_FLUSH_NETWORK_BUFFER, options, request)
    }
    
    override def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MODIFY_OPERATOR_LOGIC, options, request)
    }
    
    override def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MONITOR_WORKER_STATE, options, request)
    }
    
    override def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PAUSE_SKEW_MITIGATION, options, request)
    }
    
    override def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_AND_REMOVE_BREAKPOINTS, options, request)
    }
    
    override def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_CURRENT_INPUT_TUPLE, options, request)
    }
    
    override def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_STATISTICS, options, request)
    }
    
    override def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_START_WORKER, options, request)
    }
    
    override def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PAUSE_WORKER, options, request)
    }
    
    override def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_RESUME_WORKER, options, request)
    }
    
    override def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SCHEDULER_TIME_SLOT_EVENT, options, request)
    }
    
    override def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SEND_IMMUTABLE_STATE, options, request)
    }
    
    override def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SHARE_PARTITION, options, request)
    }
    
    override def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SHUTDOWN_DPTHREAD, options, request)
    }
    
    override def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_OPEN_OPERATOR, options, request)
    }
    
    override def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_UPDATE_INPUT_LINKING, options, request)
    }
    
    override def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_PROPAGATE_EPOCH_MARKER, options, request)
    }
    
    override def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_RETRY_TUPLE, options, request)
    }
    
    override def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_DEBUG_COMMAND, options, request)
    }
    
    override def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_EVALUATE_PYTHON_EXPRESSION, options, request)
    }
    
    override def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_INITIALIZE_OPERATOR_LOGIC, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): WorkerCallServiceBlockingStub = new WorkerCallServiceBlockingStub(channel, options)
  }
  
  class WorkerCallServiceStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[WorkerCallServiceStub](channel, options) with WorkerCallService {
    override def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AcceptImmutableStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ACCEPT_IMMUTABLE_STATE, options, request)
    }
    
    override def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AddPartitioningResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ADD_PARTITIONING, options, request)
    }
    
    override def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.AssignLocalBreakpointResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ASSIGN_LOCAL_BREAKPOINT, options, request)
    }
    
    override def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.BackPressureEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_BACK_PRESSURE, options, request)
    }
    
    override def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.FlushNetworkBufferEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_FLUSH_NETWORK_BUFFER, options, request)
    }
    
    override def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ModifyOperatorLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MODIFY_OPERATOR_LOGIC, options, request)
    }
    
    override def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.MonitorWorkerStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MONITOR_WORKER_STATE, options, request)
    }
    
    override def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseSkewMitigationResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PAUSE_SKEW_MITIGATION, options, request)
    }
    
    override def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryAndRemoveBreakpointsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_AND_REMOVE_BREAKPOINTS, options, request)
    }
    
    override def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryCurrentInputTupleResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_CURRENT_INPUT_TUPLE, options, request)
    }
    
    override def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.QueryStatisticsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_STATISTICS, options, request)
    }
    
    override def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.StartWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_START_WORKER, options, request)
    }
    
    override def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.PauseWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PAUSE_WORKER, options, request)
    }
    
    override def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.ResumeWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_RESUME_WORKER, options, request)
    }
    
    override def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SchedulerTimeSlotEventResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SCHEDULER_TIME_SLOT_EVENT, options, request)
    }
    
    override def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SendImmutableStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SEND_IMMUTABLE_STATE, options, request)
    }
    
    override def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.SharePartitionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SHARE_PARTITION, options, request)
    }
    
    override def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.ShutdownDPThreadEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SHUTDOWN_DPTHREAD, options, request)
    }
    
    override def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.OpenOperatorResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_OPEN_OPERATOR, options, request)
    }
    
    override def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.UpdateInputLinkingResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_UPDATE_INPUT_LINKING, options, request)
    }
    
    override def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkerPropagateEpochMarkerRequest): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_PROPAGATE_EPOCH_MARKER, options, request)
    }
    
    override def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.RetryTupleResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_RETRY_TUPLE, options, request)
    }
    
    override def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.DebugCommandResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_DEBUG_COMMAND, options, request)
    }
    
    override def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.EvaluatePythonExpressionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_EVALUATE_PYTHON_EXPRESSION, options, request)
    }
    
    override def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workercallservice.InitializeOperatorLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_INITIALIZE_OPERATOR_LOGIC, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): WorkerCallServiceStub = new WorkerCallServiceStub(channel, options)
  }
  
  def bindService(serviceImpl: WorkerCallService, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = WorkerCallService.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): WorkerCallServiceBlockingStub = new WorkerCallServiceBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): WorkerCallServiceStub = new WorkerCallServiceStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workercallservice.WorkercallserviceProto.javaDescriptor.getServices().get(0)
  
}