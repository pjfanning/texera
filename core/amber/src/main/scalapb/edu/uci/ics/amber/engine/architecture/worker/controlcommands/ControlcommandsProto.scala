// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

object ControlcommandsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto,
    com.google.protobuf.timestamp.TimestampProto,
    scalapb.options.ScalapbProto,
    com.google.protobuf.timestamp.TimestampProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.StartWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PauseWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ResumeWorkerV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.SchedulerTimeSlotEventV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.OpenOperatorV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.UpdateInputLinkingV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.AddPartitioningV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.WorkerExecutionCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryStatisticsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QueryCurrentInputTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LocalOperatorExceptionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.InitializeOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ModifyOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ReplayCurrentTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.EvaluateExpressionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.DebugCommandV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QuerySelfWorkloadMetricsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlCommandV2Message
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkJlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9sY29tbWFuZHMucHJvdG8SLGVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyGkdlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0Z
  WN0dXJlL3NlbmRzZW1hbnRpY3MvcGFydGl0aW9uaW5ncy5wcm90bxo1ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi92a
  XJ0dWFsaWRlbnRpdHkucHJvdG8aH2dvb2dsZS9wcm90b2J1Zi90aW1lc3RhbXAucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90b
  yIPCg1TdGFydFdvcmtlclYyIg8KDVBhdXNlV29ya2VyVjIiEAoOUmVzdW1lV29ya2VyVjIiXAoYU2NoZWR1bGVyVGltZVNsb3RFd
  mVudFYyEkAKEXRpbWVfc2xvdF9leHBpcmVkGAEgASgIQhTiPxESD3RpbWVTbG90RXhwaXJlZFIPdGltZVNsb3RFeHBpcmVkIhAKD
  k9wZW5PcGVyYXRvclYyIuIBChRVcGRhdGVJbnB1dExpbmtpbmdWMhJpCgppZGVudGlmaWVyGAEgASgLMjUuZWR1LnVjaS5pY3MuY
  W1iZXIuZW5naW5lLmNvbW1vbi5BY3RvclZpcnR1YWxJZGVudGl0eUIS4j8PEgppZGVudGlmaWVy8AEBUgppZGVudGlmaWVyEl8KC
  mlucHV0X2xpbmsYAiABKAsyLS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkxpbmtJZGVudGl0eUIR4j8OEglpbnB1d
  ExpbmvwAQFSCWlucHV0TGluayLeAQoRQWRkUGFydGl0aW9uaW5nVjISTAoDdGFnGAEgASgLMi0uZWR1LnVjaS5pY3MuYW1iZXIuZ
  W5naW5lLmNvbW1vbi5MaW5rSWRlbnRpdHlCC+I/CBIDdGFn8AEBUgN0YWcSewoMcGFydGl0aW9uaW5nGAIgASgLMkEuZWR1LnVja
  S5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS5zZW5kc2VtYW50aWNzLlBhcnRpdGlvbmluZ0IU4j8REgxwYXJ0aXRpb25pb
  mfwAQFSDHBhcnRpdGlvbmluZyIcChpXb3JrZXJFeGVjdXRpb25Db21wbGV0ZWRWMiITChFRdWVyeVN0YXRpc3RpY3NWMiIaChhRd
  WVyeUN1cnJlbnRJbnB1dFR1cGxlVjIiQgoYTG9jYWxPcGVyYXRvckV4Y2VwdGlvblYyEiYKB21lc3NhZ2UYASABKAlCDOI/CRIHb
  WVzc2FnZVIHbWVzc2FnZSLAAwoZSW5pdGlhbGl6ZU9wZXJhdG9yTG9naWNWMhIdCgRjb2RlGAEgASgJQgniPwYSBGNvZGVSBGNvZ
  GUSbQoPdXBzdHJlYW1MaW5rSWRzGAIgAygLMi0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5MaW5rSWRlbnRpdHlCF
  OI/ERIPdXBzdHJlYW1MaW5rSWRzUg91cHN0cmVhbUxpbmtJZHMSKgoJaXNfc291cmNlGAMgASgIQg3iPwoSCGlzU291cmNlUghpc
  1NvdXJjZRKRAQoNb3V0cHV0X3NjaGVtYRgEIAMoCzJZLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya
  2VyLkluaXRpYWxpemVPcGVyYXRvckxvZ2ljVjIuT3V0cHV0U2NoZW1hRW50cnlCEeI/DhIMb3V0cHV0U2NoZW1hUgxvdXRwdXRTY
  2hlbWEaVQoRT3V0cHV0U2NoZW1hRW50cnkSGgoDa2V5GAEgASgJQgjiPwUSA2tleVIDa2V5EiAKBXZhbHVlGAIgASgJQgriPwcSB
  XZhbHVlUgV2YWx1ZToCOAEiYgoVTW9kaWZ5T3BlcmF0b3JMb2dpY1YyEh0KBGNvZGUYASABKAlCCeI/BhIEY29kZVIEY29kZRIqC
  glpc19zb3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlIhYKFFJlcGxheUN1cnJlbnRUdXBsZVYyItsBChZQeXRob
  25Db25zb2xlTWVzc2FnZVYyEksKCXRpbWVzdGFtcBgBIAEoCzIaLmdvb2dsZS5wcm90b2J1Zi5UaW1lc3RhbXBCEeI/DhIJdGltZ
  XN0YW1w8AEBUgl0aW1lc3RhbXASJwoIbXNnX3R5cGUYAiABKAlCDOI/CRIHbXNnVHlwZVIHbXNnVHlwZRIjCgZzb3VyY2UYAyABK
  AlCC+I/CBIGc291cmNlUgZzb3VyY2USJgoHbWVzc2FnZRgEIAEoCUIM4j8JEgdtZXNzYWdlUgdtZXNzYWdlIkcKFEV2YWx1YXRlR
  XhwcmVzc2lvblYyEi8KCmV4cHJlc3Npb24YASABKAlCD+I/DBIKZXhwcmVzc2lvblIKZXhwcmVzc2lvbiIcChpRdWVyeVNlbGZXb
  3JrbG9hZE1ldHJpY3NWMiJpCg9MaW5rQ29tcGxldGVkVjISVgoHbGlua19pZBgBIAEoCzItLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ
  2luZS5jb21tb24uTGlua0lkZW50aXR5Qg7iPwsSBmxpbmtJZPABAVIGbGlua0lkIsgUChBDb250cm9sQ29tbWFuZFYyEnIKDHN0Y
  XJ0X3dvcmtlchgBIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlN0YXJ0V29ya2VyV
  jJCEOI/DRILc3RhcnRXb3JrZXJIAFILc3RhcnRXb3JrZXIScgoMcGF1c2Vfd29ya2VyGAIgASgLMjsuZWR1LnVjaS5pY3MuYW1iZ
  XIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUGF1c2VXb3JrZXJWMkIQ4j8NEgtwYXVzZVdvcmtlckgAUgtwYXVzZVdvcmtlc
  hJ2Cg1yZXN1bWVfd29ya2VyGAMgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUmVzd
  W1lV29ya2VyVjJCEeI/DhIMcmVzdW1lV29ya2VySABSDHJlc3VtZVdvcmtlchKCAQoQYWRkX3BhcnRpdGlvbmluZxgEIAEoCzI/L
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkFkZFBhcnRpdGlvbmluZ1YyQhTiPxESD2FkZFBhc
  nRpdGlvbmluZ0gAUg9hZGRQYXJ0aXRpb25pbmcSjwEKFHVwZGF0ZV9pbnB1dF9saW5raW5nGAUgASgLMkIuZWR1LnVjaS5pY3MuY
  W1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuVXBkYXRlSW5wdXRMaW5raW5nVjJCF+I/FBISdXBkYXRlSW5wdXRMaW5ra
  W5nSABSEnVwZGF0ZUlucHV0TGlua2luZxKCAQoQcXVlcnlfc3RhdGlzdGljcxgGIAEoCzI/LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ
  2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlF1ZXJ5U3RhdGlzdGljc1YyQhTiPxESD3F1ZXJ5U3RhdGlzdGljc0gAUg9xdWVyeVN0Y
  XRpc3RpY3MSoAEKGXF1ZXJ5X2N1cnJlbnRfaW5wdXRfdHVwbGUYByABKAsyRi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJja
  Gl0ZWN0dXJlLndvcmtlci5RdWVyeUN1cnJlbnRJbnB1dFR1cGxlVjJCG+I/GBIWcXVlcnlDdXJyZW50SW5wdXRUdXBsZUgAUhZxd
  WVyeUN1cnJlbnRJbnB1dFR1cGxlEp8BChhsb2NhbF9vcGVyYXRvcl9leGNlcHRpb24YCCABKAsyRi5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Mb2NhbE9wZXJhdG9yRXhjZXB0aW9uVjJCG+I/GBIWbG9jYWxPcGVyYXRvckV4Y
  2VwdGlvbkgAUhZsb2NhbE9wZXJhdG9yRXhjZXB0aW9uEnYKDW9wZW5fb3BlcmF0b3IYCSABKAsyPC5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5PcGVuT3BlcmF0b3JWMkIR4j8OEgxvcGVuT3BlcmF0b3JIAFIMb3Blbk9wZXJhd
  G9yEnoKDmxpbmtfY29tcGxldGVkGAogASgLMj0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuT
  Glua0NvbXBsZXRlZFYyQhLiPw8SDWxpbmtDb21wbGV0ZWRIAFINbGlua0NvbXBsZXRlZBKgAQoZc2NoZWR1bGVyX3RpbWVfc2xvd
  F9ldmVudBgLIAEoCzJGLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlNjaGVkdWxlclRpbWVTb
  G90RXZlbnRWMkIb4j8YEhZzY2hlZHVsZXJUaW1lU2xvdEV2ZW50SABSFnNjaGVkdWxlclRpbWVTbG90RXZlbnQSowEKGWluaXRpY
  WxpemVfb3BlcmF0b3JfbG9naWMYFSABKAsyRy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Jb
  ml0aWFsaXplT3BlcmF0b3JMb2dpY1YyQhziPxkSF2luaXRpYWxpemVPcGVyYXRvckxvZ2ljSABSF2luaXRpYWxpemVPcGVyYXRvc
  kxvZ2ljEpMBChVtb2RpZnlfb3BlcmF0b3JfbG9naWMYFiABKAsyQy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0d
  XJlLndvcmtlci5Nb2RpZnlPcGVyYXRvckxvZ2ljVjJCGOI/FRITbW9kaWZ5T3BlcmF0b3JMb2dpY0gAUhNtb2RpZnlPcGVyYXRvc
  kxvZ2ljEpcBChZweXRob25fY29uc29sZV9tZXNzYWdlGBcgASgLMkQuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuUHl0aG9uQ29uc29sZU1lc3NhZ2VWMkIZ4j8WEhRweXRob25Db25zb2xlTWVzc2FnZUgAUhRweXRob25Db25zb
  2xlTWVzc2FnZRKPAQoUcmVwbGF5X2N1cnJlbnRfdHVwbGUYGCABKAsyQi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0Z
  WN0dXJlLndvcmtlci5SZXBsYXlDdXJyZW50VHVwbGVWMkIX4j8UEhJyZXBsYXlDdXJyZW50VHVwbGVIAFIScmVwbGF5Q3VycmVud
  FR1cGxlEo4BChNldmFsdWF0ZV9leHByZXNzaW9uGBkgASgLMkIuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZ
  S53b3JrZXIuRXZhbHVhdGVFeHByZXNzaW9uVjJCF+I/FBISZXZhbHVhdGVFeHByZXNzaW9uSABSEmV2YWx1YXRlRXhwcmVzc2lvb
  hKoAQobcXVlcnlfc2VsZl93b3JrbG9hZF9tZXRyaWNzGCkgASgLMkguZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuUXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzVjJCHeI/GhIYcXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzSABSGHF1Z
  XJ5U2VsZldvcmtsb2FkTWV0cmljcxKnAQoad29ya2VyX2V4ZWN1dGlvbl9jb21wbGV0ZWQYZSABKAsySC5lZHUudWNpLmljcy5hb
  WJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJFeGVjdXRpb25Db21wbGV0ZWRWMkId4j8aEhh3b3JrZXJFeGVjd
  XRpb25Db21wbGV0ZWRIAFIYd29ya2VyRXhlY3V0aW9uQ29tcGxldGVkQg4KDHNlYWxlZF92YWx1ZUIJ4j8GSABYAHgBYgZwcm90b
  zM="""
      ).mkString)
  lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {
    val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)
    _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))
  }
  lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {
    val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)
    com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, _root_.scala.Array(
      edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto.javaDescriptor,
      edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto.javaDescriptor,
      com.google.protobuf.timestamp.TimestampProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor,
      com.google.protobuf.timestamp.TimestampProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}