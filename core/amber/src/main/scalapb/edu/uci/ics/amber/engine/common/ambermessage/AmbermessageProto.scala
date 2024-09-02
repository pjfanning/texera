// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.ambermessage

object AmbermessageProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlcommandsProto,
    edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.common.ambermessage.ControlInvocationV2,
      edu.uci.ics.amber.engine.common.ambermessage.ReturnInvocationV2,
      edu.uci.ics.amber.engine.common.ambermessage.ControlPayloadV2Message,
      edu.uci.ics.amber.engine.common.ambermessage.PythonDataHeader,
      edu.uci.ics.amber.engine.common.ambermessage.PythonControlMessage
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CjJlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvY29tbW9uL2FtYmVybWVzc2FnZS5wcm90bxIfZWR1LnVjaS5pY3MuYW1iZXIuZ
  W5naW5lLmNvbW1vbhpCZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2FyY2hpdGVjdHVyZS93b3JrZXIvY29udHJvbGNvbW1hbmRzL
  nByb3RvGkFlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9scmV0dXJucy5wcm90bxo1Z
  WR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi92aXJ0dWFsaWRlbnRpdHkucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90b
  yKvAQoTQ29udHJvbEludm9jYXRpb25WMhItCgpjb21tYW5kX2lkGAEgASgDQg7iPwsSCWNvbW1hbmRJZFIJY29tbWFuZElkEmkKB
  2NvbW1hbmQYAiABKAsyPi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Db250cm9sQ29tbWFuZ
  FYyQg/iPwwSB2NvbW1hbmTwAQFSB2NvbW1hbmQi2QEKElJldHVybkludm9jYXRpb25WMhJGChNvcmlnaW5hbF9jb21tYW5kX2lkG
  AEgASgDQhbiPxMSEW9yaWdpbmFsQ29tbWFuZElkUhFvcmlnaW5hbENvbW1hbmRJZBJ7Cg5jb250cm9sX3JldHVybhgCIAEoCzI9L
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkNvbnRyb2xSZXR1cm5WMkIV4j8SEg1jb250cm9sU
  mV0dXJu8AEBUg1jb250cm9sUmV0dXJuIpwCChBDb250cm9sUGF5bG9hZFYyEn0KEmNvbnRyb2xfaW52b2NhdGlvbhgBIAEoCzI0L
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uQ29udHJvbEludm9jYXRpb25WMkIW4j8TEhFjb250cm9sSW52b2NhdGlvb
  kgAUhFjb250cm9sSW52b2NhdGlvbhJ5ChFyZXR1cm5faW52b2NhdGlvbhgCIAEoCzIzLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZ
  S5jb21tb24uUmV0dXJuSW52b2NhdGlvblYyQhXiPxISEHJldHVybkludm9jYXRpb25IAFIQcmV0dXJuSW52b2NhdGlvbkIOCgxzZ
  WFsZWRfdmFsdWUinQEKEFB5dGhvbkRhdGFIZWFkZXISVAoDdGFnGAEgASgLMjUuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvb
  W1vbi5BY3RvclZpcnR1YWxJZGVudGl0eUIL4j8IEgN0YWfwAQFSA3RhZxIzCgxwYXlsb2FkX3R5cGUYAiABKAlCEOI/DRILcGF5b
  G9hZFR5cGVSC3BheWxvYWRUeXBlIsoBChRQeXRob25Db250cm9sTWVzc2FnZRJUCgN0YWcYASABKAsyNS5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuY29tbW9uLkFjdG9yVmlydHVhbElkZW50aXR5QgviPwgSA3RhZ/ABAVIDdGFnElwKB3BheWxvYWQYAiABKAsyM
  S5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkNvbnRyb2xQYXlsb2FkVjJCD+I/DBIHcGF5bG9hZPABAVIHcGF5bG9hZ
  EIJ4j8GSABYAHgBYgZwcm90bzM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlcommandsProto.javaDescriptor,
      edu.uci.ics.amber.engine.architecture.worker.controlreturns.ControlreturnsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}