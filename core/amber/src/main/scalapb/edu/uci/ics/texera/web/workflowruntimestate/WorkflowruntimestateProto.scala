// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

object WorkflowruntimestateProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto,
    edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlcommandsProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.texera.web.workflowruntimestate.BreakpointFault,
      edu.uci.ics.texera.web.workflowruntimestate.OperatorBreakpoints,
      edu.uci.ics.texera.web.workflowruntimestate.JobBreakpointStore,
      edu.uci.ics.texera.web.workflowruntimestate.EvaluatedValueList,
      edu.uci.ics.texera.web.workflowruntimestate.PythonOperatorInfo,
      edu.uci.ics.texera.web.workflowruntimestate.PythonWorkerInfo,
      edu.uci.ics.texera.web.workflowruntimestate.JobPythonStore,
      edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats,
      edu.uci.ics.texera.web.workflowruntimestate.WorkerRuntimeStats,
      edu.uci.ics.texera.web.workflowruntimestate.JobStatsStore,
      edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Ci1lZHUvdWNpL2ljcy90ZXhlcmEvd29ya2Zsb3dydW50aW1lc3RhdGUucHJvdG8SFmVkdS51Y2kuaWNzLnRleGVyYS53ZWIaQ
  WVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvd29ya2VyL2NvbnRyb2xyZXR1cm5zLnByb3RvGkJlZHUvdWNpL
  2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9sY29tbWFuZHMucHJvdG8aFXNjYWxhcGIvc2NhbGFwY
  i5wcm90byLTAgoPQnJlYWtwb2ludEZhdWx0Ei0KCmFjdG9yX3BhdGgYASABKAlCDuI/CxIJYWN0b3JQYXRoUglhY3RvclBhdGgSb
  woNZmF1bHRlZF90dXBsZRgCIAEoCzI3LmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuQnJlYWtwb2ludEZhdWx0LkJyZWFrcG9pbnRUd
  XBsZUIR4j8OEgxmYXVsdGVkVHVwbGVSDGZhdWx0ZWRUdXBsZRIpCghtZXNzYWdlcxgDIAMoCUIN4j8KEghtZXNzYWdlc1IIbWVzc
  2FnZXMadQoPQnJlYWtwb2ludFR1cGxlEhcKAmlkGAEgASgDQgfiPwQSAmlkUgJpZBInCghpc19pbnB1dBgCIAEoCEIM4j8JEgdpc
  0lucHV0Ugdpc0lucHV0EiAKBXR1cGxlGAMgAygJQgriPwcSBXR1cGxlUgV0dXBsZSKRAQoTT3BlcmF0b3JCcmVha3BvaW50cxJ6C
  hZ1bnJlc29sdmVkX2JyZWFrcG9pbnRzGAEgAygLMicuZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5CcmVha3BvaW50RmF1bHRCGuI/F
  xIVdW5yZXNvbHZlZEJyZWFrcG9pbnRzUhV1bnJlc29sdmVkQnJlYWtwb2ludHMijwIKEkpvYkJyZWFrcG9pbnRTdG9yZRJ0Cg1vc
  GVyYXRvcl9pbmZvGAEgAygLMjwuZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5Kb2JCcmVha3BvaW50U3RvcmUuT3BlcmF0b3JJbmZvR
  W50cnlCEeI/DhIMb3BlcmF0b3JJbmZvUgxvcGVyYXRvckluZm8aggEKEU9wZXJhdG9ySW5mb0VudHJ5EhoKA2tleRgBIAEoCUII4
  j8FEgNrZXlSA2tleRJNCgV2YWx1ZRgCIAEoCzIrLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuT3BlcmF0b3JCcmVha3BvaW50c0IK4
  j8HEgV2YWx1ZVIFdmFsdWU6AjgBIncKEkV2YWx1YXRlZFZhbHVlTGlzdBJhCgZ2YWx1ZXMYASADKAsyPC5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5FdmFsdWF0ZWRWYWx1ZUIL4j8IEgZ2YWx1ZXNSBnZhbHVlcyKgBAoSUHl0a
  G9uT3BlcmF0b3JJbmZvEmwKC3dvcmtlcl9pbmZvGAEgAygLMjouZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5QeXRob25PcGVyYXRvc
  kluZm8uV29ya2VySW5mb0VudHJ5Qg/iPwwSCndvcmtlckluZm9SCndvcmtlckluZm8SkQEKFWV2YWx1YXRlX2V4cHJfcmVzdWx0c
  xgCIAMoCzJDLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuUHl0aG9uT3BlcmF0b3JJbmZvLkV2YWx1YXRlRXhwclJlc3VsdHNFbnRye
  UIY4j8VEhNldmFsdWF0ZUV4cHJSZXN1bHRzUhNldmFsdWF0ZUV4cHJSZXN1bHRzGn0KD1dvcmtlckluZm9FbnRyeRIaCgNrZXkYA
  SABKAlCCOI/BRIDa2V5UgNrZXkSSgoFdmFsdWUYAiABKAsyKC5lZHUudWNpLmljcy50ZXhlcmEud2ViLlB5dGhvbldvcmtlckluZ
  m9CCuI/BxIFdmFsdWVSBXZhbHVlOgI4ARqIAQoYRXZhbHVhdGVFeHByUmVzdWx0c0VudHJ5EhoKA2tleRgBIAEoCUII4j8FEgNrZ
  XlSA2tleRJMCgV2YWx1ZRgCIAEoCzIqLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuRXZhbHVhdGVkVmFsdWVMaXN0QgriPwcSBXZhb
  HVlUgV2YWx1ZToCOAEiqgEKEFB5dGhvbldvcmtlckluZm8SlQEKFnB5dGhvbl9jb25zb2xlX21lc3NhZ2UYASADKAsyRC5lZHUud
  WNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5QeXRob25Db25zb2xlTWVzc2FnZVYyQhniPxYSFHB5dGhvb
  kNvbnNvbGVNZXNzYWdlUhRweXRob25Db25zb2xlTWVzc2FnZSKGAgoOSm9iUHl0aG9uU3RvcmUScAoNb3BlcmF0b3JfaW5mbxgBI
  AMoCzI4LmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuSm9iUHl0aG9uU3RvcmUuT3BlcmF0b3JJbmZvRW50cnlCEeI/DhIMb3BlcmF0b
  3JJbmZvUgxvcGVyYXRvckluZm8agQEKEU9wZXJhdG9ySW5mb0VudHJ5EhoKA2tleRgBIAEoCUII4j8FEgNrZXlSA2tleRJMCgV2Y
  Wx1ZRgCIAEoCzIqLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuUHl0aG9uT3BlcmF0b3JJbmZvQgriPwcSBXZhbHVlUgV2YWx1ZToCO
  AEiwQMKFE9wZXJhdG9yUnVudGltZVN0YXRzElEKBXN0YXRlGAEgASgOMi8uZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5Xb3JrZmxvd
  0FnZ3JlZ2F0ZWRTdGF0ZUIK4j8HEgVzdGF0ZVIFc3RhdGUSMAoLaW5wdXRfY291bnQYAiABKANCD+I/DBIKaW5wdXRDb3VudFIKa
  W5wdXRDb3VudBIzCgxvdXRwdXRfY291bnQYAyABKANCEOI/DRILb3V0cHV0Q291bnRSC291dHB1dENvdW50Em4KC3dvcmtlcl9pb
  mZvGAQgAygLMjwuZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5PcGVyYXRvclJ1bnRpbWVTdGF0cy5Xb3JrZXJJbmZvRW50cnlCD+I/D
  BIKd29ya2VySW5mb1IKd29ya2VySW5mbxp/Cg9Xb3JrZXJJbmZvRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa2V5EkwKB
  XZhbHVlGAIgASgLMiouZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5Xb3JrZXJSdW50aW1lU3RhdHNCCuI/BxIFdmFsdWVSBXZhbHVlO
  gI4ASIUChJXb3JrZXJSdW50aW1lU3RhdHMihgIKDUpvYlN0YXRzU3RvcmUSbwoNb3BlcmF0b3JfaW5mbxgDIAMoCzI3LmVkdS51Y
  2kuaWNzLnRleGVyYS53ZWIuSm9iU3RhdHNTdG9yZS5PcGVyYXRvckluZm9FbnRyeUIR4j8OEgxvcGVyYXRvckluZm9SDG9wZXJhd
  G9ySW5mbxqDAQoRT3BlcmF0b3JJbmZvRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa2V5Ek4KBXZhbHVlGAIgASgLMiwuZ
  WR1LnVjaS5pY3MudGV4ZXJhLndlYi5PcGVyYXRvclJ1bnRpbWVTdGF0c0IK4j8HEgV2YWx1ZVIFdmFsdWU6AjgBIqMBChBKb2JNZ
  XRhZGF0YVN0b3JlElEKBXN0YXRlGAEgASgOMi8uZWR1LnVjaS5pY3MudGV4ZXJhLndlYi5Xb3JrZmxvd0FnZ3JlZ2F0ZWRTdGF0Z
  UIK4j8HEgVzdGF0ZVIFc3RhdGUSIAoFZXJyb3IYAiABKAlCCuI/BxIFZXJyb3JSBWVycm9yEhoKA2VpZBgDIAEoA0II4j8FEgNla
  WRSA2VpZCqkAQoXV29ya2Zsb3dBZ2dyZWdhdGVkU3RhdGUSEQoNVU5JTklUSUFMSVpFRBAAEgkKBVJFQURZEAESCwoHUlVOTklOR
  xACEgsKB1BBVVNJTkcQAxIKCgZQQVVTRUQQBBIMCghSRVNVTUlORxAFEg4KClJFQ09WRVJJTkcQBhINCglDT01QTEVURUQQBxILC
  gdBQk9SVEVEEAgSCwoHVU5LTk9XThAJQgniPwZIAFgAeABiBnByb3RvMw=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto.javaDescriptor,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlcommandsProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}