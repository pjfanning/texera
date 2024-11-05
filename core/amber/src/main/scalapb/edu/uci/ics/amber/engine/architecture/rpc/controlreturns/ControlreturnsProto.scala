// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.rpc.controlreturns

object ControlreturnsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.statistics.StatisticsProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.ControlReturnMessage,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.EmptyReturn,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.ControlError,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.ReturnInvocation,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.StringResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.IntResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.RetrieveWorkflowStateResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.FinalizeCheckpointResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.PropagateChannelMarkerResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.TakeGlobalCheckpointResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.TypedValue,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.EvaluatedValue,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.EvaluatePythonExpressionResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.StartWorkflowResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkerStateResponse,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.WorkerMetricsResponse
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Cj5lZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3JwYy9jb250cm9scmV0dXJucy5wcm90bxIpZWR1LnVja
  S5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMaPWVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY3R1cmUvd
  29ya2VyL3N0YXRpc3RpY3MucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90byKiDgoNQ29udHJvbFJldHVybhK0AQodcmV0cmlld
  mVXb3JrZmxvd1N0YXRlUmVzcG9uc2UYASABKAsySC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5SZ
  XRyaWV2ZVdvcmtmbG93U3RhdGVSZXNwb25zZUIi4j8fEh1yZXRyaWV2ZVdvcmtmbG93U3RhdGVSZXNwb25zZUgAUh1yZXRyaWV2Z
  VdvcmtmbG93U3RhdGVSZXNwb25zZRK4AQoecHJvcGFnYXRlQ2hhbm5lbE1hcmtlclJlc3BvbnNlGAIgASgLMkkuZWR1LnVjaS5pY
  3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuUHJvcGFnYXRlQ2hhbm5lbE1hcmtlclJlc3BvbnNlQiPiPyASHnByb3BhZ
  2F0ZUNoYW5uZWxNYXJrZXJSZXNwb25zZUgAUh5wcm9wYWdhdGVDaGFubmVsTWFya2VyUmVzcG9uc2USsAEKHHRha2VHbG9iYWxDa
  GVja3BvaW50UmVzcG9uc2UYAyABKAsyRy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5UYWtlR2xvY
  mFsQ2hlY2twb2ludFJlc3BvbnNlQiHiPx4SHHRha2VHbG9iYWxDaGVja3BvaW50UmVzcG9uc2VIAFIcdGFrZUdsb2JhbENoZWNrc
  G9pbnRSZXNwb25zZRLAAQogZXZhbHVhdGVQeXRob25FeHByZXNzaW9uUmVzcG9uc2UYBCABKAsySy5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5FdmFsdWF0ZVB5dGhvbkV4cHJlc3Npb25SZXNwb25zZUIl4j8iEiBldmFsdWF0ZVB5d
  GhvbkV4cHJlc3Npb25SZXNwb25zZUgAUiBldmFsdWF0ZVB5dGhvbkV4cHJlc3Npb25SZXNwb25zZRKUAQoVc3RhcnRXb3JrZmxvd
  1Jlc3BvbnNlGAUgASgLMkAuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuU3RhcnRXb3JrZmxvd1Jlc
  3BvbnNlQhriPxcSFXN0YXJ0V29ya2Zsb3dSZXNwb25zZUgAUhVzdGFydFdvcmtmbG93UmVzcG9uc2USjAEKE3dvcmtlclN0YXRlU
  mVzcG9uc2UYMiABKAsyPi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5Xb3JrZXJTdGF0ZVJlc3Bvb
  nNlQhjiPxUSE3dvcmtlclN0YXRlUmVzcG9uc2VIAFITd29ya2VyU3RhdGVSZXNwb25zZRKUAQoVd29ya2VyTWV0cmljc1Jlc3Bvb
  nNlGDMgASgLMkAuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuV29ya2VyTWV0cmljc1Jlc3BvbnNlQ
  hriPxcSFXdvcmtlck1ldHJpY3NSZXNwb25zZUgAUhV3b3JrZXJNZXRyaWNzUmVzcG9uc2USqAEKGmZpbmFsaXplQ2hlY2twb2lud
  FJlc3BvbnNlGDQgASgLMkUuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuRmluYWxpemVDaGVja3Bva
  W50UmVzcG9uc2VCH+I/HBIaZmluYWxpemVDaGVja3BvaW50UmVzcG9uc2VIAFIaZmluYWxpemVDaGVja3BvaW50UmVzcG9uc2USW
  woFZXJyb3IYZSABKAsyNy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5Db250cm9sRXJyb3JCCuI/B
  xIFZXJyb3JIAFIFZXJyb3ISbAoLZW1wdHlSZXR1cm4YZiABKAsyNi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0d
  XJlLnJwYy5FbXB0eVJldHVybkIQ4j8NEgtlbXB0eVJldHVybkgAUgtlbXB0eVJldHVybhJ4Cg5zdHJpbmdSZXNwb25zZRhnIAEoC
  zI5LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLlN0cmluZ1Jlc3BvbnNlQhPiPxASDnN0cmluZ1Jlc
  3BvbnNlSABSDnN0cmluZ1Jlc3BvbnNlEmwKC2ludFJlc3BvbnNlGGggASgLMjYuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY
  2hpdGVjdHVyZS5ycGMuSW50UmVzcG9uc2VCEOI/DRILaW50UmVzcG9uc2VIAFILaW50UmVzcG9uc2VCDgoMc2VhbGVkX3ZhbHVlI
  g0KC0VtcHR5UmV0dXJuIpICCgxDb250cm9sRXJyb3ISNQoMZXJyb3JNZXNzYWdlGAEgASgJQhHiPw4SDGVycm9yTWVzc2FnZVIMZ
  XJyb3JNZXNzYWdlEjUKDGVycm9yRGV0YWlscxgCIAEoCUIR4j8OEgxlcnJvckRldGFpbHNSDGVycm9yRGV0YWlscxIvCgpzdGFja
  1RyYWNlGAMgASgJQg/iPwwSCnN0YWNrVHJhY2VSCnN0YWNrVHJhY2USYwoIbGFuZ3VhZ2UYBCABKA4yOC5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5FcnJvckxhbmd1YWdlQg3iPwoSCGxhbmd1YWdlUghsYW5ndWFnZSLzAQoQUmV0d
  XJuSW52b2NhdGlvbhIsCgljb21tYW5kSWQYASABKANCDuI/CxIJY29tbWFuZElkUgljb21tYW5kSWQSbwoLcmV0dXJuVmFsdWUYA
  iABKAsyOC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5Db250cm9sUmV0dXJuQhPiPxASC3JldHVyb
  lZhbHVl8AEBUgtyZXR1cm5WYWx1ZTpA4j89CjtlZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLmFtYmVybWVzc2FnZS5Db
  250cm9sUGF5bG9hZCIyCg5TdHJpbmdSZXNwb25zZRIgCgV2YWx1ZRgBIAEoCUIK4j8HEgV2YWx1ZVIFdmFsdWUiLwoLSW50UmVzc
  G9uc2USIAoFdmFsdWUYASABKAVCCuI/BxIFdmFsdWVSBXZhbHVlIuYBCh1SZXRyaWV2ZVdvcmtmbG93U3RhdGVSZXNwb25zZRJ1C
  gVzdGF0ZRgBIAMoCzJTLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLlJldHJpZXZlV29ya2Zsb3dTd
  GF0ZVJlc3BvbnNlLlN0YXRlRW50cnlCCuI/BxIFc3RhdGVSBXN0YXRlGk4KClN0YXRlRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA
  2tleVIDa2V5EiAKBXZhbHVlGAIgASgJQgriPwcSBXZhbHVlUgV2YWx1ZToCOAEiOwoaRmluYWxpemVDaGVja3BvaW50UmVzcG9uc
  2USHQoEc2l6ZRgBIAEoA0IJ4j8GEgRzaXplUgRzaXplIq0CCh5Qcm9wYWdhdGVDaGFubmVsTWFya2VyUmVzcG9uc2USfgoHcmV0d
  XJucxgBIAMoCzJWLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLlByb3BhZ2F0ZUNoYW5uZWxNYXJrZ
  XJSZXNwb25zZS5SZXR1cm5zRW50cnlCDOI/CRIHcmV0dXJuc1IHcmV0dXJucxqKAQoMUmV0dXJuc0VudHJ5EhoKA2tleRgBIAEoC
  UII4j8FEgNrZXlSA2tleRJaCgV2YWx1ZRgCIAEoCzI4LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjL
  kNvbnRyb2xSZXR1cm5CCuI/BxIFdmFsdWVSBXZhbHVlOgI4ASJMChxUYWtlR2xvYmFsQ2hlY2twb2ludFJlc3BvbnNlEiwKCXRvd
  GFsU2l6ZRgBIAEoA0IO4j8LEgl0b3RhbFNpemVSCXRvdGFsU2l6ZSL1AQoKVHlwZWRWYWx1ZRIvCgpleHByZXNzaW9uGAEgASgJQ
  g/iPwwSCmV4cHJlc3Npb25SCmV4cHJlc3Npb24SKgoJdmFsdWVfcmVmGAIgASgJQg3iPwoSCHZhbHVlUmVmUgh2YWx1ZVJlZhIqC
  gl2YWx1ZV9zdHIYAyABKAlCDeI/ChIIdmFsdWVTdHJSCHZhbHVlU3RyEi0KCnZhbHVlX3R5cGUYBCABKAlCDuI/CxIJdmFsdWVUe
  XBlUgl2YWx1ZVR5cGUSLwoKZXhwYW5kYWJsZRgFIAEoCEIP4j8MEgpleHBhbmRhYmxlUgpleHBhbmRhYmxlItEBCg5FdmFsdWF0Z
  WRWYWx1ZRJXCgV2YWx1ZRgBIAEoCzI1LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLlR5cGVkVmFsd
  WVCCuI/BxIFdmFsdWVSBXZhbHVlEmYKCmF0dHJpYnV0ZXMYAiADKAsyNS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0Z
  WN0dXJlLnJwYy5UeXBlZFZhbHVlQg/iPwwSCmF0dHJpYnV0ZXNSCmF0dHJpYnV0ZXMiggEKIEV2YWx1YXRlUHl0aG9uRXhwcmVzc
  2lvblJlc3BvbnNlEl4KBnZhbHVlcxgBIAMoCzI5LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLkV2Y
  Wx1YXRlZFZhbHVlQgviPwgSBnZhbHVlc1IGdmFsdWVzIpgBChVTdGFydFdvcmtmbG93UmVzcG9uc2USfwoNd29ya2Zsb3dTdGF0Z
  RgBIAEoDjJCLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLldvcmtmbG93QWdncmVnYXRlZFN0YXRlQ
  hXiPxISDXdvcmtmbG93U3RhdGXwAQFSDXdvcmtmbG93U3RhdGUidQoTV29ya2VyU3RhdGVSZXNwb25zZRJeCgVzdGF0ZRgBIAEoD
  jI5LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLldvcmtlclN0YXRlQg3iPwoSBXN0YXRl8AEBU
  gVzdGF0ZSJ/ChVXb3JrZXJNZXRyaWNzUmVzcG9uc2USZgoHbWV0cmljcxgBIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZ
  S5hcmNoaXRlY3R1cmUud29ya2VyLldvcmtlck1ldHJpY3NCD+I/DBIHbWV0cmljc/ABAVIHbWV0cmljcyo/Cg1FcnJvckxhbmd1Y
  WdlEhcKBlBZVEhPThAAGgviPwgSBlBZVEhPThIVCgVTQ0FMQRABGgriPwcSBVNDQUxBKq8CChdXb3JrZmxvd0FnZ3JlZ2F0ZWRTd
  GF0ZRIlCg1VTklOSVRJQUxJWkVEEAAaEuI/DxINVU5JTklUSUFMSVpFRBIVCgVSRUFEWRABGgriPwcSBVJFQURZEhkKB1JVTk5JT
  kcQAhoM4j8JEgdSVU5OSU5HEhkKB1BBVVNJTkcQAxoM4j8JEgdQQVVTSU5HEhcKBlBBVVNFRBAEGgviPwgSBlBBVVNFRBIbCghSR
  VNVTUlORxAFGg3iPwoSCFJFU1VNSU5HEh0KCUNPTVBMRVRFRBAGGg7iPwsSCUNPTVBMRVRFRBIXCgZGQUlMRUQQBxoL4j8IEgZGQ
  UlMRUQSGQoHVU5LTk9XThAIGgziPwkSB1VOS05PV04SFwoGS0lMTEVEEAkaC+I/CBIGS0lMTEVEQgniPwZIAFgAeAFiBnByb3RvM
  w=="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.worker.statistics.StatisticsProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}