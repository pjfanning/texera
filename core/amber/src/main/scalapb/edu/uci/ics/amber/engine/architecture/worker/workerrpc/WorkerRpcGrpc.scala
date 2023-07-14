package edu.uci.ics.amber.engine.architecture.worker.workerrpc

object WorkerRpcGrpc {
  val METHOD_ACCEPT_IMMUTABLE_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "AcceptImmutableState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val METHOD_ADD_PARTITIONING: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "AddPartitioning"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(1)))
      .build()
  
  val METHOD_ASSIGN_LOCAL_BREAKPOINT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "AssignLocalBreakpoint"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(2)))
      .build()
  
  val METHOD_BACK_PRESSURE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "BackPressure"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(3)))
      .build()
  
  val METHOD_FLUSH_NETWORK_BUFFER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "FlushNetworkBuffer"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(4)))
      .build()
  
  val METHOD_MODIFY_OPERATOR_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "ModifyOperatorLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(5)))
      .build()
  
  val METHOD_MONITOR_WORKER_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "MonitorWorkerState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(6)))
      .build()
  
  val METHOD_PAUSE_SKEW_MITIGATION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "PauseSkewMitigation"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(7)))
      .build()
  
  val METHOD_QUERY_AND_REMOVE_BREAKPOINTS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "QueryAndRemoveBreakpoints"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(8)))
      .build()
  
  val METHOD_QUERY_CURRENT_INPUT_TUPLE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "QueryCurrentInputTuple"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(9)))
      .build()
  
  val METHOD_QUERY_STATISTICS: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "QueryStatistics"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(10)))
      .build()
  
  val METHOD_START_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "StartWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(11)))
      .build()
  
  val METHOD_PAUSE_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "PauseWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(12)))
      .build()
  
  val METHOD_RESUME_WORKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "ResumeWorker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(13)))
      .build()
  
  val METHOD_SCHEDULER_TIME_SLOT_EVENT: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "SchedulerTimeSlotEvent"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(14)))
      .build()
  
  val METHOD_SEND_IMMUTABLE_STATE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "SendImmutableState"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(15)))
      .build()
  
  val METHOD_SHARE_PARTITION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "SharePartition"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(16)))
      .build()
  
  val METHOD_SHUTDOWN_DPTHREAD: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "ShutdownDPThread"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(17)))
      .build()
  
  val METHOD_OPEN_OPERATOR: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "OpenOperator"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(18)))
      .build()
  
  val METHOD_UPDATE_INPUT_LINKING: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "UpdateInputLinking"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(19)))
      .build()
  
  val METHOD_WORKER_PROPAGATE_EPOCH_MARKER: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest, com.google.protobuf.empty.Empty] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "WorkerPropagateEpochMarker"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(20)))
      .build()
  
  val METHOD_RETRY_TUPLE: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "RetryTuple"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(21)))
      .build()
  
  val METHOD_DEBUG_COMMAND: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "DebugCommand"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(22)))
      .build()
  
  val METHOD_EVALUATE_PYTHON_EXPRESSION: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "EvaluatePythonExpression"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(23)))
      .build()
  
  val METHOD_INITIALIZE_OPERATOR_LOGIC: _root_.io.grpc.MethodDescriptor[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc", "InitializeOperatorLogic"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0).getMethods().get(24)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("edu.uci.ics.amber.engine.architecture.worker.WorkerRpc")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor))
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
  
  trait WorkerRpc extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = WorkerRpc
    def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse]
    def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse]
    def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse]
    def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse]
    def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse]
    def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse]
    def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse]
    def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse]
    def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse]
    def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse]
    def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse]
    def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse]
    def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse]
    def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse]
    def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse]
    def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse]
    def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse]
    def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest): scala.concurrent.Future[com.google.protobuf.empty.Empty]
    def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse]
    def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse]
    def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse]
    def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse]
  }
  
  object WorkerRpc extends _root_.scalapb.grpc.ServiceCompanion[WorkerRpc] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[WorkerRpc] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: WorkerRpc, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_ACCEPT_IMMUTABLE_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse]): Unit =
            serviceImpl.acceptImmutableState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_ADD_PARTITIONING,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse]): Unit =
            serviceImpl.addPartitioning(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_ASSIGN_LOCAL_BREAKPOINT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse]): Unit =
            serviceImpl.assignLocalBreakpoint(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_BACK_PRESSURE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.backPressure(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_FLUSH_NETWORK_BUFFER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.flushNetworkBuffer(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MODIFY_OPERATOR_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse]): Unit =
            serviceImpl.modifyOperatorLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_MONITOR_WORKER_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse]): Unit =
            serviceImpl.monitorWorkerState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PAUSE_SKEW_MITIGATION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse]): Unit =
            serviceImpl.pauseSkewMitigation(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_AND_REMOVE_BREAKPOINTS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse]): Unit =
            serviceImpl.queryAndRemoveBreakpoints(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_CURRENT_INPUT_TUPLE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse]): Unit =
            serviceImpl.queryCurrentInputTuple(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_QUERY_STATISTICS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse]): Unit =
            serviceImpl.queryStatistics(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_START_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse]): Unit =
            serviceImpl.startWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_PAUSE_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse]): Unit =
            serviceImpl.pauseWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_RESUME_WORKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse]): Unit =
            serviceImpl.resumeWorker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SCHEDULER_TIME_SLOT_EVENT,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse]): Unit =
            serviceImpl.schedulerTimeSlotEvent(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SEND_IMMUTABLE_STATE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse]): Unit =
            serviceImpl.sendImmutableState(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SHARE_PARTITION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse]): Unit =
            serviceImpl.sharePartition(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_SHUTDOWN_DPTHREAD,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.shutdownDPThread(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_OPEN_OPERATOR,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse]): Unit =
            serviceImpl.openOperator(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_UPDATE_INPUT_LINKING,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse]): Unit =
            serviceImpl.updateInputLinking(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_WORKER_PROPAGATE_EPOCH_MARKER,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest, com.google.protobuf.empty.Empty] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.empty.Empty]): Unit =
            serviceImpl.workerPropagateEpochMarker(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_RETRY_TUPLE,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse]): Unit =
            serviceImpl.retryTuple(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_DEBUG_COMMAND,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse]): Unit =
            serviceImpl.debugCommand(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_EVALUATE_PYTHON_EXPRESSION,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse]): Unit =
            serviceImpl.evaluatePythonExpression(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .addMethod(
        METHOD_INITIALIZE_OPERATOR_LOGIC,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest, edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse] {
          override def invoke(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest, observer: _root_.io.grpc.stub.StreamObserver[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse]): Unit =
            serviceImpl.initializeOperatorLogic(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  trait WorkerRpcBlockingClient {
    def serviceCompanion = WorkerRpc
    def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse
    def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse
    def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse
    def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent): com.google.protobuf.empty.Empty
    def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent): com.google.protobuf.empty.Empty
    def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse
    def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse
    def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse
    def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse
    def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse
    def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse
    def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse
    def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse
    def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse
    def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse
    def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse
    def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse
    def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent): com.google.protobuf.empty.Empty
    def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse
    def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse
    def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest): com.google.protobuf.empty.Empty
    def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse
    def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse
    def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse
    def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse
  }
  
  class WorkerRpcBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[WorkerRpcBlockingStub](channel, options) with WorkerRpcBlockingClient {
    override def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ACCEPT_IMMUTABLE_STATE, options, request)
    }
    
    override def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ADD_PARTITIONING, options, request)
    }
    
    override def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_ASSIGN_LOCAL_BREAKPOINT, options, request)
    }
    
    override def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_BACK_PRESSURE, options, request)
    }
    
    override def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_FLUSH_NETWORK_BUFFER, options, request)
    }
    
    override def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MODIFY_OPERATOR_LOGIC, options, request)
    }
    
    override def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_MONITOR_WORKER_STATE, options, request)
    }
    
    override def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PAUSE_SKEW_MITIGATION, options, request)
    }
    
    override def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_AND_REMOVE_BREAKPOINTS, options, request)
    }
    
    override def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_CURRENT_INPUT_TUPLE, options, request)
    }
    
    override def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_QUERY_STATISTICS, options, request)
    }
    
    override def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_START_WORKER, options, request)
    }
    
    override def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_PAUSE_WORKER, options, request)
    }
    
    override def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_RESUME_WORKER, options, request)
    }
    
    override def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SCHEDULER_TIME_SLOT_EVENT, options, request)
    }
    
    override def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SEND_IMMUTABLE_STATE, options, request)
    }
    
    override def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SHARE_PARTITION, options, request)
    }
    
    override def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SHUTDOWN_DPTHREAD, options, request)
    }
    
    override def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_OPEN_OPERATOR, options, request)
    }
    
    override def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_UPDATE_INPUT_LINKING, options, request)
    }
    
    override def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest): com.google.protobuf.empty.Empty = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_WORKER_PROPAGATE_EPOCH_MARKER, options, request)
    }
    
    override def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_RETRY_TUPLE, options, request)
    }
    
    override def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_DEBUG_COMMAND, options, request)
    }
    
    override def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_EVALUATE_PYTHON_EXPRESSION, options, request)
    }
    
    override def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest): edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_INITIALIZE_OPERATOR_LOGIC, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): WorkerRpcBlockingStub = new WorkerRpcBlockingStub(channel, options)
  }
  
  class WorkerRpcStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[WorkerRpcStub](channel, options) with WorkerRpc {
    override def acceptImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AcceptImmutableStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ACCEPT_IMMUTABLE_STATE, options, request)
    }
    
    override def addPartitioning(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AddPartitioningResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ADD_PARTITIONING, options, request)
    }
    
    override def assignLocalBreakpoint(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.AssignLocalBreakpointResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_ASSIGN_LOCAL_BREAKPOINT, options, request)
    }
    
    override def backPressure(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.BackPressureEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_BACK_PRESSURE, options, request)
    }
    
    override def flushNetworkBuffer(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.FlushNetworkBufferEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_FLUSH_NETWORK_BUFFER, options, request)
    }
    
    override def modifyOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ModifyOperatorLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MODIFY_OPERATOR_LOGIC, options, request)
    }
    
    override def monitorWorkerState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.MonitorWorkerStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_MONITOR_WORKER_STATE, options, request)
    }
    
    override def pauseSkewMitigation(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseSkewMitigationResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PAUSE_SKEW_MITIGATION, options, request)
    }
    
    override def queryAndRemoveBreakpoints(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryAndRemoveBreakpointsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_AND_REMOVE_BREAKPOINTS, options, request)
    }
    
    override def queryCurrentInputTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryCurrentInputTupleResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_CURRENT_INPUT_TUPLE, options, request)
    }
    
    override def queryStatistics(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.QueryStatisticsResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_QUERY_STATISTICS, options, request)
    }
    
    override def startWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.StartWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_START_WORKER, options, request)
    }
    
    override def pauseWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.PauseWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_PAUSE_WORKER, options, request)
    }
    
    override def resumeWorker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.ResumeWorkerResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_RESUME_WORKER, options, request)
    }
    
    override def schedulerTimeSlotEvent(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SchedulerTimeSlotEventResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SCHEDULER_TIME_SLOT_EVENT, options, request)
    }
    
    override def sendImmutableState(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SendImmutableStateResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SEND_IMMUTABLE_STATE, options, request)
    }
    
    override def sharePartition(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.SharePartitionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SHARE_PARTITION, options, request)
    }
    
    override def shutdownDPThread(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.ShutdownDPThreadEvent): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SHUTDOWN_DPTHREAD, options, request)
    }
    
    override def openOperator(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.OpenOperatorResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_OPEN_OPERATOR, options, request)
    }
    
    override def updateInputLinking(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.UpdateInputLinkingResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_UPDATE_INPUT_LINKING, options, request)
    }
    
    override def workerPropagateEpochMarker(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerPropagateEpochMarkerRequest): scala.concurrent.Future[com.google.protobuf.empty.Empty] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_WORKER_PROPAGATE_EPOCH_MARKER, options, request)
    }
    
    override def retryTuple(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.RetryTupleResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_RETRY_TUPLE, options, request)
    }
    
    override def debugCommand(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.DebugCommandResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_DEBUG_COMMAND, options, request)
    }
    
    override def evaluatePythonExpression(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.EvaluatePythonExpressionResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_EVALUATE_PYTHON_EXPRESSION, options, request)
    }
    
    override def initializeOperatorLogic(request: edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicRequest): scala.concurrent.Future[edu.uci.ics.amber.engine.architecture.worker.workerrpc.InitializeOperatorLogicResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_INITIALIZE_OPERATOR_LOGIC, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): WorkerRpcStub = new WorkerRpcStub(channel, options)
  }
  
  def bindService(serviceImpl: WorkerRpc, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = WorkerRpc.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): WorkerRpcBlockingStub = new WorkerRpcBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): WorkerRpcStub = new WorkerRpcStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = edu.uci.ics.amber.engine.architecture.worker.workerrpc.WorkerrpcProto.javaDescriptor.getServices().get(0)
  
}