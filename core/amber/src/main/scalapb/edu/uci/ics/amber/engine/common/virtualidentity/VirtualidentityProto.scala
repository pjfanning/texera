// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.common.virtualidentity

object VirtualidentityProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.common.virtualidentity.WorkflowIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.ExecutionIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.ChannelIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.PhysicalOpIdentity,
      edu.uci.ics.amber.engine.common.virtualidentity.ChannelMarkerIdentity
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CjVlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvY29tbW9uL3ZpcnR1YWxpZGVudGl0eS5wcm90bxIfZWR1LnVjaS5pY3MuYW1iZ
  XIuZW5naW5lLmNvbW1vbhoVc2NhbGFwYi9zY2FsYXBiLnByb3RvIisKEFdvcmtmbG93SWRlbnRpdHkSFwoCaWQYASABKANCB+I/B
  BICaWRSAmlkIiwKEUV4ZWN1dGlvbklkZW50aXR5EhcKAmlkGAEgASgDQgfiPwQSAmlkUgJpZCI1ChRBY3RvclZpcnR1YWxJZGVud
  Gl0eRIdCgRuYW1lGAEgASgJQgniPwYSBG5hbWVSBG5hbWUimwIKD0NoYW5uZWxJZGVudGl0eRJvCgxmcm9tV29ya2VySWQYASABK
  AsyNS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkFjdG9yVmlydHVhbElkZW50aXR5QhTiPxESDGZyb21Xb3JrZXJJZ
  PABAVIMZnJvbVdvcmtlcklkEmkKCnRvV29ya2VySWQYAiABKAsyNS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkFjd
  G9yVmlydHVhbElkZW50aXR5QhLiPw8SCnRvV29ya2VySWTwAQFSCnRvV29ya2VySWQSLAoJaXNDb250cm9sGAMgASgIQg7iPwsSC
  WlzQ29udHJvbFIJaXNDb250cm9sIisKEE9wZXJhdG9ySWRlbnRpdHkSFwoCaWQYASABKAlCB+I/BBICaWRSAmlkIqwBChJQaHlza
  WNhbE9wSWRlbnRpdHkSaAoLbG9naWNhbE9wSWQYASABKAsyMS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLk9wZXJhd
  G9ySWRlbnRpdHlCE+I/EBILbG9naWNhbE9wSWTwAQFSC2xvZ2ljYWxPcElkEiwKCWxheWVyTmFtZRgCIAEoCUIO4j8LEglsYXllc
  k5hbWVSCWxheWVyTmFtZSIwChVDaGFubmVsTWFya2VySWRlbnRpdHkSFwoCaWQYASABKAlCB+I/BBICaWRSAmlkQgniPwZIAFgAe
  AFiBnByb3RvMw=="""
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