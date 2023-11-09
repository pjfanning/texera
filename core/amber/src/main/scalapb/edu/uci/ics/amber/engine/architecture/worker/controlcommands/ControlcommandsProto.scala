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
    scalapb.options.ScalapbProto
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
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkOrdinal,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.InitializeOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ModifyOperatorLogicV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ReplayCurrentTupleV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ConsoleMessage,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.PythonConsoleMessageV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.EvaluateExpressionV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.WorkerDebugCommandV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.QuerySelfWorkloadMetricsV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.LinkCompletedV2,
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.BackpressureV2,
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
  WVyeUN1cnJlbnRJbnB1dFR1cGxlVjIimgEKC0xpbmtPcmRpbmFsElYKB2xpbmtfaWQYASABKAsyLS5lZHUudWNpLmljcy5hbWJlc
  i5lbmdpbmUuY29tbW9uLkxpbmtJZGVudGl0eUIO4j8LEgZsaW5rSWTwAQFSBmxpbmtJZBIzCgxwb3J0X29yZGluYWwYAiABKANCE
  OI/DRILcG9ydE9yZGluYWxSC3BvcnRPcmRpbmFsIugEChlJbml0aWFsaXplT3BlcmF0b3JMb2dpY1YyEh0KBGNvZGUYASABKAlCC
  eI/BhIEY29kZVIEY29kZRIqCglpc19zb3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlEocBChVpbnB1dF9vcmRpb
  mFsX21hcHBpbmcYAyADKAsyOS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5MaW5rT3JkaW5hb
  EIY4j8VEhNpbnB1dE9yZGluYWxNYXBwaW5nUhNpbnB1dE9yZGluYWxNYXBwaW5nEooBChZvdXRwdXRfb3JkaW5hbF9tYXBwaW5nG
  AQgAygLMjkuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTGlua09yZGluYWxCGeI/FhIUb3V0c
  HV0T3JkaW5hbE1hcHBpbmdSFG91dHB1dE9yZGluYWxNYXBwaW5nEpEBCg1vdXRwdXRfc2NoZW1hGAUgAygLMlkuZWR1LnVjaS5pY
  3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuSW5pdGlhbGl6ZU9wZXJhdG9yTG9naWNWMi5PdXRwdXRTY2hlbWFFb
  nRyeUIR4j8OEgxvdXRwdXRTY2hlbWFSDG91dHB1dFNjaGVtYRpVChFPdXRwdXRTY2hlbWFFbnRyeRIaCgNrZXkYASABKAlCCOI/B
  RIDa2V5UgNrZXkSIAoFdmFsdWUYAiABKAlCCuI/BxIFdmFsdWVSBXZhbHVlOgI4ASJiChVNb2RpZnlPcGVyYXRvckxvZ2ljVjISH
  QoEY29kZRgBIAEoCUIJ4j8GEgRjb2RlUgRjb2RlEioKCWlzX3NvdXJjZRgCIAEoCEIN4j8KEghpc1NvdXJjZVIIaXNTb3VyY2UiF
  goUUmVwbGF5Q3VycmVudFR1cGxlVjIi4wIKDkNvbnNvbGVNZXNzYWdlEioKCXdvcmtlcl9pZBgBIAEoCUIN4j8KEgh3b3JrZXJJZ
  FIId29ya2VySWQSSwoJdGltZXN0YW1wGAIgASgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcEIR4j8OEgl0aW1lc3RhbXDwA
  QFSCXRpbWVzdGFtcBJpCghtc2dfdHlwZRgDIAEoDjJALmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya
  2VyLkNvbnNvbGVNZXNzYWdlVHlwZUIM4j8JEgdtc2dUeXBlUgdtc2dUeXBlEiMKBnNvdXJjZRgEIAEoCUIL4j8IEgZzb3VyY2VSB
  nNvdXJjZRIgCgV0aXRsZRgFIAEoCUIK4j8HEgV0aXRsZVIFdGl0bGUSJgoHbWVzc2FnZRgGIAEoCUIM4j8JEgdtZXNzYWdlUgdtZ
  XNzYWdlIoEBChZQeXRob25Db25zb2xlTWVzc2FnZVYyEmcKB21lc3NhZ2UYASABKAsyPC5lZHUudWNpLmljcy5hbWJlci5lbmdpb
  mUuYXJjaGl0ZWN0dXJlLndvcmtlci5Db25zb2xlTWVzc2FnZUIP4j8MEgdtZXNzYWdl8AEBUgdtZXNzYWdlIkcKFEV2YWx1YXRlR
  XhwcmVzc2lvblYyEi8KCmV4cHJlc3Npb24YASABKAlCD+I/DBIKZXhwcmVzc2lvblIKZXhwcmVzc2lvbiIyChRXb3JrZXJEZWJ1Z
  0NvbW1hbmRWMhIaCgNjbWQYASABKAlCCOI/BRIDY21kUgNjbWQiHAoaUXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzVjIiaQoPTGlua
  0NvbXBsZXRlZFYyElYKB2xpbmtfaWQYASABKAsyLS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLkxpbmtJZGVudGl0e
  UIO4j8LEgZsaW5rSWTwAQFSBmxpbmtJZCJaCg5CYWNrcHJlc3N1cmVWMhJIChNlbmFibGVfYmFja3ByZXNzdXJlGAEgASgIQhfiP
  xQSEmVuYWJsZUJhY2twcmVzc3VyZVISZW5hYmxlQmFja3ByZXNzdXJlIq8VChBDb250cm9sQ29tbWFuZFYyEnIKDHN0YXJ0X3dvc
  mtlchgBIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlN0YXJ0V29ya2VyVjJCEOI/D
  RILc3RhcnRXb3JrZXJIAFILc3RhcnRXb3JrZXIScgoMcGF1c2Vfd29ya2VyGAIgASgLMjsuZWR1LnVjaS5pY3MuYW1iZXIuZW5na
  W5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUGF1c2VXb3JrZXJWMkIQ4j8NEgtwYXVzZVdvcmtlckgAUgtwYXVzZVdvcmtlchJ2Cg1yZ
  XN1bWVfd29ya2VyGAMgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUmVzdW1lV29ya
  2VyVjJCEeI/DhIMcmVzdW1lV29ya2VySABSDHJlc3VtZVdvcmtlchKCAQoQYWRkX3BhcnRpdGlvbmluZxgEIAEoCzI/LmVkdS51Y
  2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkFkZFBhcnRpdGlvbmluZ1YyQhTiPxESD2FkZFBhcnRpdGlvb
  mluZ0gAUg9hZGRQYXJ0aXRpb25pbmcSjwEKFHVwZGF0ZV9pbnB1dF9saW5raW5nGAUgASgLMkIuZWR1LnVjaS5pY3MuYW1iZXIuZ
  W5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuVXBkYXRlSW5wdXRMaW5raW5nVjJCF+I/FBISdXBkYXRlSW5wdXRMaW5raW5nSABSE
  nVwZGF0ZUlucHV0TGlua2luZxKCAQoQcXVlcnlfc3RhdGlzdGljcxgGIAEoCzI/LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hc
  mNoaXRlY3R1cmUud29ya2VyLlF1ZXJ5U3RhdGlzdGljc1YyQhTiPxESD3F1ZXJ5U3RhdGlzdGljc0gAUg9xdWVyeVN0YXRpc3RpY
  3MSoAEKGXF1ZXJ5X2N1cnJlbnRfaW5wdXRfdHVwbGUYByABKAsyRi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0d
  XJlLndvcmtlci5RdWVyeUN1cnJlbnRJbnB1dFR1cGxlVjJCG+I/GBIWcXVlcnlDdXJyZW50SW5wdXRUdXBsZUgAUhZxdWVyeUN1c
  nJlbnRJbnB1dFR1cGxlEnYKDW9wZW5fb3BlcmF0b3IYCSABKAsyPC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0d
  XJlLndvcmtlci5PcGVuT3BlcmF0b3JWMkIR4j8OEgxvcGVuT3BlcmF0b3JIAFIMb3Blbk9wZXJhdG9yEnoKDmxpbmtfY29tcGxld
  GVkGAogASgLMj0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTGlua0NvbXBsZXRlZFYyQhLiP
  w8SDWxpbmtDb21wbGV0ZWRIAFINbGlua0NvbXBsZXRlZBKgAQoZc2NoZWR1bGVyX3RpbWVfc2xvdF9ldmVudBgLIAEoCzJGLmVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlNjaGVkdWxlclRpbWVTbG90RXZlbnRWMkIb4j8YEhZzY
  2hlZHVsZXJUaW1lU2xvdEV2ZW50SABSFnNjaGVkdWxlclRpbWVTbG90RXZlbnQSowEKGWluaXRpYWxpemVfb3BlcmF0b3JfbG9na
  WMYFSABKAsyRy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Jbml0aWFsaXplT3BlcmF0b3JMb
  2dpY1YyQhziPxkSF2luaXRpYWxpemVPcGVyYXRvckxvZ2ljSABSF2luaXRpYWxpemVPcGVyYXRvckxvZ2ljEpMBChVtb2RpZnlfb
  3BlcmF0b3JfbG9naWMYFiABKAsyQy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Nb2RpZnlPc
  GVyYXRvckxvZ2ljVjJCGOI/FRITbW9kaWZ5T3BlcmF0b3JMb2dpY0gAUhNtb2RpZnlPcGVyYXRvckxvZ2ljEpcBChZweXRob25fY
  29uc29sZV9tZXNzYWdlGBcgASgLMkQuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUHl0aG9uQ
  29uc29sZU1lc3NhZ2VWMkIZ4j8WEhRweXRob25Db25zb2xlTWVzc2FnZUgAUhRweXRob25Db25zb2xlTWVzc2FnZRKPAQoUcmVwb
  GF5X2N1cnJlbnRfdHVwbGUYGCABKAsyQi5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5SZXBsY
  XlDdXJyZW50VHVwbGVWMkIX4j8UEhJyZXBsYXlDdXJyZW50VHVwbGVIAFIScmVwbGF5Q3VycmVudFR1cGxlEo4BChNldmFsdWF0Z
  V9leHByZXNzaW9uGBkgASgLMkIuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuRXZhbHVhdGVFe
  HByZXNzaW9uVjJCF+I/FBISZXZhbHVhdGVFeHByZXNzaW9uSABSEmV2YWx1YXRlRXhwcmVzc2lvbhKoAQobcXVlcnlfc2VsZl93b
  3JrbG9hZF9tZXRyaWNzGCkgASgLMkguZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUXVlcnlTZ
  WxmV29ya2xvYWRNZXRyaWNzVjJCHeI/GhIYcXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzSABSGHF1ZXJ5U2VsZldvcmtsb2FkTWV0c
  mljcxJ1CgxiYWNrcHJlc3N1cmUYMyABKAsyPC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5CY
  WNrcHJlc3N1cmVWMkIR4j8OEgxiYWNrcHJlc3N1cmVIAFIMYmFja3ByZXNzdXJlEo8BChR3b3JrZXJfZGVidWdfY29tbWFuZBhRI
  AEoCzJCLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLldvcmtlckRlYnVnQ29tbWFuZFYyQhfiP
  xQSEndvcmtlckRlYnVnQ29tbWFuZEgAUhJ3b3JrZXJEZWJ1Z0NvbW1hbmQSpwEKGndvcmtlcl9leGVjdXRpb25fY29tcGxldGVkG
  GUgASgLMkguZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuV29ya2VyRXhlY3V0aW9uQ29tcGxld
  GVkVjJCHeI/GhIYd29ya2VyRXhlY3V0aW9uQ29tcGxldGVkSABSGHdvcmtlckV4ZWN1dGlvbkNvbXBsZXRlZEIOCgxzZWFsZWRfd
  mFsdWUqRQoSQ29uc29sZU1lc3NhZ2VUeXBlEgkKBVBSSU5UEAASCQoFRVJST1IQARILCgdDT01NQU5EEAISDAoIREVCVUdHRVIQA
  0IJ4j8GSABYAHgBYgZwcm90bzM="""
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
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}