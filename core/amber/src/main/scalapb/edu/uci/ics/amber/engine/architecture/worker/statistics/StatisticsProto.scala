// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.statistics

object StatisticsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.statistics.WorkerStatistics
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Cj1lZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9zdGF0aXN0aWNzLnByb3RvEixlZHUudWNpL
  mljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlchoVc2NhbGFwYi9zY2FsYXBiLnByb3RvIowCChBXb3JrZXJTdGF0a
  XN0aWNzEnEKDHdvcmtlcl9zdGF0ZRgBIAEoDjI5LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyL
  ldvcmtlclN0YXRlQhPiPxASC3dvcmtlclN0YXRl8AEBUgt3b3JrZXJTdGF0ZRJAChFpbnB1dF90dXBsZV9jb3VudBgCIAEoA0IU4
  j8REg9pbnB1dFR1cGxlQ291bnRSD2lucHV0VHVwbGVDb3VudBJDChJvdXRwdXRfdHVwbGVfY291bnQYAyABKANCFeI/EhIQb3V0c
  HV0VHVwbGVDb3VudFIQb3V0cHV0VHVwbGVDb3VudCpTCgtXb3JrZXJTdGF0ZRIRCg1VTklOSVRJQUxJWkVEEAASCQoFUkVBRFkQA
  RILCgdSVU5OSU5HEAISCgoGUEFVU0VEEAMSDQoJQ09NUExFVEVEEARCCeI/BkgAWAB4AWIGcHJvdG8z"""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}