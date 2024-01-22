// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.amber.engine.architecture.worker.controlcommands

object ControlcommandsProto extends _root_.scalapb.GeneratedFileObject {
  lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(
    edu.uci.ics.amber.engine.architecture.sendsemantics.partitionings.PartitioningsProto,
    edu.uci.ics.amber.engine.common.virtualidentity.VirtualidentityProto,
    edu.uci.ics.amber.engine.common.workflow.WorkflowProto,
    com.google.protobuf.timestamp.TimestampProto,
    scalapb.options.ScalapbProto
  )
  lazy val messagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] =
    Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]](
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.NoOpV2,
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
      edu.uci.ics.amber.engine.architecture.worker.controlcommands.ControlCommandV2Message
    )
  private lazy val ProtoBytes: _root_.scala.Array[Byte] =
      scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(
  """CkJlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0ZWN0dXJlL3dvcmtlci9jb250cm9sY29tbWFuZHMucHJvdG8SLGVkd
  S51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyGkdlZHUvdWNpL2ljcy9hbWJlci9lbmdpbmUvYXJjaGl0Z
  WN0dXJlL3NlbmRzZW1hbnRpY3MvcGFydGl0aW9uaW5ncy5wcm90bxo1ZWR1L3VjaS9pY3MvYW1iZXIvZW5naW5lL2NvbW1vbi92a
  XJ0dWFsaWRlbnRpdHkucHJvdG8aLmVkdS91Y2kvaWNzL2FtYmVyL2VuZ2luZS9jb21tb24vd29ya2Zsb3cucHJvdG8aH2dvb2dsZ
  S9wcm90b2J1Zi90aW1lc3RhbXAucHJvdG8aFXNjYWxhcGIvc2NhbGFwYi5wcm90byIICgZOb09wVjIiDwoNU3RhcnRXb3JrZXJWM
  iIPCg1QYXVzZVdvcmtlclYyIhAKDlJlc3VtZVdvcmtlclYyIlwKGFNjaGVkdWxlclRpbWVTbG90RXZlbnRWMhJAChF0aW1lX3Nsb
  3RfZXhwaXJlZBgBIAEoCEIU4j8REg90aW1lU2xvdEV4cGlyZWRSD3RpbWVTbG90RXhwaXJlZCIQCg5PcGVuT3BlcmF0b3JWMiLiA
  QoUVXBkYXRlSW5wdXRMaW5raW5nVjISaQoKaWRlbnRpZmllchgBIAEoCzI1LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb
  24uQWN0b3JWaXJ0dWFsSWRlbnRpdHlCEuI/DxIKaWRlbnRpZmllcvABAVIKaWRlbnRpZmllchJfCgppbnB1dF9saW5rGAIgASgLM
  i0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5QaHlzaWNhbExpbmtCEeI/DhIJaW5wdXRMaW5r8AEBUglpbnB1dExpb
  msi3gEKEUFkZFBhcnRpdGlvbmluZ1YyEkwKA3RhZxgBIAEoCzItLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5jb21tb24uUGh5c
  2ljYWxMaW5rQgviPwgSA3RhZ/ABAVIDdGFnEnsKDHBhcnRpdGlvbmluZxgCIAEoCzJBLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZ
  S5hcmNoaXRlY3R1cmUuc2VuZHNlbWFudGljcy5QYXJ0aXRpb25pbmdCFOI/ERIMcGFydGl0aW9uaW5n8AEBUgxwYXJ0aXRpb25pb
  mciHAoaV29ya2VyRXhlY3V0aW9uQ29tcGxldGVkVjIiEwoRUXVlcnlTdGF0aXN0aWNzVjIiGgoYUXVlcnlDdXJyZW50SW5wdXRUd
  XBsZVYyIpMBCgtMaW5rT3JkaW5hbBJPCgRsaW5rGAEgASgLMi0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmNvbW1vbi5QaHlza
  WNhbExpbmtCDOI/CRIEbGlua/ABAVIEbGluaxIzCgxwb3J0X29yZGluYWwYAiABKANCEOI/DRILcG9ydE9yZGluYWxSC3BvcnRPc
  mRpbmFsIugEChlJbml0aWFsaXplT3BlcmF0b3JMb2dpY1YyEh0KBGNvZGUYASABKAlCCeI/BhIEY29kZVIEY29kZRIqCglpc19zb
  3VyY2UYAiABKAhCDeI/ChIIaXNTb3VyY2VSCGlzU291cmNlEocBChVpbnB1dF9vcmRpbmFsX21hcHBpbmcYAyADKAsyOS5lZHUud
  WNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5MaW5rT3JkaW5hbEIY4j8VEhNpbnB1dE9yZGluYWxNYXBwa
  W5nUhNpbnB1dE9yZGluYWxNYXBwaW5nEooBChZvdXRwdXRfb3JkaW5hbF9tYXBwaW5nGAQgAygLMjkuZWR1LnVjaS5pY3MuYW1iZ
  XIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTGlua09yZGluYWxCGeI/FhIUb3V0cHV0T3JkaW5hbE1hcHBpbmdSFG91dHB1d
  E9yZGluYWxNYXBwaW5nEpEBCg1vdXRwdXRfc2NoZW1hGAUgAygLMlkuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjd
  HVyZS53b3JrZXIuSW5pdGlhbGl6ZU9wZXJhdG9yTG9naWNWMi5PdXRwdXRTY2hlbWFFbnRyeUIR4j8OEgxvdXRwdXRTY2hlbWFSD
  G91dHB1dFNjaGVtYRpVChFPdXRwdXRTY2hlbWFFbnRyeRIaCgNrZXkYASABKAlCCOI/BRIDa2V5UgNrZXkSIAoFdmFsdWUYAiABK
  AlCCuI/BxIFdmFsdWVSBXZhbHVlOgI4ASJiChVNb2RpZnlPcGVyYXRvckxvZ2ljVjISHQoEY29kZRgBIAEoCUIJ4j8GEgRjb2RlU
  gRjb2RlEioKCWlzX3NvdXJjZRgCIAEoCEIN4j8KEghpc1NvdXJjZVIIaXNTb3VyY2UiFgoUUmVwbGF5Q3VycmVudFR1cGxlVjIi4
  wIKDkNvbnNvbGVNZXNzYWdlEioKCXdvcmtlcl9pZBgBIAEoCUIN4j8KEgh3b3JrZXJJZFIId29ya2VySWQSSwoJdGltZXN0YW1wG
  AIgASgLMhouZ29vZ2xlLnByb3RvYnVmLlRpbWVzdGFtcEIR4j8OEgl0aW1lc3RhbXDwAQFSCXRpbWVzdGFtcBJpCghtc2dfdHlwZ
  RgDIAEoDjJALmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkNvbnNvbGVNZXNzYWdlVHlwZUIM4
  j8JEgdtc2dUeXBlUgdtc2dUeXBlEiMKBnNvdXJjZRgEIAEoCUIL4j8IEgZzb3VyY2VSBnNvdXJjZRIgCgV0aXRsZRgFIAEoCUIK4
  j8HEgV0aXRsZVIFdGl0bGUSJgoHbWVzc2FnZRgGIAEoCUIM4j8JEgdtZXNzYWdlUgdtZXNzYWdlIoEBChZQeXRob25Db25zb2xlT
  WVzc2FnZVYyEmcKB21lc3NhZ2UYASABKAsyPC5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Db
  25zb2xlTWVzc2FnZUIP4j8MEgdtZXNzYWdl8AEBUgdtZXNzYWdlIkcKFEV2YWx1YXRlRXhwcmVzc2lvblYyEi8KCmV4cHJlc3Npb
  24YASABKAlCD+I/DBIKZXhwcmVzc2lvblIKZXhwcmVzc2lvbiIyChRXb3JrZXJEZWJ1Z0NvbW1hbmRWMhIaCgNjbWQYASABKAlCC
  OI/BRIDY21kUgNjbWQiHAoaUXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzVjIiYgoPTGlua0NvbXBsZXRlZFYyEk8KBGxpbmsYASABK
  AsyLS5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY29tbW9uLlBoeXNpY2FsTGlua0IM4j8JEgRsaW5r8AEBUgRsaW5rIpEVChBDb
  250cm9sQ29tbWFuZFYyEnIKDHN0YXJ0X3dvcmtlchgBIAEoCzI7LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1c
  mUud29ya2VyLlN0YXJ0V29ya2VyVjJCEOI/DRILc3RhcnRXb3JrZXJIAFILc3RhcnRXb3JrZXIScgoMcGF1c2Vfd29ya2VyGAIgA
  SgLMjsuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuUGF1c2VXb3JrZXJWMkIQ4j8NEgtwYXVzZ
  VdvcmtlckgAUgtwYXVzZVdvcmtlchJ2Cg1yZXN1bWVfd29ya2VyGAMgASgLMjwuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY
  2hpdGVjdHVyZS53b3JrZXIuUmVzdW1lV29ya2VyVjJCEeI/DhIMcmVzdW1lV29ya2VySABSDHJlc3VtZVdvcmtlchKCAQoQYWRkX
  3BhcnRpdGlvbmluZxgEIAEoCzI/LmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLkFkZFBhcnRpd
  GlvbmluZ1YyQhTiPxESD2FkZFBhcnRpdGlvbmluZ0gAUg9hZGRQYXJ0aXRpb25pbmcSjwEKFHVwZGF0ZV9pbnB1dF9saW5raW5nG
  AUgASgLMkIuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuVXBkYXRlSW5wdXRMaW5raW5nVjJCF
  +I/FBISdXBkYXRlSW5wdXRMaW5raW5nSABSEnVwZGF0ZUlucHV0TGlua2luZxKCAQoQcXVlcnlfc3RhdGlzdGljcxgGIAEoCzI/L
  mVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlF1ZXJ5U3RhdGlzdGljc1YyQhTiPxESD3F1ZXJ5U
  3RhdGlzdGljc0gAUg9xdWVyeVN0YXRpc3RpY3MSoAEKGXF1ZXJ5X2N1cnJlbnRfaW5wdXRfdHVwbGUYByABKAsyRi5lZHUudWNpL
  mljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5RdWVyeUN1cnJlbnRJbnB1dFR1cGxlVjJCG+I/GBIWcXVlcnlDd
  XJyZW50SW5wdXRUdXBsZUgAUhZxdWVyeUN1cnJlbnRJbnB1dFR1cGxlEnYKDW9wZW5fb3BlcmF0b3IYCSABKAsyPC5lZHUudWNpL
  mljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5PcGVuT3BlcmF0b3JWMkIR4j8OEgxvcGVuT3BlcmF0b3JIAFIMb
  3Blbk9wZXJhdG9yEnoKDmxpbmtfY29tcGxldGVkGAogASgLMj0uZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZ
  S53b3JrZXIuTGlua0NvbXBsZXRlZFYyQhLiPw8SDWxpbmtDb21wbGV0ZWRIAFINbGlua0NvbXBsZXRlZBKgAQoZc2NoZWR1bGVyX
  3RpbWVfc2xvdF9ldmVudBgLIAEoCzJGLmVkdS51Y2kuaWNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLlNjaGVkd
  WxlclRpbWVTbG90RXZlbnRWMkIb4j8YEhZzY2hlZHVsZXJUaW1lU2xvdEV2ZW50SABSFnNjaGVkdWxlclRpbWVTbG90RXZlbnQSo
  wEKGWluaXRpYWxpemVfb3BlcmF0b3JfbG9naWMYFSABKAsyRy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlL
  ndvcmtlci5Jbml0aWFsaXplT3BlcmF0b3JMb2dpY1YyQhziPxkSF2luaXRpYWxpemVPcGVyYXRvckxvZ2ljSABSF2luaXRpYWxpe
  mVPcGVyYXRvckxvZ2ljEpMBChVtb2RpZnlfb3BlcmF0b3JfbG9naWMYFiABKAsyQy5lZHUudWNpLmljcy5hbWJlci5lbmdpbmUuY
  XJjaGl0ZWN0dXJlLndvcmtlci5Nb2RpZnlPcGVyYXRvckxvZ2ljVjJCGOI/FRITbW9kaWZ5T3BlcmF0b3JMb2dpY0gAUhNtb2RpZ
  nlPcGVyYXRvckxvZ2ljEpcBChZweXRob25fY29uc29sZV9tZXNzYWdlGBcgASgLMkQuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lL
  mFyY2hpdGVjdHVyZS53b3JrZXIuUHl0aG9uQ29uc29sZU1lc3NhZ2VWMkIZ4j8WEhRweXRob25Db25zb2xlTWVzc2FnZUgAUhRwe
  XRob25Db25zb2xlTWVzc2FnZRKPAQoUcmVwbGF5X2N1cnJlbnRfdHVwbGUYGCABKAsyQi5lZHUudWNpLmljcy5hbWJlci5lbmdpb
  mUuYXJjaGl0ZWN0dXJlLndvcmtlci5SZXBsYXlDdXJyZW50VHVwbGVWMkIX4j8UEhJyZXBsYXlDdXJyZW50VHVwbGVIAFIScmVwb
  GF5Q3VycmVudFR1cGxlEo4BChNldmFsdWF0ZV9leHByZXNzaW9uGBkgASgLMkIuZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lLmFyY
  2hpdGVjdHVyZS53b3JrZXIuRXZhbHVhdGVFeHByZXNzaW9uVjJCF+I/FBISZXZhbHVhdGVFeHByZXNzaW9uSABSEmV2YWx1YXRlR
  XhwcmVzc2lvbhKoAQobcXVlcnlfc2VsZl93b3JrbG9hZF9tZXRyaWNzGCkgASgLMkguZWR1LnVjaS5pY3MuYW1iZXIuZW5naW5lL
  mFyY2hpdGVjdHVyZS53b3JrZXIuUXVlcnlTZWxmV29ya2xvYWRNZXRyaWNzVjJCHeI/GhIYcXVlcnlTZWxmV29ya2xvYWRNZXRya
  WNzSABSGHF1ZXJ5U2VsZldvcmtsb2FkTWV0cmljcxKPAQoUd29ya2VyX2RlYnVnX2NvbW1hbmQYUSABKAsyQi5lZHUudWNpLmljc
  y5hbWJlci5lbmdpbmUuYXJjaGl0ZWN0dXJlLndvcmtlci5Xb3JrZXJEZWJ1Z0NvbW1hbmRWMkIX4j8UEhJ3b3JrZXJEZWJ1Z0Nvb
  W1hbmRIAFISd29ya2VyRGVidWdDb21tYW5kEqcBChp3b3JrZXJfZXhlY3V0aW9uX2NvbXBsZXRlZBhlIAEoCzJILmVkdS51Y2kua
  WNzLmFtYmVyLmVuZ2luZS5hcmNoaXRlY3R1cmUud29ya2VyLldvcmtlckV4ZWN1dGlvbkNvbXBsZXRlZFYyQh3iPxoSGHdvcmtlc
  kV4ZWN1dGlvbkNvbXBsZXRlZEgAUhh3b3JrZXJFeGVjdXRpb25Db21wbGV0ZWQSVwoFbm9fb3AYj04gASgLMjQuZWR1LnVjaS5pY
  3MuYW1iZXIuZW5naW5lLmFyY2hpdGVjdHVyZS53b3JrZXIuTm9PcFYyQgniPwYSBG5vT3BIAFIEbm9PcEIOCgxzZWFsZWRfdmFsd
  WUqRQoSQ29uc29sZU1lc3NhZ2VUeXBlEgkKBVBSSU5UEAASCQoFRVJST1IQARILCgdDT01NQU5EEAISDAoIREVCVUdHRVIQA0IJ4
  j8GSABYAHgBYgZwcm90bzM="""
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
      edu.uci.ics.amber.engine.common.workflow.WorkflowProto.javaDescriptor,
      com.google.protobuf.timestamp.TimestampProto.javaDescriptor,
      scalapb.options.ScalapbProto.javaDescriptor
    ))
  }
  @deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")
  def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor
}