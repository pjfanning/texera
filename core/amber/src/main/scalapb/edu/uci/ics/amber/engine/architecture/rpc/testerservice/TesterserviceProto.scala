// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.rpc.testerservice

object TesterserviceProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlcommandsProto,
    edu.uci.ics.amber.engine.architecture.rpc.controlreturns.ControlreturnsProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """Cj1lZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3JwYy90ZXN0ZXJzZXJ2aWNlLnByb3RvEillZHUudWNpL
  mljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYxo/ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2FyY2hpdGVjdHVyZS9yc
  GMvY29udHJvbGNvbW1hbmRzLnByb3RvGj5lZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3JwYy9jb250cm9sc
  mV0dXJucy5wcm90bxoVc2NhbGFwYi9zY2FsYXBiLnByb3RvMo4KCglSUENUZXN0ZXISdQoIU2VuZFBpbmcSLy5lZHUudWNpLmljc
  y5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5QaW5nGjYuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZ
  S5ycGMuSW50UmVzcG9uc2UiABJ1CghTZW5kUG9uZxIvLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjL
  lBvbmcaNi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5JbnRSZXNwb25zZSIAEnwKClNlbmROZXN0Z
  WQSMS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5OZXN0ZWQaOS5lZHUudWNpLmljcy5hbWJlci5lb
  mdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5TdHJpbmdSZXNwb25zZSIAEngKCFNlbmRQYXNzEi8uZWR1LnVjaS5pY3MuYW1iZXIuZW5na
  W5lLmFyY2hpdGVjdHVyZS5ycGMuUGFzcxo5LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLlN0cmluZ
  1Jlc3BvbnNlIgASiAEKEFNlbmRFcnJvckNvbW1hbmQSNy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwY
  y5FcnJvckNvbW1hbmQaOS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5TdHJpbmdSZXNwb25zZSIAE
  oIBCg1TZW5kUmVjdXJzaW9uEjQuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuUmVjdXJzaW9uGjkuZ
  WR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuU3RyaW5nUmVzcG9uc2UiABJ+CgtTZW5kQ29sbGVjdBIyL
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLkNvbGxlY3QaOS5lZHUudWNpLmljcy5hbWJlci5lbmdpb
  mUuYXJjaGl0ZWN0dXJlLnJwYy5TdHJpbmdSZXNwb25zZSIAEokBChJTZW5kR2VuZXJhdGVOdW1iZXISOS5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5HZW5lcmF0ZU51bWJlcho2LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoa
  XRlY3R1cmUucnBjLkludFJlc3BvbnNlIgASggEKDVNlbmRNdWx0aUNhbGwSNC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJja
  Gl0ZWN0dXJlLnJwYy5NdWx0aUNhbGwaOS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLnJwYy5TdHJpbmdSZ
  XNwb25zZSIAEnoKCVNlbmRDaGFpbhIwLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUucnBjLkNoYWluGjkuZ
  WR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5ycGMuU3RyaW5nUmVzcG9uc2UiAEIJ4j8GSABYAHgBYgZwcm90b
  zM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.rpc.controlcommands.ControlcommandsProto.javaDescriptor,
      edu.uci.ics.amber.engine.architecture.rpc.controlreturns.ControlreturnsProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}