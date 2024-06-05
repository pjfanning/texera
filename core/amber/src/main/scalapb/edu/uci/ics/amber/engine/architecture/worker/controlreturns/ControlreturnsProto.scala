// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlreturns

object ControlreturnsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.statistics.StatisticsProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.CurrentInputTupleInfo,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlException,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.TypedValue,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.EvaluatedValue,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlReturnV2
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkFlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9scmV0dXJucy5wcm90bxIsZWR1L
  nVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIaPWVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9hcmNoaXRlY
  3R1cmUvd29ya2VyL3N0YXRpc3RpY3MucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90byIXChVDdXJyZW50SW5wdXRUdXBsZUluZ
  m8iLgoQQ29udHJvbEV4Y2VwdGlvbhIaCgNtc2cYASABKAlCCOI/BRIDbXNnUgNtc2ci9QEKClR5cGVkVmFsdWUSLwoKZXhwcmVzc
  2lvbhgBIAEoCUIP4j8MEgpleHByZXNzaW9uUgpleHByZXNzaW9uEioKCXZhbHVlX3JlZhgCIAEoCUIN4j8KEgh2YWx1ZVJlZlIId
  mFsdWVSZWYSKgoJdmFsdWVfc3RyGAMgASgJQg3iPwoSCHZhbHVlU3RyUgh2YWx1ZVN0chItCgp2YWx1ZV90eXBlGAQgASgJQg7iP
  wsSCXZhbHVlVHlwZVIJdmFsdWVUeXBlEi8KCmV4cGFuZGFibGUYBSABKAhCD+I/DBIKZXhwYW5kYWJsZVIKZXhwYW5kYWJsZSLXA
  QoORXZhbHVhdGVkVmFsdWUSWgoFdmFsdWUYASABKAsyOC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvc
  mtlci5UeXBlZFZhbHVlQgriPwcSBXZhbHVlUgV2YWx1ZRJpCgphdHRyaWJ1dGVzGAIgAygLMjguZWR1LnVjaS5pY3MuYW1iZXIuZ
  W5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuVHlwZWRWYWx1ZUIP4j8MEgphdHRyaWJ1dGVzUgphdHRyaWJ1dGVzIqgFCg9Db250c
  m9sUmV0dXJuVjIShAEKEWNvbnRyb2xfZXhjZXB0aW9uGAEgASgLMj4uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuQ29udHJvbEV4Y2VwdGlvbkIV4j8SEhBjb250cm9sRXhjZXB0aW9uSABSEGNvbnRyb2xFeGNlcHRpb24ScAoMd
  29ya2VyX3N0YXRlGAIgASgOMjkuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuV29ya2VyU3Rhd
  GVCEOI/DRILd29ya2VyU3RhdGVIAFILd29ya2VyU3RhdGUSeAoOd29ya2VyX21ldHJpY3MYAyABKAsyOy5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJNZXRyaWNzQhLiPw8SDXdvcmtlck1ldHJpY3NIAFINd29ya2VyT
  WV0cmljcxKaAQoYY3VycmVudF9pbnB1dF90dXBsZV9pbmZvGAQgASgLMkMuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpd
  GVjdHVyZS53b3JrZXIuQ3VycmVudElucHV0VHVwbGVJbmZvQhriPxcSFWN1cnJlbnRJbnB1dFR1cGxlSW5mb0gAUhVjdXJyZW50S
  W5wdXRUdXBsZUluZm8SfAoPZXZhbHVhdGVkX3ZhbHVlGAUgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuRXZhbHVhdGVkVmFsdWVCE+I/EBIOZXZhbHVhdGVkVmFsdWVIAFIOZXZhbHVhdGVkVmFsdWVCBwoFdmFsdWVCC
  eI/BkgAWAB4AWIGcHJvdG8z"""
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