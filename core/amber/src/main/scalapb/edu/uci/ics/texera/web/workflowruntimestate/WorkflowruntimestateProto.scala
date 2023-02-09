// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

object WorkflowruntimestateProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto,
    com.google.protobuf.timestamp.TimestampProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.texera.web.workflowruntimestate.BreakpointFault,
      edu.uci.ics.texera.web.workflowruntimestate.OperatorBreakpoints,
      edu.uci.ics.texera.web.workflowruntimestate.JobBreakpointStore,
      edu.uci.ics.texera.web.workflowruntimestate.EvaluatedValueList,
      edu.uci.ics.texera.web.workflowruntimestate.ConsoleMessage,
      edu.uci.ics.texera.web.workflowruntimestate.PythonOperatorInfo,
      edu.uci.ics.texera.web.workflowruntimestate.JobPythonStore,
      edu.uci.ics.texera.web.workflowruntimestate.OperatorWorkerMapping,
      edu.uci.ics.texera.web.workflowruntimestate.OperatorRuntimeStats,
      edu.uci.ics.texera.web.workflowruntimestate.JobStatsStore,
      edu.uci.ics.texera.web.workflowruntimestate.JobMetadataStore
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Ci1lZHUvdWNpL2ljcy90ZXhlcmEvd29ya2Zsb3dydW50aW1lc3RhdGUucHJvdG8SFmVkdS51Y2kuaWNzLnRleGVyYS53ZWIaQ
  WVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvd29ya2VyL2NvbnRyb2xyZXR1cm5zLnByb3RvGh9nb29nbGUvc
  HJvdG9idWYvdGltZXN0YW1wLnByb3RvGhVzY2FsYXBiL3NjYWxhcGIucHJvdG8i0wIKD0JyZWFrcG9pbnRGYXVsdBItCgphY3Rvc
  l9wYXRoGAEgASgJQg7iPwsSCWFjdG9yUGF0aFIJYWN0b3JQYXRoEm8KDWZhdWx0ZWRfdHVwbGUYAiABKAsyNy5lZHUudWNpLmljc
  y50ZXhlcmEud2ViLkJyZWFrcG9pbnRGYXVsdC5CcmVha3BvaW50VHVwbGVCEeI/DhIMZmF1bHRlZFR1cGxlUgxmYXVsdGVkVHVwb
  GUSKQoIbWVzc2FnZXMYAyADKAlCDeI/ChIIbWVzc2FnZXNSCG1lc3NhZ2VzGnUKD0JyZWFrcG9pbnRUdXBsZRIXCgJpZBgBIAEoA
  0IH4j8EEgJpZFICaWQSJwoIaXNfaW5wdXQYAiABKAhCDOI/CRIHaXNJbnB1dFIHaXNJbnB1dBIgCgV0dXBsZRgDIAMoCUIK4j8HE
  gV0dXBsZVIFdHVwbGUikQEKE09wZXJhdG9yQnJlYWtwb2ludHMSegoWdW5yZXNvbHZlZF9icmVha3BvaW50cxgBIAMoCzInLmVkd
  S51Y2kuaWNzLnRleGVyYS53ZWIuQnJlYWtwb2ludEZhdWx0QhriPxcSFXVucmVzb2x2ZWRCcmVha3BvaW50c1IVdW5yZXNvbHZlZ
  EJyZWFrcG9pbnRzIo8CChJKb2JCcmVha3BvaW50U3RvcmUSdAoNb3BlcmF0b3JfaW5mbxgBIAMoCzI8LmVkdS51Y2kuaWNzLnRle
  GVyYS53ZWIuSm9iQnJlYWtwb2ludFN0b3JlLk9wZXJhdG9ySW5mb0VudHJ5QhHiPw4SDG9wZXJhdG9ySW5mb1IMb3BlcmF0b3JJb
  mZvGoIBChFPcGVyYXRvckluZm9FbnRyeRIaCgNrZXkYASABKAlCCOI/BRIDa2V5UgNrZXkSTQoFdmFsdWUYAiABKAsyKy5lZHUud
  WNpLmljcy50ZXhlcmEud2ViLk9wZXJhdG9yQnJlYWtwb2ludHNCCuI/BxIFdmFsdWVSBXZhbHVlOgI4ASJ3ChJFdmFsdWF0ZWRWY
  Wx1ZUxpc3QSYQoGdmFsdWVzGAEgAygLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuRXZhb
  HVhdGVkVmFsdWVCC+I/CBIGdmFsdWVzUgZ2YWx1ZXMi/gEKDkNvbnNvbGVNZXNzYWdlEikKCHdvcmtlcklkGAEgASgJQg3iPwoSC
  HdvcmtlcklkUgh3b3JrZXJJZBJLCgl0aW1lc3RhbXAYAiABKAsyGi5nb29nbGUucHJvdG9idWYuVGltZXN0YW1wQhHiPw4SCXRpb
  WVzdGFtcPABAVIJdGltZXN0YW1wEicKCG1zZ190eXBlGAMgASgJQgziPwkSB21zZ1R5cGVSB21zZ1R5cGUSIwoGc291cmNlGAQgA
  SgJQgviPwgSBnNvdXJjZVIGc291cmNlEiYKB21lc3NhZ2UYBSABKAlCDOI/CRIHbWVzc2FnZVIHbWVzc2FnZSKcAwoSUHl0aG9uT
  3BlcmF0b3JJbmZvEmcKEGNvbnNvbGVfbWVzc2FnZXMYASADKAsyJi5lZHUudWNpLmljcy50ZXhlcmEud2ViLkNvbnNvbGVNZXNzY
  WdlQhTiPxESD2NvbnNvbGVNZXNzYWdlc1IPY29uc29sZU1lc3NhZ2VzEpEBChVldmFsdWF0ZV9leHByX3Jlc3VsdHMYAiADKAsyQ
  y5lZHUudWNpLmljcy50ZXhlcmEud2ViLlB5dGhvbk9wZXJhdG9ySW5mby5FdmFsdWF0ZUV4cHJSZXN1bHRzRW50cnlCGOI/FRITZ
  XZhbHVhdGVFeHByUmVzdWx0c1ITZXZhbHVhdGVFeHByUmVzdWx0cxqIAQoYRXZhbHVhdGVFeHByUmVzdWx0c0VudHJ5EhoKA2tle
  RgBIAEoCUII4j8FEgNrZXlSA2tleRJMCgV2YWx1ZRgCIAEoCzIqLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuRXZhbHVhdGVkVmFsd
  WVMaXN0QgriPwcSBXZhbHVlUgV2YWx1ZToCOAEihgIKDkpvYlB5dGhvblN0b3JlEnAKDW9wZXJhdG9yX2luZm8YASADKAsyOC5lZ
  HUudWNpLmljcy50ZXhlcmEud2ViLkpvYlB5dGhvblN0b3JlLk9wZXJhdG9ySW5mb0VudHJ5QhHiPw4SDG9wZXJhdG9ySW5mb1IMb
  3BlcmF0b3JJbmZvGoEBChFPcGVyYXRvckluZm9FbnRyeRIaCgNrZXkYASABKAlCCOI/BRIDa2V5UgNrZXkSTAoFdmFsdWUYAiABK
  AsyKi5lZHUudWNpLmljcy50ZXhlcmEud2ViLlB5dGhvbk9wZXJhdG9ySW5mb0IK4j8HEgV2YWx1ZVIFdmFsdWU6AjgBInYKFU9wZ
  XJhdG9yV29ya2VyTWFwcGluZxIvCgpvcGVyYXRvcklkGAEgASgJQg/iPwwSCm9wZXJhdG9ySWRSCm9wZXJhdG9ySWQSLAoJd29ya
  2VySWRzGAIgAygJQg7iPwsSCXdvcmtlcklkc1IJd29ya2VySWRzItABChRPcGVyYXRvclJ1bnRpbWVTdGF0cxJRCgVzdGF0ZRgBI
  AEoDjIvLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuV29ya2Zsb3dBZ2dyZWdhdGVkU3RhdGVCCuI/BxIFc3RhdGVSBXN0YXRlEjAKC
  2lucHV0X2NvdW50GAIgASgDQg/iPwwSCmlucHV0Q291bnRSCmlucHV0Q291bnQSMwoMb3V0cHV0X2NvdW50GAMgASgDQhDiPw0SC
  291dHB1dENvdW50UgtvdXRwdXRDb3VudCKKAwoNSm9iU3RhdHNTdG9yZRJvCg1vcGVyYXRvcl9pbmZvGAEgAygLMjcuZWR1LnVja
  S5pY3MudGV4ZXJhLndlYi5Kb2JTdGF0c1N0b3JlLk9wZXJhdG9ySW5mb0VudHJ5QhHiPw4SDG9wZXJhdG9ySW5mb1IMb3BlcmF0b
  3JJbmZvEoEBChdvcGVyYXRvcl93b3JrZXJfbWFwcGluZxgCIAMoCzItLmVkdS51Y2kuaWNzLnRleGVyYS53ZWIuT3BlcmF0b3JXb
  3JrZXJNYXBwaW5nQhriPxcSFW9wZXJhdG9yV29ya2VyTWFwcGluZ1IVb3BlcmF0b3JXb3JrZXJNYXBwaW5nGoMBChFPcGVyYXRvc
  kluZm9FbnRyeRIaCgNrZXkYASABKAlCCOI/BRIDa2V5UgNrZXkSTgoFdmFsdWUYAiABKAsyLC5lZHUudWNpLmljcy50ZXhlcmEud
  2ViLk9wZXJhdG9yUnVudGltZVN0YXRzQgriPwcSBXZhbHVlUgV2YWx1ZToCOAEiqwQKEEpvYk1ldGFkYXRhU3RvcmUSUQoFc3Rhd
  GUYASABKA4yLy5lZHUudWNpLmljcy50ZXhlcmEud2ViLldvcmtmbG93QWdncmVnYXRlZFN0YXRlQgriPwcSBXN0YXRlUgVzdGF0Z
  RIgCgVlcnJvchgCIAEoCUIK4j8HEgVlcnJvclIFZXJyb3ISGgoDZWlkGAMgASgDQgjiPwUSA2VpZFIDZWlkEjYKDWlzX3JlY292Z
  XJpbmcYBCABKAhCEeI/DhIMaXNSZWNvdmVyaW5nUgxpc1JlY292ZXJpbmcSMwoMaXNfcmVwbGF5aW5nGAUgASgIQhDiPw0SC2lzU
  mVwbGF5aW5nUgtpc1JlcGxheWluZxJDChJjdXJyZW50X3JlcGxheV9wb3MYBiABKAVCFeI/EhIQY3VycmVudFJlcGxheVBvc1IQY
  3VycmVudFJlcGxheVBvcxJIChNpbnRlcmFjdGlvbl9oaXN0b3J5GAcgAygFQhfiPxQSEmludGVyYWN0aW9uSGlzdG9yeVISaW50Z
  XJhY3Rpb25IaXN0b3J5EkAKEW9wZXJhdG9yX2luZm9fc3RyGAggASgJQhTiPxESD29wZXJhdG9ySW5mb1N0clIPb3BlcmF0b3JJb
  mZvU3RyEkgKE2NoZWNrcG9pbnRlZF9zdGF0ZXMYCSADKAVCF+I/FBISY2hlY2twb2ludGVkU3RhdGVzUhJjaGVja3BvaW50ZWRTd
  GF0ZXMqlAEKF1dvcmtmbG93QWdncmVnYXRlZFN0YXRlEhEKDVVOSU5JVElBTElaRUQQABIJCgVSRUFEWRABEgsKB1JVTk5JTkcQA
  hILCgdQQVVTSU5HEAMSCgoGUEFVU0VEEAQSDAoIUkVTVU1JTkcQBRINCglDT01QTEVURUQQBhILCgdBQk9SVEVEEAcSCwoHVU5LT
  k9XThAIQgniPwZIAFgAeABiBnByb3RvMw=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto.javaDescriptor,
      com.google.protobuf.timestamp.TimestampProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}