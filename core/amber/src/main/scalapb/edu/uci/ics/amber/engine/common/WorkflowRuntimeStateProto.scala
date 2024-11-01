// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common

object WorkflowRuntimeStateProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.rpc.ControlCommandsProto,
    edu.uci.ics.amber.engine.architecture.rpc.ControlReturnsProto,
    edu.uci.ics.amber.engine.common.WorkflowMetricsProto,
    edu.uci.ics.amber.engine.common.VirtualIdentityProto,
    com.google.protobuf.timestamp.TimestampProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.common.BreakpointFault,
      edu.uci.ics.amber.engine.common.OperatorBreakpoints,
      edu.uci.ics.amber.engine.common.ExecutionBreakpointStore,
      edu.uci.ics.amber.engine.common.EvaluatedValueList,
      edu.uci.ics.amber.engine.common.OperatorConsole,
      edu.uci.ics.amber.engine.common.ExecutionConsoleStore,
      edu.uci.ics.amber.engine.common.OperatorWorkerMapping,
      edu.uci.ics.amber.engine.common.ExecutionStatsStore,
      edu.uci.ics.amber.engine.common.WorkflowFatalError,
      edu.uci.ics.amber.engine.common.ExecutionMetadataStore
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CjxlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvY29tbW9uL3dvcmtmbG93X3J1bnRpbWVfc3RhdGUucHJvdG8SH2VkdS51Y2kua
  WNzLmFtYmVyLmVuZ2luZS5jb21tb24aQGVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvcnBjL2NvbnRyb2xfY
  29tbWFuZHMucHJvdG8aP2VkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvcnBjL2NvbnRyb2xfcmV0dXJucy5wc
  m90bxo2ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi93b3JrZmxvd19tZXRyaWNzLnByb3RvGjZlZHUvdWNpL2ljcy9hb
  WJlci9lbmdpbmUvY29tbW9uL3ZpcnR1YWxfaWRlbnRpdHkucHJvdG8aH2dvb2dsZS9wcm90b2J1Zi90aW1lc3RhbXAucHJvdG8aF
  XNjYWxhcGIvc2NhbGFwYi5wcm90byK0AgoPQnJlYWtwb2ludEZhdWx0EjAKC3dvcmtlcl9uYW1lGAEgASgJQg/iPwwSCndvcmtlc
  k5hbWVSCndvcmtlck5hbWUSeAoNZmF1bHRlZF90dXBsZRgCIAEoCzJALmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uQ
  nJlYWtwb2ludEZhdWx0LkJyZWFrcG9pbnRUdXBsZUIR4j8OEgxmYXVsdGVkVHVwbGVSDGZhdWx0ZWRUdXBsZRp1Cg9CcmVha3Bva
  W50VHVwbGUSFwoCaWQYASABKANCB+I/BBICaWRSAmlkEicKCGlzX2lucHV0GAIgASgIQgziPwkSB2lzSW5wdXRSB2lzSW5wdXQSI
  AoFdHVwbGUYAyADKAlCCuI/BxIFdHVwbGVSBXR1cGxlIpsBChNPcGVyYXRvckJyZWFrcG9pbnRzEoMBChZ1bnJlc29sdmVkX2JyZ
  WFrcG9pbnRzGAEgAygLMjAuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5CcmVha3BvaW50RmF1bHRCGuI/FxIVdW5yZ
  XNvbHZlZEJyZWFrcG9pbnRzUhV1bnJlc29sdmVkQnJlYWtwb2ludHMirgIKGEV4ZWN1dGlvbkJyZWFrcG9pbnRTdG9yZRKDAQoNb
  3BlcmF0b3JfaW5mbxgBIAMoCzJLLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uRXhlY3V0aW9uQnJlYWtwb2ludFN0b
  3JlLk9wZXJhdG9ySW5mb0VudHJ5QhHiPw4SDG9wZXJhdG9ySW5mb1IMb3BlcmF0b3JJbmZvGosBChFPcGVyYXRvckluZm9FbnRye
  RIaCgNrZXkYASABKAlCCOI/BRIDa2V5UgNrZXkSVgoFdmFsdWUYAiABKAsyNC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tb
  W9uLk9wZXJhdG9yQnJlYWtwb2ludHNCCuI/BxIFdmFsdWVSBXZhbHVlOgI4ASJ0ChJFdmFsdWF0ZWRWYWx1ZUxpc3QSXgoGdmFsd
  WVzGAEgAygLMjkuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuRXZhbHVhdGVkVmFsdWVCC+I/CBIGd
  mFsdWVzUgZ2YWx1ZXMiuwMKD09wZXJhdG9yQ29uc29sZRJ6ChBjb25zb2xlX21lc3NhZ2VzGAEgAygLMjkuZWR1LnVjaS5pY3MuY
  W1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuQ29uc29sZU1lc3NhZ2VCFOI/ERIPY29uc29sZU1lc3NhZ2VzUg9jb25zb2xlT
  WVzc2FnZXMSlwEKFWV2YWx1YXRlX2V4cHJfcmVzdWx0cxgCIAMoCzJJLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uT
  3BlcmF0b3JDb25zb2xlLkV2YWx1YXRlRXhwclJlc3VsdHNFbnRyeUIY4j8VEhNldmFsdWF0ZUV4cHJSZXN1bHRzUhNldmFsdWF0Z
  UV4cHJSZXN1bHRzGpEBChhFdmFsdWF0ZUV4cHJSZXN1bHRzRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa2V5ElUKBXZhb
  HVlGAIgASgLMjMuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5FdmFsdWF0ZWRWYWx1ZUxpc3RCCuI/BxIFdmFsdWVSB
  XZhbHVlOgI4ASKzAgoVRXhlY3V0aW9uQ29uc29sZVN0b3JlEowBChBvcGVyYXRvcl9jb25zb2xlGAEgAygLMksuZWR1LnVjaS5pY
  3MuYW1iZXIuZW5naW5lLmNvbW1vbi5FeGVjdXRpb25Db25zb2xlU3RvcmUuT3BlcmF0b3JDb25zb2xlRW50cnlCFOI/ERIPb3Blc
  mF0b3JDb25zb2xlUg9vcGVyYXRvckNvbnNvbGUaigEKFE9wZXJhdG9yQ29uc29sZUVudHJ5EhoKA2tleRgBIAEoCUII4j8FEgNrZ
  XlSA2tleRJSCgV2YWx1ZRgCIAEoCzIwLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uT3BlcmF0b3JDb25zb2xlQgriP
  wcSBXZhbHVlUgV2YWx1ZToCOAEieAoVT3BlcmF0b3JXb3JrZXJNYXBwaW5nEjAKC29wZXJhdG9yX2lkGAEgASgJQg/iPwwSCm9wZ
  XJhdG9ySWRSCm9wZXJhdG9ySWQSLQoKd29ya2VyX2lkcxgCIAMoCUIO4j8LEgl3b3JrZXJJZHNSCXdvcmtlcklkcyKiBAoTRXhlY
  3V0aW9uU3RhdHNTdG9yZRI8Cg9zdGFydF90aW1lc3RhbXAYASABKANCE+I/EBIOc3RhcnRUaW1lc3RhbXBSDnN0YXJ0VGltZXN0Y
  W1wEjYKDWVuZF90aW1lc3RhbXAYAiABKANCEeI/DhIMZW5kVGltZXN0YW1wUgxlbmRUaW1lc3RhbXASfgoNb3BlcmF0b3JfaW5mb
  xgDIAMoCzJGLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uRXhlY3V0aW9uU3RhdHNTdG9yZS5PcGVyYXRvckluZm9Fb
  nRyeUIR4j8OEgxvcGVyYXRvckluZm9SDG9wZXJhdG9ySW5mbxKKAQoXb3BlcmF0b3Jfd29ya2VyX21hcHBpbmcYBCADKAsyNi5lZ
  HUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLk9wZXJhdG9yV29ya2VyTWFwcGluZ0Ia4j8XEhVvcGVyYXRvcldvcmtlck1hc
  HBpbmdSFW9wZXJhdG9yV29ya2VyTWFwcGluZxqHAQoRT3BlcmF0b3JJbmZvRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa
  2V5ElIKBXZhbHVlGAIgASgLMjAuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5PcGVyYXRvck1ldHJpY3NCCuI/BxIFd
  mFsdWVSBXZhbHVlOgI4ASLfAgoSV29ya2Zsb3dGYXRhbEVycm9yEk4KBHR5cGUYASABKA4yLy5lZHUudWNpLmljcy5hbWJlci5lb
  mdpbmUuY29tbW9uLkZhdGFsRXJyb3JUeXBlQgniPwYSBHR5cGVSBHR5cGUSSwoJdGltZXN0YW1wGAIgASgLMhouZ29vZ2xlLnByb
  3RvYnVmLlRpbWVzdGFtcEIR4j8OEgl0aW1lc3RhbXDwAQFSCXRpbWVzdGFtcBImCgdtZXNzYWdlGAMgASgJQgziPwkSB21lc3NhZ
  2VSB21lc3NhZ2USJgoHZGV0YWlscxgEIAEoCUIM4j8JEgdkZXRhaWxzUgdkZXRhaWxzEjAKC29wZXJhdG9yX2lkGAUgASgJQg/iP
  wwSCm9wZXJhdG9ySWRSCm9wZXJhdG9ySWQSKgoJd29ya2VyX2lkGAYgASgJQg3iPwoSCHdvcmtlcklkUgh3b3JrZXJJZCKCAwoWR
  XhlY3V0aW9uTWV0YWRhdGFTdG9yZRJaCgVzdGF0ZRgBIAEoDjI4LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uV29ya
  2Zsb3dBZ2dyZWdhdGVkU3RhdGVCCuI/BxIFc3RhdGVSBXN0YXRlEmgKDGZhdGFsX2Vycm9ycxgCIAMoCzIzLmVkdS51Y2kuaWNzL
  mFtYmVyLmVuZ2luZS5jb21tb24uV29ya2Zsb3dGYXRhbEVycm9yQhDiPw0SC2ZhdGFsRXJyb3JzUgtmYXRhbEVycm9ycxJqCgxle
  GVjdXRpb25faWQYAyABKAsyMi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkV4ZWN1dGlvbklkZW50aXR5QhPiPxASC
  2V4ZWN1dGlvbklk8AEBUgtleGVjdXRpb25JZBI2Cg1pc19yZWNvdmVyaW5nGAQgASgIQhHiPw4SDGlzUmVjb3ZlcmluZ1IMaXNSZ
  WNvdmVyaW5nKm4KDkZhdGFsRXJyb3JUeXBlEi0KEUNPTVBJTEFUSU9OX0VSUk9SEAAaFuI/ExIRQ09NUElMQVRJT05fRVJST1ISL
  QoRRVhFQ1VUSU9OX0ZBSUxVUkUQARoW4j8TEhFFWEVDVVRJT05fRkFJTFVSRUIL4j8IEAFIAFgAeABiBnByb3RvMw=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.rpc.ControlCommandsProto.javaDescriptor,
      edu.uci.ics.amber.engine.architecture.rpc.ControlReturnsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.WorkflowMetricsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.VirtualIdentityProto.javaDescriptor,
      com.google.protobuf.timestamp.TimestampProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}